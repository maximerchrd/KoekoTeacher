package koeko.controllers.TestControlling;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import koeko.Tools.FilesHandler;
import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableRelationObjectiveTest;
import koeko.database_management.DbTableTest;
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
    }

    public void addObjective() {
        TestEditing.addObjectiveField(objectivesComboBoxArrayList, vBoxObjectives);
    }

    public void certificativeCheckBoxAction() {
        TestEditing.certificativeCheckAction(certificativeCheckBox, addObjectiveButton, objectivesComboBoxArrayList);
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
            if (certificativeCheckBox.isSelected()) {
                newTest.setTestMode(0);
            } else {
                newTest.setTestMode(1);
            }
            String testID = DbTableTest.addTest(newTest);
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
