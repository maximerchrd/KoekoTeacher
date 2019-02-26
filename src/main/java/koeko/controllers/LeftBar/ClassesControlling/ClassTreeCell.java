package koeko.controllers.LeftBar.ClassesControlling;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.controllers.CreateClassController;
import koeko.controllers.controllers_tools.ControllerUtils;
import koeko.database_management.*;
import koeko.students_management.Classroom;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class ClassTreeCell extends TreeCell<Classroom> {
    ResourceBundle bundle;

    public ClassTreeCell(ResourceBundle bundle) {
        this.bundle = bundle;
    }

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
                        new Tooltip(bundle.getString("string.edit_class_group"))
                );
                buttonEdit.setOnAction((event) -> {
                    editItem(this);
                });

                ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, true));
                Button buttonDelete = new Button();
                buttonDelete.setGraphic(deleteImage);
                buttonDelete.setTooltip(
                        new Tooltip(bundle.getString("string.delete_class_group"))
                );
                buttonDelete.setOnAction((event) -> {
                    deleteItem(item, this);
                });
                buttonsBox.getChildren().addAll(buttonEdit, buttonDelete);
            }

            VBox buttonsBox2 = new VBox(3);

//            ImageView qrImage = new ImageView(new Image("/drawable/qrImage.png", buttonImageSize, buttonImageSize, true, true));
//            Button buttonAddSubject = new Button();
//            buttonAddSubject.setGraphic(qrImage);
//            buttonAddSubject.setTooltip(
//                    new Tooltip("Filter questions according to subject")
//            );
//            buttonAddSubject.setOnAction((event) -> {
//
//            });

            ImageView linkImage = new ImageView(new Image("/drawable/linkImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonCreateSubject = new Button();
            buttonCreateSubject.setGraphic(linkImage);
            buttonCreateSubject.setTooltip(
                    new Tooltip(bundle.getString("string.add_class_group"))
            );
            buttonCreateSubject.setOnAction((event) -> {
                createClass(this);
            });

            buttonsBox2.getChildren().addAll(buttonCreateSubject);

            ImageView image = new ImageView();

            setText(item.getClassName());
            hbox.getChildren().addAll(buttonsBox, buttonsBox2, image);
            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void editItem(ClassTreeCell treeItem) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditClass.fxml"));
        Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
        EditClassController controller = fxmlLoader.<EditClassController>getController();
        controller.initializeParameters(treeItem);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(bundle.getString("editclass.edit"));
        stage.setScene(new Scene(root1));
        stage.show();
    }

    private void deleteItem(Classroom classroom, ClassTreeCell treeCell) {
        if (treeCell.getTreeItem().getChildren().size() == 0) {
            DbTableClasses.deleteGroup(classroom.getClassName());
            treeCell.getTreeItem().getParent().getChildren().remove(treeCell.getTreeItem());
        } else {
            Koeko.leftBarController.promptGenericPopUp(bundle.getString("string.impossible_delete_class"), bundle.getString("string.illegal_operation"));
        }
    }

    public void createClass(ClassTreeCell classroomClassTreeCell) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateClass.fxml"));
        Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
        CreateClassController controller = fxmlLoader.<CreateClassController>getController();
        controller.initializeParameters(classroomClassTreeCell);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(bundle.getString("createclass.create"));
        stage.setScene(new Scene(root1));
        stage.show();
    }
}