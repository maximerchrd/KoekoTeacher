package koeko.controllers.StudentsVsQuestions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.controllers.SubjectsBrowsing.EditSubjectController;
import koeko.controllers.controllers_tools.SingleStudentAnswersLine;
import koeko.database_management.DbTableQuestionGeneric;
import koeko.database_management.DbTableRelationQuestionSubject;
import koeko.database_management.DbTableRelationSubjectSubject;
import koeko.database_management.DbTableSubject;
import koeko.students_management.Student;
import koeko.view.Subject;

import java.io.IOException;
import java.util.Vector;

public class StudentTableCell extends TableCell<SingleStudentAnswersLine, Student> {

    @Override
    protected void updateItem(Student item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            if (Koeko.studentsVsQuestionsTableControllerSingleton.studentsCheckBoxes) {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(item.getHomeworkChecked());
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        item.setHomeworkChecked(true);
                    } else {
                        item.setHomeworkChecked(false);
                    }
                });

                setGraphic(checkBox);
            }
            setText(item.getName());
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}