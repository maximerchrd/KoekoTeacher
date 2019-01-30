package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.database_management.DbTableHomework;
import koeko.database_management.DbTableSettings;
import koeko.view.Homework;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EditHomeworkController extends Window implements Initializable {

    @FXML
    private TextField homeworkName;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox homeworkKeyCombobox;

    private String oldName = "";
    private HomeworkListCell homeworkListCell;
    private Homework homework;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initParams(String pname, LocalDate pDate, HomeworkListCell phomeworkListCell) {
        homeworkName.setText(pname);
        datePicker.setValue(pDate);
        oldName = pname;
        homeworkListCell = phomeworkListCell;
        homework = DbTableHomework.getHomeworkWithName(pname);
        homeworkKeyCombobox.getItems().addAll(DbTableSettings.getHomeworkKeys());
        if (homework.getIdCode() != null && homework.getIdCode().length() > 0) {
            homeworkKeyCombobox.getSelectionModel().select(homework.getIdCode());
        }
    }

    public void saveHomework() {
        if (oldName.contentEquals(homeworkName.getText()) || !DbTableHomework.checkIfNameAlreadyExists(homeworkName.getText())) {
            homeworkListCell.getItem().setName(homeworkName.getText());
            homeworkListCell.getItem().setDueDate(datePicker.getValue().toString());
            homeworkListCell.getItem().setIdCode(homeworkKeyCombobox.getSelectionModel().getSelectedItem().toString());
            homeworkListCell.updateItem(homeworkListCell.getItem(), false);
            DbTableHomework.updateHomework(homeworkListCell.getItem(), oldName);
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
    }
}
