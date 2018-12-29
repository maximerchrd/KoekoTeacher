package koeko.controllers;

import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import koeko.controllers.ClassesControlling.ClassTreeCell;
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
public class CreateClassController extends Window implements Initializable {
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
        if (classroomTreeItem.getTreeItem().getParent() != null) {
            classLevel.setVisible(false);
            classYear.setVisible(false);
            classYearLabel.setVisible(false);
            classLevelLabel.setVisible(false);
        }
    }

    public void saveClass() {
        if (classroomTreeItem.getTreeItem().getParent() == null) {
            DbTableClasses.addClass(className.getText(), classLevel.getText(), classYear.getText());
        } else {
            DbTableClasses.addGroupToClass(className.getText(), classroomTreeItem.getItem().getClassName());
        }
        Classroom classroom  = new Classroom();
        classroom.setClassName(className.getText());
        classroomTreeItem.getTreeItem().getChildren().add(new TreeItem<>(classroom));
        Stage stage = (Stage) className.getScene().getWindow();
        stage.close();
    }
}
