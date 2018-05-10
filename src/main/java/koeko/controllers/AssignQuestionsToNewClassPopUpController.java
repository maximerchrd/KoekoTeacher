package koeko.controllers;

import koeko.Koeko;
import koeko.database_management.DbTableRelationClassQuestion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AssignQuestionsToNewClassPopUpController extends Window implements Initializable {
    private String className;
    @FXML private Label label;

    public void initParameters(String className) {
        this.className = className;
    }

    public void assignQuestions() {
        ArrayList<Integer>  questionIds = DbTableRelationClassQuestion.getQuestionsIDsForClass(className);
        for (Integer id : questionIds) {
            if (!Koeko.studentGroupsAndClass.get(0).getActiveIDs().contains(id)) {
                Koeko.studentGroupsAndClass.get(0).getActiveIDs().add(id);
                if (!Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(id)) {
                    Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(id);
                }
            }
        }
        for (Integer id : Koeko.studentGroupsAndClass.get(0).getActiveIDs()) {
            DbTableRelationClassQuestion.addClassQuestionRelation(className, String.valueOf(id));
        }
        Koeko.questionSendingControllerSingleton.refreshReadyQuestionsList();
        Stage stage = (Stage) label.getScene().getWindow();
        stage.close();
    }

    public void doNotAssign() {
        Koeko.studentGroupsAndClass.get(0).getActiveIDs().clear();
        ArrayList<Integer>  questionIds = DbTableRelationClassQuestion.getQuestionsIDsForClass(className);
        for (Integer id : questionIds) {
            Koeko.studentGroupsAndClass.get(0).getActiveIDs().add(id);
            if (!Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(id)) {
                Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(id);
            }
        }
        Koeko.questionSendingControllerSingleton.refreshReadyQuestionsList();
        Stage stage = (Stage) label.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
