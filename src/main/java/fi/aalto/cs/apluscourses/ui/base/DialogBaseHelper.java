package fi.aalto.cs.apluscourses.ui.base;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class DialogBaseHelper extends JDialog {

  protected void addDefaultListeners(JButton buttonOk, JButton buttonCancel, JPanel contentPane) {
    buttonOk.addActionListener(e -> onOk());

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane
        .registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  protected void onOk() {
    // add your code here
    dispose();
  }

  protected void onCancel() {
    // add your code here if necessary
    dispose();
  }

}
