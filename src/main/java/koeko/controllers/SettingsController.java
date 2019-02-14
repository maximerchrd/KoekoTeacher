package koeko.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import koeko.Koeko;
import koeko.KoekoSyncCollect.SyncOperations;
import koeko.database_management.DbTableProfessor;
import koeko.database_management.DbTableSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import koeko.view.Professor;
import koeko.view.Utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    /**
     *  -1: not initialized; 0: OFF; 1: ON
     */
    static public Integer correctionMode = -1;

    static public Integer forceSync = -1;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TextField teacherName;
    @FXML
    private ToggleButton correctionModeButton;
    @FXML
    private ToggleButton forceSyncButton;
    @FXML
    private ComboBox languageCombobox;
    @FXML
    private TextField synchronizationKeyTextField;
    @FXML
    private ComboBox appLanguageCombobox;

    private String serverAddress = "127.0.0.1";
    private int serverPort = 50507;


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

    public void forceSyncChanged() {
        if (forceSyncButton.isSelected()) {
            forceSync = 1;
            DbTableSettings.insertForceSync(forceSync);
            forceSyncButton.setText("ON");
        } else {
            forceSync = 0;
            DbTableSettings.insertForceSync(forceSync);
            forceSyncButton.setText("OFF");
        }
    }

    public void setUserName() {
        Professor professor = DbTableProfessor.getProfessor();
        if (professor == null) {
            DbTableProfessor.addProfessor(teacherName.getText(),teacherName.getText(),teacherName.getText());
            DbTableProfessor.setProfessorLanguage(teacherName.getText(), "no language");
        } else if(professor.get_alias().contentEquals(teacherName.getText())) {
            //Do nothing
        } else {
            DbTableProfessor.setProfessorAlias("1", teacherName.getText());
        }
    }

    public void setLanguage() {
        Professor professor = DbTableProfessor.getProfessor();
        String language = languageCombobox.getSelectionModel().getSelectedItem().toString();
        if (professor.get_language() == null || !professor.get_language().contentEquals(language)) {
            DbTableProfessor.setProfessorLanguage(teacherName.getText(), language);
        }
    }

    public void syncWithServer() {
        Platform.runLater(() -> {
            if (languageCombobox.getSelectionModel().getSelectedItem() != null) {
                setUserName();
                setLanguage();
                Boolean success = true;
                if (synchronizationKeyTextField.getText().length() == 20) {
                    DbTableProfessor.setProfessorSyncKey(teacherName.getText(), synchronizationKeyTextField.getText());
                }
                try {
                    SyncOperations.SyncAll(InetAddress.getByName(serverAddress), serverPort, false);
                } catch (Exception e) {
                    success = false;
                    e.printStackTrace();
                }
                if (success) {
                    Koeko.leftBarController.promptGenericPopUp("Synchronization succeeded", "Synchronization");
                } else {
                    Koeko.leftBarController.promptGenericPopUp("Synchronization failed", "Synchronization");
                }
            } else {
                Koeko.leftBarController.promptGenericPopUp("Choose a language before synchronizing", "Language");
            }
        });
    }

    public void resetAndSync() {
        Platform.runLater(() -> {
            if (languageCombobox.getSelectionModel().getSelectedItem() != null) {
                setUserName();
                setLanguage();
                Boolean success = true;
                if (synchronizationKeyTextField.getText().length() == 20) {
                    DbTableProfessor.setProfessorSyncKey(teacherName.getText(), synchronizationKeyTextField.getText());
                }
                try {
                    SyncOperations.SyncAll(InetAddress.getByName(serverAddress), serverPort, true);
                } catch (Exception e) {
                    success = false;
                    e.printStackTrace();
                }
                if (success) {
                    Koeko.leftBarController.promptGenericPopUp("Synchronization succeeded", "Synchronization");
                } else {
                    Koeko.leftBarController.promptGenericPopUp("Synchronization failed", "Synchronization");
                }
            } else {
                Koeko.leftBarController.promptGenericPopUp("Choose a language before synchronizing", "Language");
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        correctionMode = DbTableSettings.getCorrectionMode();
        forceSync = DbTableSettings.getForceSync();

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

        if (forceSync == 0) {
            forceSyncButton.setText("OFF");
        } else if (forceSync == 1) {
            forceSyncButton.setText("ON");
            forceSyncButton.setSelected(true);
        }

        if (DbTableProfessor.getProfessor() == null) {
            DbTableProfessor.addProfessor(teacherName.getText(), teacherName.getText(), teacherName.getText());
        }

        appLanguageCombobox.setItems(data);
        appLanguageCombobox.getSelectionModel().select(Utilities.codeToLanguageMap.get(DbTableSettings.getLanguage()));
    }

    public void requestHomeworkKey() {
        try {
            SyncOperations.RequestNewHomeworkKey(InetAddress.getByName(serverAddress), serverPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeAppLanguage() {
        String languageCode = Utilities.languageToCodeMap.get(appLanguageCombobox.getSelectionModel().getSelectedItem().toString());
        DbTableSettings.insertLanguage(languageCode);
        try {
            Koeko.loadView(new Locale(languageCode), getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
