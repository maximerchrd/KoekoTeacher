package koeko.controllers;

import koeko.controllers.StudentsVsQuestions.StudentsVsQuestionsTableController;
import koeko.students_management.Student;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 19.02.18.
 */
public class ClassroomActivityTabController implements Initializable {

    static int screenWidth = 0;
    static int screenHeight = 0;

    @FXML private AnchorPane studentsQuestionsTable;
    @FXML private StudentsVsQuestionsTableController studentsQuestionsTableController;
    @FXML private Tab classroom_activity_tab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }



    public void addQuestion(String question, String id, Integer group) {
        studentsQuestionsTableController.addQuestion(question,id, group);
    }
    public void addUser(Student UserStudent, Boolean connection) {
        studentsQuestionsTableController.addUser(UserStudent,connection);
    }
    public void addAnswerForUser(Student student, String answer, String question, double evaluation, String questionId, Integer groupIndex) {
        studentsQuestionsTableController.addAnswerForUser(student,answer,question,evaluation,questionId, groupIndex);
    }
    public void removeQuestion(int index) {
        studentsQuestionsTableController.removeQuestion(index);
    }
    public void userDisconnected(Student student) {
        studentsQuestionsTableController.userDisconnected(student);
    }
}
