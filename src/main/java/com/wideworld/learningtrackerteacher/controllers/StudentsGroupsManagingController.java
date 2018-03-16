package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.database_management.DbTableClasses;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class StudentsGroupsManagingController implements Initializable {
    @FXML private TableView groupsTable;
    @FXML private ComboBox chooseClass;

    public void createGroup() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateGroup.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateGroupController controller = fxmlLoader.<CreateGroupController>getController();
        controller.initParameters(chooseClass.getSelectionModel().getSelectedItem().toString());
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Question");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void removeGroup() {
        TablePosition tablePosition = groupsTable.getFocusModel().getFocusedCell();
        groupsTable.getColumns().remove(tablePosition.getColumn());
    }

    public void addGroups() {

    }

    public void initialize(URL location, ResourceBundle resources) {
    }
}
