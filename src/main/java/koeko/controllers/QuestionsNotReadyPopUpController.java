package koeko.controllers;

import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.questions_management.QuestionGeneric;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.students_management.Student;

import java.util.Vector;

public class QuestionsNotReadyPopUpController extends Window {
    @FXML private Label text;
    QuestionGeneric questionGeneric;
    Vector<Student> students;

    public void initParameters(QuestionGeneric questionGeneric, Vector<Student> students) {
        this.questionGeneric = questionGeneric;
        this.students = students;
        text.setText("Some questions might not be saved on all devices. \nDo you really want to try to activate the question now?");
    }

    public void waitQuestions() {
        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }

    public void sendAnyway() {
            if (Long.valueOf(questionGeneric.getGlobalID()) > 0) {
                NetworkCommunication.networkCommunicationSingleton.SendQuestionID(questionGeneric.getGlobalID(),students);
            } else {
                Koeko.questionSendingControllerSingleton.activateTestSynchroneousQuestions();
            }

        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }
}
