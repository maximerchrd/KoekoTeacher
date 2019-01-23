package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.database_management.DbTableHomework;
import koeko.database_management.DbTableRelationHomeworkStudent;
import koeko.view.Homework;

import java.io.IOException;
import java.util.ArrayList;

public class HomeworkListCell extends ListCell<Homework> {

    @Override
    public void updateItem(Homework homework, boolean empty) {
        super.updateItem(homework, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
            VBox vBox = new VBox();
            HBox hBox = new HBox();
            hBox.setSpacing(5);
            Button buttonDelete = new javafx.scene.control.Button("X");
            buttonDelete.setTooltip(
                    new Tooltip("Delete Homework")
            );
            buttonDelete.setOnAction((event) -> {
                deleteHomework(this);
            });
            double buttonImageSize = 20;
            ImageView editImage = new ImageView(new Image("/drawable/editImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonEdit = new Button();
            buttonEdit.setGraphic(editImage);
            buttonEdit.setTooltip(
                    new Tooltip("Edit Homework")
            );
            buttonEdit.setOnAction((event) -> {
                editHomework(this);
            });
            hBox.getChildren().addAll(buttonDelete, buttonEdit);

            HBox hBox2 = new HBox();
            CheckBox addStudentCheckBox = new CheckBox("Add Students");
            addStudentCheckBox.setTooltip(
                    new Tooltip("Add students to this homework")
            );
            addStudentCheckBox.setOnAction((event) -> {
                toggleAddingStudents(homework.getName());
            });

            hBox2.getChildren().addAll(addStudentCheckBox);

            vBox.getChildren().addAll(hBox, hBox2);
            setText(homework.getName());
            setGraphic(vBox);
        }
    }

    private void toggleAddingStudents(String homeworkName) {
        if (!Koeko.studentsVsQuestionsTableControllerSingleton.studentsCheckBoxes) {
            ArrayList<String> studentIds = DbTableRelationHomeworkStudent.getStudentIdsFromHomeworkName(homeworkName);
            for (int i = 0; i < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size() - 1; i++) {
                if (studentIds.contains(Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudentObject().getStudentID())) {
                    Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudentObject().setHomeworkChecked(true);
                } else {
                    Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudentObject().setHomeworkChecked(false);
                }
            }
            Koeko.studentsVsQuestionsTableControllerSingleton.studentsCheckBoxes = true;
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).refresh();
        } else {
            for (int i = 0; i < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size() - 1; i++) {
                if (Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudentObject().getHomeworkChecked()) {
                    DbTableRelationHomeworkStudent.insertHomeworkStudentRelation(homeworkName, Koeko.studentsVsQuestionsTableControllerSingleton.
                            tableViewArrayList.get(0).getItems().get(i).getStudentObject().getStudentID());
                } else {
                    DbTableRelationHomeworkStudent.deleteHomeworkStudentRelation(homeworkName, Koeko.studentsVsQuestionsTableControllerSingleton.
                            tableViewArrayList.get(0).getItems().get(i).getStudentObject().getStudentID());
                }
            }
            Koeko.studentsVsQuestionsTableControllerSingleton.studentsCheckBoxes = false;
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).refresh();
        }
    }

    private void editHomework(HomeworkListCell homeworkListCell) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditHomework.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditHomeworkController controller = fxmlLoader.getController();
        controller.initParams(homeworkListCell.getItem().getName(), homeworkListCell.getItem().getDueDate(), homeworkListCell);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Edit Homework");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    private void deleteHomework(HomeworkListCell homeworkListCell) {
        DbTableHomework.deleteHomework(homeworkListCell.getItem().getName());
        homeworkListCell.getListView().getItems().remove(homeworkListCell.getItem());
    }
}