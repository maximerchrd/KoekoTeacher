package koeko.controllers;

import javafx.scene.control.*;
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

import java.net.URL;
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

    public void initParameters(TreeItem<QuestionGeneric> root, ArrayList<String> testNames) {
        this.root = root;
        this.testNames = testNames;
        hBoxArrayList = new ArrayList<>();
        objectivesComboBoxArrayList = new ArrayList<>();
        testMode = 1;
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

    public void saveTest() {
        if (!testNames.contains(testName.getText())) {
            Test newTest = new Test();
            newTest.setTestName(testName.getText());
            if (certificativeCheckBox.isSelected()) {
                newTest.setTestMode(0);
            } else {
                newTest.setTestMode(1);
            }
            Integer testID = DbTableTests.addTest(newTest);
            TreeItem<QuestionGeneric> testTreeItem = new TreeItem<>();
            QuestionGeneric test = new QuestionGeneric();
            test.setGlobalID("-" + testID);
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
