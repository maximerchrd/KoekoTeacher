package koeko.controllers.TestControlling;

import javafx.stage.FileChooser;
import koeko.ResultsManagement.MedalsInstructions;
import koeko.Tools.FilesHandler;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.questions_management.Test;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

public class EditTestController extends Window implements Initializable {
    private TreeView treeView;
    private ArrayList<String> testNames;
    private ArrayList<HBox> hBoxArrayList;
    private ArrayList<ComboBox> objectivesComboBoxArrayList;
    private String presentName;
    private ArrayList<String> objectives;
    private Test test;
    private QuestionGeneric questionGeneric;
    private TreeItem treeItem;

    private String originalMediaFile = "";

    @FXML private TextField testName;
    @FXML private VBox vBoxObjectives;
    @FXML private CheckBox certificativeCheckBox;
    @FXML private Button addObjectiveButton;
    @FXML private Label warningLabel;
    @FXML private TextField goldMedalTime;
    @FXML private TextField goldMedalScore;
    @FXML private TextField silverMedalTime;
    @FXML private TextField silverMedalScore;
    @FXML private TextField bronzeMedalTime;
    @FXML private TextField bronzeMedalScore;
    @FXML private CheckBox medalsCheckbox;
    @FXML private TextField mediaPath;
    @FXML private CheckBox sendMedia;

    public void initParameters(TreeView treeView, ArrayList<String> testNames, String testID, ArrayList<String> objectives,
                               QuestionGeneric questionGeneric, TreeItem treeItem) {
        this.treeView = treeView;
        this.questionGeneric = questionGeneric;
        this.treeItem = treeItem;
        this.testNames = testNames;
        hBoxArrayList = new ArrayList<>();
        objectivesComboBoxArrayList = new ArrayList<>();
        this.objectives = objectives;
        for (String obj : objectives) {
            addObjective(obj);
        }
        test = DbTableTest.getTestWithID(testID);
        test.setMedalsInstructions(DbTableTest.getMedals(test.getTestName()));
        this.presentName = test.getTestName();
        if (test.getTestMode() == 0) {
            certificativeCheckBox.setSelected(true);
            addObjectiveButton.setDisable(false);
            for (ComboBox comboBox : objectivesComboBoxArrayList) {
                comboBox.setDisable(false);
            }
        } else {
            addObjectiveButton.setDisable(true);
        }
        testName.setText(presentName);

        ArrayList<String> questionsOfTest = DbTableRelationQuestionQuestion.getFirstLayerQuestionIdsFromTestName(test.getTestName());
        if (questionsOfTest.size() == 0) {
            warningLabel.setText("");
        } else {
            warningLabel.setText("You cannot change the type of a formative test \nwhich contains questions");
            addObjectiveButton.setDisable(true);
            certificativeCheckBox.setDisable(true);
        }

        //set media file
        if (test.getMediaFileName() != null) {
            mediaPath.setText(test.getMediaFileName());
            originalMediaFile = test.getMediaFileName();
            if (test.getSendMediaFile() == 1) {
                sendMedia.setSelected(true);
            }
        }

        //set medals
        if (test.getMedalsInstructions() != null && test.getMedalsInstructions().length() > 0) {
            medalsCheckbox.setSelected(true);
            MedalsInstructions medalsInstructions = new MedalsInstructions();
            medalsInstructions.parseInstructions(test.getMedalsInstructions());
            bronzeMedalTime.setText(String.valueOf(medalsInstructions.getBronzeTime()));
            bronzeMedalScore.setText(String.valueOf(medalsInstructions.getBronzeScore()));
            silverMedalTime.setText(String.valueOf(medalsInstructions.getSilverTime()));
            silverMedalScore.setText(String.valueOf(medalsInstructions.getSilverScore()));
            goldMedalTime.setText(String.valueOf(medalsInstructions.getGoldTime()));
            goldMedalScore.setText(String.valueOf(medalsInstructions.getGoldScore()));
        }
    }

    public void addObjective() {
        addObjective("");
    }
    public void addObjective(String objective) {
        ComboBox comboBox = TestEditing.addObjectiveField(objectivesComboBoxArrayList, vBoxObjectives);
        comboBox.getSelectionModel().select(objective);
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

    public void addMediaFile() {
        TestEditing.ShowMediaFileChoser(mediaPath);
    }

    public void togglingMedals() {
        TestEditing.toggleMedals(medalsCheckbox, goldMedalTime, goldMedalScore, silverMedalTime, silverMedalScore,
                bronzeMedalTime, bronzeMedalScore);
    }

    public void saveTest() {
        if (certificativeCheckBox.isSelected()) {
            DbTableTest.changeTestMode(test.getIdTest(),0);
        } else {
            DbTableTest.changeTestMode(test.getIdTest(),1);
        }
        if (!testNames.contains(testName.getText()) || testName.getText().contentEquals(presentName)) {
            questionGeneric.setQuestion(testName.getText());

            //set the type of resource (formative/certificative test)
            if (certificativeCheckBox.isSelected()) {
                questionGeneric.setTypeOfQuestion("TECE");
            } else {
                questionGeneric.setTypeOfQuestion("TEFO");
            }
            treeItem.setValue(questionGeneric);

            treeView.refresh();
            DbTableTest.renameTest(QuestionGeneric.changeIdSign(questionGeneric.getGlobalID()),testName.getText());


            //add objectives to test
            ArrayList<String> newObjectives = new ArrayList<>();
            if (certificativeCheckBox.isSelected()) {
                for (ComboBox objectiveCombo : objectivesComboBoxArrayList) {
                    try {
                        newObjectives.add(objectiveCombo.getEditor().getText());
                        DbTableLearningObjectives.addObjective(objectiveCombo.getEditor().getText(), -1);
                        DbTableRelationObjectiveTest.addRelationObjectiveTest(objectiveCombo.getEditor().getText(), testName.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (String obj : objectives) {
                if (!newObjectives.contains(obj)) {
                    DbTableRelationObjectiveTest.removeRelationObjectiveTest(obj, testName.getText());
                }
            }

            //add medals
            DbTableTest.setMedals(testName.getText(), "");
            TestEditing.addMedalsToTest(medalsCheckbox, bronzeMedalTime, bronzeMedalScore, silverMedalTime, silverMedalScore,
                    goldMedalTime, goldMedalScore, testName);

            //add media file
            TestEditing.addMediaFile(mediaPath, sendMedia, testName);

            Stage stage = (Stage) testName.getScene().getWindow();
            stage.close();
        } else {
            TestEditing.nameCollisionWarning(this);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
