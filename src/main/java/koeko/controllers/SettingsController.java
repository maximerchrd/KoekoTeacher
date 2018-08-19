package koeko.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import koeko.Koeko;
import koeko.KoekoSyncCollect.SyncOperations;
import koeko.database_management.DbTableProfessor;
import koeko.database_management.DbTableQuestionGeneric;
import koeko.database_management.DbTableQuestionMultipleChoice;
import koeko.database_management.DbTableSettings;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.QuestionMultipleChoice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import koeko.view.Professor;
import koeko.view.Utilities;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

public class SettingsController implements Initializable {

    /**
     *  -1: not initialized; 0: OFF; 1: ON
     */
    static public Integer correctionMode = -1;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TextField teacherName;
    @FXML
    private ToggleButton correctionModeButton;
    @FXML
    private ComboBox languageCombobox;
    @FXML
    private TextField synchronizationKeyTextField;


    public void correctionModeChanged() {
        if (correctionModeButton.isSelected()) {
            correctionMode = 1;
            DbTableSettings.insertCorrectionMode(correctionMode);
            correctionModeButton.setText("ON");
        } else {
            correctionMode = 0;
            DbTableSettings.insertCorrectionMode(correctionMode);
            correctionModeButton.setText("OFF");
        }
    }

    public void setUserName() {
        Professor professor = DbTableProfessor.getProfessor();
        if (professor == null) {
            DbTableProfessor.addProfessor(teacherName.getText(),teacherName.getText(),teacherName.getText());
        } else {
            DbTableProfessor.setProfessorAlias("1", teacherName.getText());
        }
    }

    public void setLanguage() {
        String language = languageCombobox.getSelectionModel().getSelectedItem().toString();
        DbTableProfessor.setProfessorLanguage(teacherName.getText(),language);
    }

    public void syncWithServer() {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                if (languageCombobox.getSelectionModel().getSelectedItem() != null) {
                    Boolean success = true;
                    DbTableProfessor.setProfessorSyncKey(teacherName.getText(), synchronizationKeyTextField.getText());
                    try {
                        SyncOperations.SyncAll(InetAddress.getByName("127.0.0.1"), 50507);
                    } catch (Exception e) {
                        success = false;
                        e.printStackTrace();
                    }
                    if (success) {
                        Koeko.questionBrowsingControllerSingleton.promptGenericPopUp("Synchronization succeeded", "Synchronization");
                    } else {
                        Koeko.questionBrowsingControllerSingleton.promptGenericPopUp("Synchronization failed", "Synchronization");
                    }
                } else {
                    Koeko.questionBrowsingControllerSingleton.promptGenericPopUp("Chose a language before synchronizing", "Language");
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        correctionMode = DbTableSettings.getCorrectionMode();

        Utilities.initCodeToLanguageMap();
        Utilities.initLanguageToCodeMap();
        ObservableList<String> data = FXCollections.observableArrayList(Utilities.codeToLanguageMap.values());
        languageCombobox.setItems(data);
        Professor professor = DbTableProfessor.getProfessor();
        if (professor == null) {
            teacherName.setText("No Name");
        } else {
            teacherName.setText(professor.get_alias());
            if (professor.get_language() != null) {
                languageCombobox.getSelectionModel().select(Utilities.codeToLanguageMap.get(professor.get_language()));
            }
        }

        if (correctionMode == 0) {
            correctionModeButton.setText("OFF");
        } else if (correctionMode == 1) {
            correctionModeButton.setText("ON");
            correctionModeButton.setSelected(true);
        }
    }
}
