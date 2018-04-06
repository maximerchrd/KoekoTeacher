package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.Networking.NetworkCommunication;
import com.wideworld.learningtrackerteacher.database_management.DbTableIndividualQuestionForStudentResult;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 14.03.18.
 */
public class EditEvaluationController implements Initializable {
    String identifier = "";
    Integer studentID = -1;
    Integer globalID = -1;

    @FXML private TextField currentEval;
    @FXML private TextField newEval;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initializeVariable(Integer globalID, Integer studentID) {
        this.studentID = studentID;
        this.globalID = globalID;
        String evalAndIdentifier = DbTableIndividualQuestionForStudentResult.getEvalForQuestionAndStudentIDs(globalID, studentID);
        if (evalAndIdentifier != null && evalAndIdentifier.split("///").length > 1) {
            Double oldEval = Double.valueOf(evalAndIdentifier.split("///")[0]);
            currentEval.setText(String.valueOf(oldEval));
            identifier = evalAndIdentifier.split("///")[1];
        }
    }

    public void saveNewEvaluation() {
        DbTableIndividualQuestionForStudentResult.setEvalForQuestionAndStudentIDs(Double.valueOf(newEval.getText()),
                identifier);
        NetworkCommunication.networkCommunicationSingleton.UpdateEvaluation(Double.valueOf(newEval.getText()), globalID, studentID);
        Stage stage = (Stage) currentEval.getScene().getWindow();
        stage.close();
    }
}
