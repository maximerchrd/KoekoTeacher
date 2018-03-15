package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.database_management.DbTableTests;
import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;
import com.wideworld.learningtrackerteacher.questions_management.Test;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CreateTestController  extends Window implements Initializable {
    private TreeItem<QuestionGeneric> root;
    private ArrayList<String> testNames;

    @FXML private TextField testName;

    public void initParameters(TreeItem<QuestionGeneric> root, ArrayList<String> testNames) {
        this.root = root;
        this.testNames = testNames;
    }

    public void saveTest() {
        if (!testNames.contains(testName.getText())) {
            DbTableTests.addTest(testName.getText());
            TreeItem<QuestionGeneric> testTreeItem = new TreeItem<>();
            QuestionGeneric test = new QuestionGeneric();
            test.setGlobalID(-10);
            test.setQuestion(testName.getText());
            testTreeItem.setValue(test);
            root.getChildren().add(testTreeItem);
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
