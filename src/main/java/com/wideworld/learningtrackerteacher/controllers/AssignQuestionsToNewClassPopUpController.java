package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.LearningTracker;
import com.wideworld.learningtrackerteacher.database_management.DbTableClasses;
import com.wideworld.learningtrackerteacher.database_management.DbTableRelationClassQuestion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

public class AssignQuestionsToNewClassPopUpController extends Window implements Initializable {
    private String className;
    @FXML private Label label;

    public void initParameters(String className) {
        this.className = className;
    }

    public void assignQuestions() {
        ArrayList<Integer>  questionIds = DbTableRelationClassQuestion.getQuestionsIDsForClass(className);
        for (Integer id : questionIds) {
            if (!QuestionSendingController.IDsFromBroadcastedQuestions.contains(String.valueOf(id))) {
                QuestionSendingController.IDsFromBroadcastedQuestions.add(String.valueOf(id));
            }
        }
        for (String id : QuestionSendingController.IDsFromBroadcastedQuestions) {
            DbTableRelationClassQuestion.addClassQuestionRelation(className, id);
        }
        LearningTracker.questionSendingControllerSingleton.refreshReadyQuestionsList();
        Stage stage = (Stage) label.getScene().getWindow();
        stage.close();
    }

    public void doNotAssign() {
        QuestionSendingController.IDsFromBroadcastedQuestions.removeAllElements();
        ArrayList<Integer>  questionIds = DbTableRelationClassQuestion.getQuestionsIDsForClass(className);
        for (Integer id : questionIds) {
            QuestionSendingController.IDsFromBroadcastedQuestions.add(String.valueOf(id));
        }
        LearningTracker.questionSendingControllerSingleton.refreshReadyQuestionsList();
        Stage stage = (Stage) label.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
