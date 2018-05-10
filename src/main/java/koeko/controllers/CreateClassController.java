package koeko.controllers;

import koeko.database_management.DbTableClasses;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 14.03.18.
 */
public class CreateClassController extends Window implements Initializable {
    ComboBox chooseClassComboBox;

    @FXML private TextField className;
    @FXML private TextField classLevel;
    @FXML private TextField classYear;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initializeParameters(ComboBox chooseClassComboBox) {
        this.chooseClassComboBox = chooseClassComboBox;
    }

    public void saveClass() {
        DbTableClasses.addClass(className.getText(), classLevel.getText(), classYear.getText());
        chooseClassComboBox.getItems().add(className.getText());
        Stage stage = (Stage) className.getScene().getWindow();
        stage.close();
    }
}
