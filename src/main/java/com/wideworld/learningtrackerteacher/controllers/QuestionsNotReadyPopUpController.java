package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.Networking.NetworkCommunication;
import com.wideworld.learningtrackerteacher.database_management.DbTableRelationQuestionTest;
import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class QuestionsNotReadyPopUpController extends Window {
    @FXML private Label text;
    QuestionGeneric questionGeneric;

    public void initParameters(QuestionGeneric questionGeneric) {
        this.questionGeneric = questionGeneric;
    }
    public void waitQuestions() {
        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }

    public void sendAnyway() {
        try {
            if (questionGeneric.getGlobalID() > 0) {
                NetworkCommunication.networkCommunicationSingleton.SendQuestionID(questionGeneric.getGlobalID());
            } else {
                ArrayList<Integer> questionIds = DbTableRelationQuestionTest.getQuestionIdsFromTestName(questionGeneric.getQuestion());
                NetworkCommunication.networkCommunicationSingleton.activateTest(questionIds, questionGeneric.getGlobalID());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = (Stage) text.getScene().getWindow();
        stage.close();
    }
}
