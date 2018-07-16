package koeko.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

public class GenericPopUpController extends Window {
    @FXML
    private Label messageLabel;

    public void initParameters(String message) {
        messageLabel.setText(message);
    }


    public void closePopUp() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

}
