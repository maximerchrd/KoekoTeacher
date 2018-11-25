package koeko.controllers.TestControlling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableRelationObjectiveTest;
import koeko.database_management.DbTableTest;
import koeko.questions_management.QuestionGeneric;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.questions_management.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CreateTestController extends Window implements Initializable {
    private TreeItem<QuestionGeneric> root;
    private ArrayList<String> testNames;
    private ArrayList<HBox> hBoxArrayList;
    private ArrayList<ComboBox> objectivesComboBoxArrayList;
    private Integer testMode;

    @FXML private TextField testName;
    @FXML private VBox vBoxObjectives;
    @FXML private ComboBox testTypeCombobox;
    @FXML private Button addObjectiveButton;
    @FXML private TextField goldMedalTime;
    @FXML private TextField goldMedalScore;
    @FXML private TextField silverMedalTime;
    @FXML private TextField silverMedalScore;
    @FXML private TextField bronzeMedalTime;
    @FXML private TextField bronzeMedalScore;
    @FXML private CheckBox medalsCheckbox;
    @FXML private TextField mediaPath;
    @FXML private CheckBox sendMedia;

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
        ObservableList<String> testTypes =
                FXCollections.observableArrayList(TestEditing.testTypes);
        testTypeCombobox.setItems(testTypes);
        testTypeCombobox.getSelectionModel().select(TestEditing.formativeTest);
    }

    public void addObjective() {
        TestEditing.addObjectiveField(objectivesComboBoxArrayList, vBoxObjectives);
    }

    public void changedTestType() {
        TestEditing.testTypeChanged(testTypeCombobox, addObjectiveButton, objectivesComboBoxArrayList);
    }

    public void togglingMedals() {
        TestEditing.toggleMedals(medalsCheckbox, goldMedalTime, goldMedalScore, silverMedalTime, silverMedalScore,
                bronzeMedalTime, bronzeMedalScore);
    }

    public void addMediaFile() {
        TestEditing.ShowMediaFileChoser(mediaPath);
    }

    public void saveTest() {
        if (!testNames.contains(testName.getText())) {
            Test newTest = new Test();
            newTest.setTestName(testName.getText());
            if (testTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals(TestEditing.certificativeTest)) {
                newTest.setTestMode(QuestionGeneric.CERTIFICATIVE_TEST);
            } else if (testTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals(TestEditing.formativeTest)) {
                newTest.setTestMode(QuestionGeneric.FORMATIVE_TEST);
            } else if (testTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals(TestEditing.game)) {
                newTest.setTestMode(QuestionGeneric.GAME);
            }  else if (testTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals(TestEditing.questionSet)) {
                newTest.setTestMode(QuestionGeneric.GAME_QUESTIONSET);
            }
            String testID = DbTableTest.addTest(newTest);
            TreeItem<QuestionGeneric> testTreeItem = new TreeItem<>();
            QuestionGeneric test = new QuestionGeneric();
            test.setGlobalID(QuestionGeneric.changeIdSign(testID));
            test.setQuestion(testName.getText());

            //set the type of resource (formative/certificative test)
            test.setIntTypeOfQuestion(newTest.getTestMode());

            testTreeItem.setValue(test);
            root.getChildren().add(testTreeItem);

            //add objectives to test
            if (testTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals(TestEditing.certificativeTest)) {
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
