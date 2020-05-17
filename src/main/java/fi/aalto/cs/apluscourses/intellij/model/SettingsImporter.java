package fi.aalto.cs.apluscourses.intellij.model;

import com.intellij.openapi.project.Project;
import fi.aalto.cs.apluscourses.model.Course;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public interface SettingsImporter {

  void importIdeSettings(@NotNull Course course) throws IOException;

  @NotNull
  String lastImportedIdeSettings();

  void importProjectSettings(@NotNull Project project, @NotNull Course course) throws IOException;

}
