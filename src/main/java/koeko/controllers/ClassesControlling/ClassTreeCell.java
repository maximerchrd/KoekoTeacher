package koeko.controllers.ClassesControlling;

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
import koeko.controllers.CreateClassController;
import koeko.controllers.QuestionSendingController;
import koeko.controllers.SubjectsBrowsing.CreateSubjectController;
import koeko.controllers.SubjectsBrowsing.EditSubjectController;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.students_management.Classroom;
import koeko.view.Subject;

import java.io.IOException;
import java.util.Vector;

public class ClassTreeCell extends TreeCell<Classroom> {
    private double imageSize = 60;
    private double buttonSize = 30;

    @Override
    protected void updateItem(Classroom item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            HBox hbox = new HBox(6);

            VBox buttonsBox = new VBox(3);

            double buttonImageSize = 20;

            if (this.getTreeItem().getParent() != null) {
                ImageView editImage = new ImageView(new Image("/drawable/editImage.png", buttonImageSize, buttonImageSize, true, true));
                Button buttonEdit = new Button();
                buttonEdit.setGraphic(editImage);
                buttonEdit.setTooltip(
                        new Tooltip("Edit class / group")
                );
                buttonEdit.setOnAction((event) -> {
                    editItem(item);
                });

                ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, true));
                Button buttonDelete = new Button();
                buttonDelete.setGraphic(deleteImage);
                buttonDelete.setTooltip(
                        new Tooltip("Delete class / group")
                );
                buttonDelete.setOnAction((event) -> {
                    deleteItem(item, this);
                });
                buttonsBox.getChildren().addAll(buttonEdit, buttonDelete);
            }

            VBox buttonsBox2 = new VBox(3);

            ImageView qrImage = new ImageView(new Image("/drawable/qrImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonAddSubject = new Button();
            buttonAddSubject.setGraphic(qrImage);
            buttonAddSubject.setTooltip(
                    new Tooltip("Filter questions according to subject")
            );
            buttonAddSubject.setOnAction((event) -> {

            });

            ImageView linkImage = new ImageView(new Image("/drawable/linkImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonCreateSubject = new Button();
            buttonCreateSubject.setGraphic(linkImage);
            buttonCreateSubject.setTooltip(
                    new Tooltip("Add class / group")
            );
            buttonCreateSubject.setOnAction((event) -> {
                createClass(this);
            });

            buttonsBox2.getChildren().addAll(buttonAddSubject, buttonCreateSubject);

            ImageView image = new ImageView();

            setText(item.getClassName());
            hbox.getChildren().addAll(buttonsBox, buttonsBox2, image);
            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void editItem(Classroom item) {

    }

    private void deleteItem(Classroom classroom, ClassTreeCell treeCell) {
        if (treeCell.getTreeItem().getChildren().size() == 0) {
            DbTableClasses.deleteGroup(classroom.getClassName());
            treeCell.getTreeItem().getParent().getChildren().remove(treeCell.getTreeItem());
        } else {
            Koeko.leftBarController.promptGenericPopUp("You can't delete a class with sub-groups.", "Illegal operation");
        }
    }

    public void createClass(ClassTreeCell classroomClassTreeCell) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateClass.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateClassController controller = fxmlLoader.<CreateClassController>getController();
        controller.initializeParameters(classroomClassTreeCell);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Class");
        stage.setScene(new Scene(root1));
        stage.show();
    }
}