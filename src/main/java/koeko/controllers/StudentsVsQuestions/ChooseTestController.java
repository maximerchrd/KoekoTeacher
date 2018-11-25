package koeko.controllers.StudentsVsQuestions;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import koeko.database_management.DbTableRelationClassTest;
import koeko.database_management.DbTableTest;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ChooseTestController implements Initializable{

    @FXML ListView<Test> testsList;

    private ComboBox chooseTestComboBox;
    private String selectedClass;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Test> tests = DbTableTest.getAllTests();

        for (Test test : tests) {
            if (test.getTestMode().equals(QuestionGeneric.CERTIFICATIVE_TEST)) {
                testsList.getItems().add(test);
            }
        }

        testsList.setCellFactory(param -> new ListCell<Test>() {
            private ImageView imageView = new ImageView();

            public void updateItem(Test test, boolean empty) {
                super.updateItem(test, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(test.getTestName());
                    setGraphic(null);
                }
            }
        });
    }

    public void assignTest() {
        DbTableRelationClassTest.addClassTestRelation(selectedClass,testsList.getSelectionModel().getSelectedItem().getTestName());
        chooseTestComboBox.getItems().add(testsList.getSelectionModel().getSelectedItem().getTestName());
        Stage stage = (Stage) testsList.getScene().getWindow();
        stage.close();
    }

    public void initializeParameters(ComboBox chooseTestCombo, String selectedClass) {
        chooseTestComboBox = chooseTestCombo;
        this.selectedClass = selectedClass;
    }
}