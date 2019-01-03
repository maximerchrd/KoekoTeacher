package koeko.controllers.LeftBar.ClassesControlling;

import javafx.scene.control.Label;
import koeko.database_management.DbTableClasses;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.students_management.Classroom;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 14.03.18.
 */
public class EditClassController extends Window implements Initializable {
    ClassTreeCell classroomTreeItem;

    @FXML private TextField className;
    @FXML private TextField classLevel;
    @FXML private TextField classYear;
    @FXML private Label classLevelLabel;
    @FXML private Label classYearLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initializeParameters(ClassTreeCell classroomTreeItem) {
        this.classroomTreeItem = classroomTreeItem;
        className.setText(classroomTreeItem.getItem().getClassName());
        if (classroomTreeItem.getTreeItem().getParent().getParent() != null) {
            classLevel.setVisible(false);
            classYear.setVisible(false);
            classYearLabel.setVisible(false);
            classLevelLabel.setVisible(false);
        } else {
            Classroom classroom = DbTableClasses.getClassroomFromName(classroomTreeItem.getItem().getClassName());
            classLevel.setText(classroom.getClassLevel());
            classYear.setText(classroom.getClassYear());
        }
    }

    public void saveClass() {
        if (classroomTreeItem.getTreeItem().getParent().getParent() == null) {
            DbTableClasses.updateClass(className.getText(), classroomTreeItem.getItem().getClassName(), classYear.getText(), classLevel.getText());
        } else {
            DbTableClasses.updateGroup(className.getText(), classroomTreeItem.getItem().getClassName());
        }
        classroomTreeItem.getItem().setClassName(className.getText());
        classroomTreeItem.updateItem(classroomTreeItem.getItem(), false);
        Stage stage = (Stage) className.getScene().getWindow();
        stage.close();
    }
}
