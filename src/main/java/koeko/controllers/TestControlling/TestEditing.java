package koeko.controllers.TestControlling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.Tools.FilesHandler;
import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableTest;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class TestEditing {
    public static String formativeTest = "Formative test";
    public static String certificativeTest = "Certificative test";
    public static String game = "Game";
    public static String questionSet = "Question set for game";
    public static String[] testTypes = {formativeTest, certificativeTest, game, questionSet};

    public static void toggleMedals(CheckBox medalsCheckbox, TextField goldMedalTime, TextField goldMedalScore,
                                    TextField silverMedalTime, TextField silverMedalScore, TextField bronzeMedalTime,
                                    TextField bronzeMedalScore) {
        if (medalsCheckbox.isSelected()) {
            goldMedalTime.setEditable(true);
            goldMedalScore.setEditable(true);
            silverMedalTime.setEditable(true);
            silverMedalScore.setEditable(true);
            bronzeMedalTime.setEditable(true);
            bronzeMedalScore.setEditable(true);
        } else {
            goldMedalTime.setEditable(false);
            goldMedalScore.setEditable(false);
            silverMedalTime.setEditable(false);
            silverMedalScore.setEditable(false);
            bronzeMedalTime.setEditable(false);
            bronzeMedalScore.setEditable(false);
        }
    }

    public static void ShowMediaFileChoser(TextField mediaPath) {
        FilesHandler.createMediaDirIfNotExists();

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Web, Audio or Video Files",
                FilesHandler.supportedMediaExtensions);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Select Media file");
        Stage stage = (Stage) mediaPath.getScene().getWindow();
        File source_file = fileChooser.showOpenDialog(stage);

        File hashedFile = FilesHandler.saveMediaFile(source_file);

        mediaPath.setText(hashedFile.getName());
        mediaPath.setEditable(false);
    }

    public static void testTypeChanged(ComboBox testTypeCombobox, Button addObjectiveButton,
                                       ArrayList<ComboBox> objectivesComboBoxArrayList) {
        if (testTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals(TestEditing.certificativeTest)) {
            addObjectiveButton.setDisable(false);
            for (ComboBox comboBox : objectivesComboBoxArrayList) {
                comboBox.setDisable(false);
            }
        } else {
            addObjectiveButton.setDisable(true);
            for (ComboBox comboBox : objectivesComboBoxArrayList) {
                comboBox.setDisable(true);
            }
        }
    }

    public static ComboBox addObjectiveField(ArrayList<ComboBox> objectivesComboBoxArrayList, VBox vBoxObjectives) {
        Vector<String> objectivessVector = DbTableLearningObjectives.getAllObjectives();
        String[] objectives = objectivessVector.toArray(new String[objectivessVector.size()]);;
        ObservableList<String> options =
                FXCollections.observableArrayList(objectives);
        ComboBox comboBox = new ComboBox(options);
        comboBox.setEditable(true);
        objectivesComboBoxArrayList.add(comboBox);

        HBox hBox = new HBox();
        //button for removing subject
        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            objectivesComboBoxArrayList.remove(comboBox);
            (( VBox)hBox.getParent()).getChildren().remove(hBox);
        });
        hBox.getChildren().add(comboBox);
        hBox.getChildren().add(removeButton);
        vBoxObjectives.getChildren().add(hBox);
        TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());

        return comboBox;
    }

    public static void nameCollisionWarning(Window window) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(window);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("This test name already exists. Please choose an other name."));
        Scene dialogScene = new Scene(dialogVbox, 400, 40);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public static void addMedalsToTest(CheckBox medalsCheckbox, TextField bronzeMedalTime, TextField bronzeMedalScore,
                                       TextField silverMedalTime, TextField silverMedalScore, TextField goldMedalTime,
                                       TextField goldMedalScore, TextField testName) {
        if (medalsCheckbox.isSelected()) {
            String medals = "";
            String bronzeTime;
            String silverTime;
            String goldTime;
            try {
                Long time = Long.valueOf(bronzeMedalTime.getText());
                bronzeTime = String.valueOf(time);
            } catch (NumberFormatException e) {
                bronzeTime = "0";
            }
            try {
                Long time = Long.valueOf(silverMedalTime.getText());
                silverTime = String.valueOf(time);
            } catch (NumberFormatException e) {
                silverTime = "0";
            }
            try {
                Long time = Long.valueOf(goldMedalTime.getText());
                goldTime = String.valueOf(time);
            } catch (NumberFormatException e) {
                goldTime = "0";
            }

            String bronzeScore;
            String silverScore;
            String goldScore;
            try {
                Double score = Double.valueOf(bronzeMedalScore.getText());
                bronzeScore = String.valueOf(score);
            } catch (NumberFormatException e) {
                bronzeScore = "100";
            }
            try {
                Double score = Double.valueOf(silverMedalScore.getText());
                silverScore = String.valueOf(score);
            } catch (NumberFormatException e) {
                silverScore = "100";
            }
            try {
                Double score = Double.valueOf(goldMedalScore.getText());
                goldScore = String.valueOf(score);
            } catch (NumberFormatException e) {
                goldScore = "100";
            }

            medals += "bronze:" + bronzeTime + "/" + bronzeScore + ";";
            medals += "silver:" + silverTime + "/" + silverScore + ";";
            medals += "gold:" + goldTime + "/" + goldScore + ";";
            DbTableTest.setMedals(testName.getText(), medals);
        }
    }

    public static void addMediaFile(TextField mediaPath, CheckBox sendMedia, TextField testName) {
        if (!mediaPath.getText().contentEquals("none")) {
            DbTableTest.setMediaFile(mediaPath.getText(), testName.getText());
        }

        if (!sendMedia.isSelected()) {
            DbTableTest.setSendMediaFile(0, testName.getText());
        }
    }
}
