package fi.aalto.cs.apluscourses.ui.courseproject;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import fi.aalto.cs.apluscourses.presentation.CourseProjectViewModel;
import fi.aalto.cs.apluscourses.ui.GuiObject;
import fi.aalto.cs.apluscourses.ui.IconListCellRenderer;
import fi.aalto.cs.apluscourses.ui.base.CheckBox;
import fi.aalto.cs.apluscourses.ui.base.OurComboBox;
import fi.aalto.cs.apluscourses.ui.base.OurDialogWrapper;
import fi.aalto.cs.apluscourses.ui.base.TemplateLabel;
import fi.aalto.cs.apluscourses.utils.APlusLocalizationUtil;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CourseProjectView extends OurDialogWrapper {
  private JPanel basePanel;
  private CourseProjectViewModel viewModel;

  @GuiObject
  private TemplateLabel infoText;
  @GuiObject
  private TemplateLabel currentSettingsText;
  @GuiObject
  private JLabel settingsInfoText;
  @GuiObject
  private CheckBox settingsOptOutCheckbox;
  @GuiObject
  private JLabel warningText;
  @GuiObject
  private OurComboBox<String> languageComboBox;

  CourseProjectView(@NotNull Project project,
                    @NotNull CourseProjectViewModel viewModel) {
    super(project);

    this.viewModel = viewModel;

    setTitle("Turn Project Into A+ Course Project");
    setButtonsAlignment(SwingConstants.CENTER);

    registerValidationItem(languageComboBox.selectedItemBindable);

    infoText.setIcon(Messages.getInformationIcon());

    settingsOptOutCheckbox.isCheckedBindable.bindToSource(viewModel.settingsOptOutProperty);

    settingsOptOutCheckbox.setEnabled(viewModel.canUserOptOutSettings());

    warningText.setVisible(viewModel.shouldWarnUser());
    settingsInfoText.setVisible(viewModel.shouldShowSettingsInfo());

    infoText.applyTemplate(viewModel.getCourseName());

    currentSettingsText.applyTemplate(viewModel.getCourseName());
    currentSettingsText.setVisible(viewModel.shouldShowCurrentSettings());

    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return basePanel;
  }

  @NotNull
  @Override
  protected Action[] createActions() {
    return new Action[] { getOKAction(), getCancelAction() };
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void createUIComponents() {
    languageComboBox = new OurComboBox<>(viewModel.getLanguages(), String.class);
    languageComboBox.setRenderer(new IconListCellRenderer<>(
        "Select language",
        APlusLocalizationUtil::languageCodeToName,
        null));
    languageComboBox.selectedItemBindable.bindToSource(viewModel.languageProperty);
  }
}
