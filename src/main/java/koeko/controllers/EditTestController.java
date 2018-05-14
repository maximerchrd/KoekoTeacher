package koeko.controllers;

import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableRelationObjectiveTest;
import koeko.database_management.DbTableTests;
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

    @FXML private TextField testName;
    @FXML private VBox vBoxObjectives;
    @FXML private CheckBox certificativeCheckBox;
    @FXML private Button addObjectiveButton;

    public void initParameters(TreeView treeView, ArrayList<String> testNames, String testID, ArrayList<String> objectives) {
        this.treeView = treeView;
        this.testNames = testNames;
        hBoxArrayList = new ArrayList<>();
        objectivesComboBoxArrayList = new ArrayList<>();
        this.objectives = objectives;
        for (String obj : objectives) {
            addObjective(obj);
        }
        test = DbTableTests.getTestWithID(Integer.valueOf(testID));
        this.presentName = test.getTestName();
        if (test.getTestMode() == 0) {
            certificativeCheckBox.setSelected(true);
            addObjectiveButton.setDisable(true);
            for (ComboBox comboBox : objectivesComboBoxArrayList) {
                comboBox.setDisable(true);
            }
        }
        testName.setText(presentName);
    }

    public void addObjective() {
        addObjective("");
    }
    public void addObjective(String objective) {
        Vector<String> objectivessVector = DbTableLearningObjectives.getAllObjectives();
        String[] objectivesVector = objectivessVector.toArray(new String[objectivessVector.size()]);
        ObservableList<String> options =
                FXCollections.observableArrayList(objectivesVector);
        ComboBox comboBox = new ComboBox(options);
        comboBox.setEditable(true);
        if (objective.length() > 0) {
            comboBox.getEditor().setText(objective);
        }
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
        if (certificativeCheckBox.isSelected()) {
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
        if (certificativeCheckBox.isSelected()) {
            DbTableTests.changeTestMode(test.getIdTest(),0);
        } else {
            DbTableTests.changeTestMode(test.getIdTest(),1);
        }
        if (!testNames.contains(testName.getText()) || testName.getText().contentEquals(presentName)) {
            TreeItem<QuestionGeneric> testTreeItem = (TreeItem<QuestionGeneric>) treeView.getSelectionModel().getSelectedItem();
            testTreeItem.getValue().setQuestion(testName.getText());

            //set the type of resource (formative/certificative test)
            if (certificativeCheckBox.isSelected()) {
                testTreeItem.getValue().setTypeOfQuestion("TECE");
            } else {
                testTreeItem.getValue().setTypeOfQuestion("TEFO");
            }

            treeView.refresh();
            DbTableTests.renameTest(-testTreeItem.getValue().getGlobalID(),testName.getText());

            //add objectives to test
            ArrayList<String> newObjectives = new ArrayList<>();
            if (!certificativeCheckBox.isSelected()) {
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
