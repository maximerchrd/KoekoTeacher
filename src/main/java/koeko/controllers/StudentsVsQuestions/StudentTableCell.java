package koeko.controllers.StudentsVsQuestions;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import koeko.Koeko;
import koeko.controllers.controllers_tools.SingleStudentAnswersLine;
import koeko.students_management.Student;

public class StudentTableCell extends TableCell<SingleStudentAnswersLine, Student> {
    int buttonImageSize = 15;
    @Override
    protected void updateItem(Student item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            HBox hBox = new HBox(10);
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

                hBox.getChildren().add(checkBox);
            }
            if (!item.getStudentID().contentEquals("-1")) {
                Button deleteButton = new Button();
                deleteButton.setPrefSize(15, 15);
                ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, false));
                deleteButton.setGraphic(deleteImage);
                deleteButton.setOnAction((event) -> deleteItem(item));
                hBox.getChildren().add(deleteButton);
            }
            setGraphic(hBox);
            setText(item.getName());
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void deleteItem(Student item) {
        Koeko.leftBarController.removeStudentFromClass(item.getName(), this.getIndex());
    }
}