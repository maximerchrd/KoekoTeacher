package koeko.controllers;

import koeko.students_management.Student;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 12.03.18.
 */
public class LearningTrackerController implements Initializable {
    @FXML private AnchorPane ClassroomActivityTab;
    @FXML private ClassroomActivityTabController ClassroomActivityTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void addQuestion(String question, String id, Integer group) {
        ClassroomActivityTabController.addQuestion(question,id, group);
    }
    public void addUser(Student UserStudent, Boolean connection) {
        ClassroomActivityTabController.addUser(UserStudent,connection);
    }
    public void removeQuestion(int index) {
        ClassroomActivityTabController.removeQuestion(index);
    }
    public void userDisconnected(Student student) {
        ClassroomActivityTabController.userDisconnected(student);
    }
}
