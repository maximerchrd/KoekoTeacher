package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.Koeko;
import koeko.controllers.LeftBar.LeftBarController;
import koeko.database_management.DbTableHomework;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateHomeworkController extends Window implements Initializable {

    @FXML
    private TextField homeworkName;
    @FXML private DatePicker datePicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void saveHomework() {
        if (homeworkName.getCharacters().length() > 0 && datePicker.getEditor().getCharacters().length() > 0) {
            if (!DbTableHomework.checkIfNameAlreadyExists(homeworkName.getText())) {
                Homework homework = new Homework();
                homework.setName(homeworkName.getText());
                homework.setDueDate(datePicker.getValue());
                DbTableHomework.insertHomework(homework);
                Koeko.leftBarController.homeworksList.getItems().add(homework);
                Koeko.leftBarController.homeworksList.getSelectionModel().select(homework);
                Stage stage = (Stage) homeworkName.getScene().getWindow();
                stage.close();
            } else {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(this);
                VBox dialogVbox = new VBox(20);
                dialogVbox.getChildren().add(new Text("This homework name already exists. Please choose an other name."));
                Scene dialogScene = new Scene(dialogVbox, 400, 40);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        } else {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("You must specify both due date and homework name"));
            Scene dialogScene = new Scene(dialogVbox, 400, 40);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }
}
