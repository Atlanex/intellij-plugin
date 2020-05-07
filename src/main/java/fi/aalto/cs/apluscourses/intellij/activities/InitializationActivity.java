package fi.aalto.cs.apluscourses.intellij.activities;

import com.intellij.notification.Notifications;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.aalto.cs.apluscourses.intellij.actions.ActionUtil;
import fi.aalto.cs.apluscourses.intellij.actions.RequiredPluginsCheckerAction;
import fi.aalto.cs.apluscourses.intellij.model.IntelliJModelFactory;
import fi.aalto.cs.apluscourses.intellij.notifications.ClientIoError;
import fi.aalto.cs.apluscourses.intellij.notifications.CourseConfigurationError;
import fi.aalto.cs.apluscourses.intellij.notifications.Notifier;
import fi.aalto.cs.apluscourses.intellij.services.PluginSettings;
import fi.aalto.cs.apluscourses.intellij.utils.ExtendedDataContext;
import fi.aalto.cs.apluscourses.model.Component;
import fi.aalto.cs.apluscourses.model.Course;
import fi.aalto.cs.apluscourses.model.MalformedCourseConfigurationFileException;
import fi.aalto.cs.apluscourses.model.UnexpectedResponseException;
import fi.aalto.cs.apluscourses.presentation.CourseViewModel;
import fi.aalto.cs.apluscourses.utils.CoursesClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InitializationActivity implements StartupActivity, DumbAware {
  private static final Logger logger = LoggerFactory.getLogger(InitializationActivity.class);

  @NotNull
  private final Notifier notifier;

  public InitializationActivity() {
    this(Notifications.Bus::notify);
  }

  public InitializationActivity(@NotNull Notifier notifier) {
    this.notifier = notifier;
  }

  @Override
  public void runActivity(@NotNull Project project) {
    Course course;
    try {
      InputStream inputStream = CoursesClient.fetchJson(
          new URL(PluginSettings.COURSE_CONFIGURATION_FILE_URL));
      course = Course.fromConfigurationData(
          new InputStreamReader(inputStream),
          PluginSettings.COURSE_CONFIGURATION_FILE_URL,
          new IntelliJModelFactory(project));
    } catch (UnexpectedResponseException | MalformedCourseConfigurationFileException e) {
      logger.error("Error occurred while trying to parse a course configuration file", e);
      notifier.notify(new CourseConfigurationError(e), null);
      return;
    } catch (IOException e) {
      logger.info("IOException occurred while using the HTTP client", e);
      notifier.notify(new ClientIoError(e), null);
      return;
    }
    PluginSettings.getInstance()
        .getMainViewModel(project).courseViewModel.set(new CourseViewModel(course));
    ActionUtil.launch(RequiredPluginsCheckerAction.ACTION_ID,
        new ExtendedDataContext().withProject(project));
  }
}
