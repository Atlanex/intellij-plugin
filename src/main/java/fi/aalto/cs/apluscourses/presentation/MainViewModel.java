package fi.aalto.cs.apluscourses.presentation;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.dal.PasswordStorage;
import fi.aalto.cs.apluscourses.dal.TokenAuthentication;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.CourseFileManager;
import fi.aalto.cs.apluscourses.model.Authentication;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.ExerciseDataSource;
import fi.aalto.cs.apluscourses.model.ExerciseGroup;
import fi.aalto.cs.apluscourses.model.InvalidAuthenticationException;
import fi.aalto.cs.apluscourses.model.Points;
import fi.aalto.cs.apluscourses.model.SubmissionResult;
import fi.aalto.cs.apluscourses.presentation.base.BaseViewModel;
import fi.aalto.cs.apluscourses.presentation.exercise.ExercisesTreeViewModel;
import fi.aalto.cs.apluscourses.presentation.filter.Options;
import fi.aalto.cs.apluscourses.utils.Event;
import fi.aalto.cs.apluscourses.utils.observable.ObservableProperty;
import fi.aalto.cs.apluscourses.utils.observable.ObservableReadWriteProperty;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainViewModel {

  public static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);

  public final Event disposing = new Event();

  @NotNull
  public final ObservableProperty<CourseViewModel> courseViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<ExercisesTreeViewModel> exercisesViewModel =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  public final ObservableProperty<Authentication> authentication =
      new ObservableReadWriteProperty<>(null);

  @NotNull
  private final Options exerciseFilterOptions;

  private AtomicBoolean hasTriedToReadAuthenticationFromStorage = new AtomicBoolean(false);

  private Project project;

  /**
   * Instantiates a class representing the whole main view of the plugin.
   */
  public MainViewModel(@NotNull Options exerciseFilterOptions) {
    this.exerciseFilterOptions = exerciseFilterOptions;
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
    authentication.addValueObserver(this, MainViewModel::updateExercises);
  }


  public MainViewModel(@NotNull Options exerciseFilterOptions, @NotNull Project project) {
    this.exerciseFilterOptions = exerciseFilterOptions;
    this.project = project;
    courseViewModel.addValueObserver(this, MainViewModel::updateExercises);
    authentication.addValueObserver(this, MainViewModel::updateExercises);
  }

  private void updateExercises() {
    Course course = Optional.ofNullable(courseViewModel.get())
        .map(BaseViewModel::getModel)
        .orElse(null);
    Authentication auth = authentication.get();
    if (course == null || auth == null) {
      exercisesViewModel.set(null);
      return;
    }
    ExerciseDataSource dataSource = course.getExerciseDataSource();
    try {
      Points points = dataSource.getPoints(course, auth);
      List<ExerciseGroup> exerciseGroups = dataSource.getExerciseGroups(course, points, auth);
      if (project != null) {
        for (ExerciseGroup exerciseGroup : exerciseGroups) {
          CourseFileManager courseFileManager = PluginSettings
                              .getInstance()
                              .getCourseFileManager(project);
          exerciseGroup.getExercises().forEach((exerciseId, exercise) -> {
            if (exercise.isOptional()) {
              for (SubmissionResult submissionResult : exercise.getSubmissionResults()) {
                HashMap<String, Integer> newResults =
                        (HashMap<String, Integer>) courseFileManager
                                    .getTestResults(submissionResult.getId());
                submissionResult.setTestResults(newResults);
                if (newResults.isEmpty()) {
                  try {
                    SubmissionResult newResult = dataSource.getSubmissionResult(
                            course.getApiUrl() + "/submissions/" + submissionResult.getId(),
                            exercise,
                            auth);
                    submissionResult.setTestResults(newResult.getTestResults());
                    courseFileManager.addTestResultsEntry(newResult);
                  } catch (IOException e) {
                    logger.error("Failed to fetch submission feedback", e);
                  }
                }
              }
            }
          });
        }
      }
      exercisesViewModel.set(new ExercisesTreeViewModel(exerciseGroups, exerciseFilterOptions));
    } catch (InvalidAuthenticationException e) {
      logger.error("Failed to fetch exercises", e);
      // TODO: might want to communicate this to the user somehow
    } catch (IOException e) {
      logger.error("Failed to fetch exercises", e);
      // This too
    }
  }

  public void dispose() {
    disposing.trigger();
  }

  public void setAuthentication(Authentication auth) {
    disposing.addListener(auth, Authentication::clear);
    Optional.ofNullable(authentication.getAndSet(auth)).ifPresent(Authentication::clear);
  }

  /**
   * <p>Sets authentication to the one that is read from the password storage and constructed with
   * the given factory.</p>
   *
   * <p>This method does anything only when it's called the first time for an instance. All the
   * subsequent calls do nothing.</p>
   *
   * @param passwordStorage Password storage.
   * @param factory         Authentication factory.
   */
  public void readAuthenticationFromStorage(@Nullable PasswordStorage passwordStorage,
                                            @NotNull TokenAuthentication.Factory factory) {
    if (hasTriedToReadAuthenticationFromStorage.getAndSet(true) || authentication.get() != null) {
      return;
    }
    Optional.ofNullable(passwordStorage)
        .map(PasswordStorage::restorePassword)
        .map(factory::create)
        .ifPresent(this::setAuthentication);
  }

  @Nullable
  public ExercisesTreeViewModel getExercises() {
    return exercisesViewModel.get();
  }

  @NotNull
  public Options getExerciseFilterOptions() {
    return exerciseFilterOptions;
  }
}
