package koeko.controllers;

import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.questions_management.QuestionGeneric;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

public class QuestionsNotReadyPopUpController extends Window {
    @FXML private Label text;
    QuestionGeneric questionGeneric;

    public void initParameters(QuestionGeneric questionGeneric) {
        this.questionGeneric = questionGeneric;
        text.setText("Some questions might not be saved on all devices. \nDo you really want to try to activate the question now?");
    }

    public void waitQuestions() {
        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }

    public void sendAnyway() {
            if (questionGeneric.getGlobalID() > 0) {
                NetworkCommunication.networkCommunicationSingleton.SendQuestionID(questionGeneric.getGlobalID());
            } else {
                Koeko.questionSendingControllerSingleton.activateTestSynchroneousQuestions();
            }

        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }
}
