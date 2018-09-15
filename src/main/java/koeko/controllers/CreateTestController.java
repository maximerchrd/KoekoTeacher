package koeko.controllers;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableRelationObjectiveTest;
import koeko.database_management.DbTableTests;
import koeko.questions_management.QuestionGeneric;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.questions_management.Test;
import org.controlsfx.control.textfield.TextFields;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

public class CreateTestController extends Window implements Initializable {
    private TreeItem<QuestionGeneric> root;
    private ArrayList<String> testNames;
    private ArrayList<HBox> hBoxArrayList;
    private ArrayList<ComboBox> objectivesComboBoxArrayList;
    private Integer testMode;

    @FXML private TextField testName;
    @FXML private VBox vBoxObjectives;
    @FXML private CheckBox certificativeCheckBox;
    @FXML private Button addObjectiveButton;
    @FXML private TextField goldMedalTime;
    @FXML private TextField goldMedalScore;
    @FXML private TextField silverMedalTime;
    @FXML private TextField silverMedalScore;
    @FXML private TextField bronzeMedalTime;
    @FXML private TextField bronzeMedalScore;
    @FXML private CheckBox medalsCheckbox;
    @FXML private TextField mediaPath;

    public void initParameters(TreeItem<QuestionGeneric> root, ArrayList<String> testNames) {
        this.root = root;
        this.testNames = testNames;
        hBoxArrayList = new ArrayList<>();
        objectivesComboBoxArrayList = new ArrayList<>();
        testMode = 1;
        goldMedalTime.setEditable(false);
        goldMedalScore.setEditable(false);
        silverMedalTime.setEditable(false);
        silverMedalScore.setEditable(false);
        bronzeMedalTime.setEditable(false);
        bronzeMedalScore.setEditable(false);
    }

    public void addObjective() {
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
    }

    public void certificativeCheckBoxAction() {
        if (!certificativeCheckBox.isSelected()) {
            addObjectiveButton.setDisable(true);
            for (ComboBox comboBox : objectivesComboBoxArrayList) {
                comboBox.setDisable(true);
            }
        } else {
            addObjectiveButton.setDisable(false);
            for (ComboBox comboBox : objectivesComboBoxArrayList) {
                comboBox.setDisable(false);
            }
        }
    }

    public void togglingMedals() {
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

    public void addMediaFile() {
        String dirName = "media";
        File theDir = new File(dirName);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                System.out.println("DIR created");
            }
        }

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Music or Video Files",
                "*.mp3", "*.wav", "*.mp4", "*.avi");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Select Media file");
        Stage stage = (Stage) mediaPath.getScene().getWindow();
        File source_file = fileChooser.showOpenDialog(stage);
        String directory = dirName + "/";
        File dest_file = new File(directory + source_file.getName());
        File hashedFileName = new File(directory + source_file.getName());
        try {
            Files.copy(source_file.toPath(), dest_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = Files.readAllBytes(dest_file.toPath());
            messageDigest.update(hashedBytes);
            String encryptedString = DatatypeConverter.printHexBinary(messageDigest.digest());
            hashedFileName = new File(directory + encryptedString);
            Files.move(dest_file.toPath(), hashedFileName.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        mediaPath.setText(hashedFileName.getName());
        mediaPath.setEditable(false);
    }

    public void saveTest() {
        if (!testNames.contains(testName.getText())) {
            Test newTest = new Test();
            newTest.setTestName(testName.getText());
            if (certificativeCheckBox.isSelected()) {
                newTest.setTestMode(0);
            } else {
                newTest.setTestMode(1);
            }
            String testID = DbTableTests.addTest(newTest);
            TreeItem<QuestionGeneric> testTreeItem = new TreeItem<>();
            QuestionGeneric test = new QuestionGeneric();
            test.setGlobalID(QuestionGeneric.changeIdSign(testID));
            test.setQuestion(testName.getText());

            //set the type of resource (formative/certificative test)
            if (certificativeCheckBox.isSelected()) {
                test.setTypeOfQuestion("TECE");
            } else {
                test.setTypeOfQuestion("TEFO");
            }

            testTreeItem.setValue(test);
            root.getChildren().add(testTreeItem);

            //add objectives to test
            if (certificativeCheckBox.isSelected()) {
                for (ComboBox objectiveCombo : objectivesComboBoxArrayList) {
                    try {
                        DbTableLearningObjectives.addObjective(objectiveCombo.getEditor().getText(), -1);
                        DbTableRelationObjectiveTest.addRelationObjectiveTest(objectiveCombo.getEditor().getText(), testName.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //add medals to test
            if (medalsCheckbox.isSelected()) {
                String medals = "";
                medals += "bronze:" + bronzeMedalTime.getText() + "/" + bronzeMedalScore.getText() + ";";
                medals += "silver:" + silverMedalTime.getText() + "/" + silverMedalScore.getText() + ";";
                medals += "gold:" + goldMedalTime.getText() + "/" + goldMedalScore.getText() + ";";
                DbTableTests.setMedals(testName.getText(), medals);
            }

            //add media file name
            if (!mediaPath.getText().contentEquals("none")) {
                DbTableTests.setMediaFile(mediaPath.getText(), testName.getText());
            }

            Stage stage = (Stage) testName.getScene().getWindow();
            stage.close();
        } else {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("This test name already exists. Please choose an other name."));
            Scene dialogScene = new Scene(dialogVbox, 400, 40);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
