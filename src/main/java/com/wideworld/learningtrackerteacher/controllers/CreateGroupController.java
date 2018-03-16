package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.database_management.DbTableClasses;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateGroupController extends Window implements Initializable {
    private String className;
    @FXML private TextField groupName;

    public void initParameters(String className) {
        this.className = className;
    }

    public void saveGroup() {
        DbTableClasses.addGroupToClass(groupName.getText(), className);;
        Stage stage = (Stage) groupName.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
