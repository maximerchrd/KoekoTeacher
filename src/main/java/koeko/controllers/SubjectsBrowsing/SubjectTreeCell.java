package koeko.controllers.SubjectsBrowsing;

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
import koeko.controllers.controllers_tools.ControllerUtils;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.view.Subject;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

public class SubjectTreeCell extends TreeCell<Subject> {
    private double imageSize = 60;
    private double buttonSize = 30;

    @Override
    protected void updateItem(Subject item, boolean empty) {
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
                        new Tooltip("Edit subject")
                );
                buttonEdit.setOnAction((event) -> {
                    editItem(item);
                });

                ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, true));
                Button buttonDelete = new Button();
                buttonDelete.setGraphic(deleteImage);
                buttonDelete.setTooltip(
                        new Tooltip("Delete subject")
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
                filterQuestionsWithSubject(item);
            });

            ImageView linkImage = new ImageView(new Image("/drawable/linkImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonCreateSubject = new Button();
            buttonCreateSubject.setGraphic(linkImage);
            buttonCreateSubject.setTooltip(
                    new Tooltip("Add child subject")
            );
            buttonCreateSubject.setOnAction((event) -> {
                createSubject();
            });

            buttonsBox2.getChildren().addAll(buttonAddSubject, buttonCreateSubject);

            ImageView image = new ImageView();

            setText(item.get_subjectName());
            hbox.getChildren().addAll(buttonsBox, buttonsBox2, image);
            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void editItem(Subject item) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSubject.fxml"));
        Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
        EditSubjectController controller = fxmlLoader.getController();
        controller.initializeSubject(item.get_subjectName(), Koeko.leftBarController.subjectsTree);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Edit the Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    private void deleteItem(Subject subject, SubjectTreeCell treeCell) {
        if (treeCell.getTreeItem().getChildren().size() == 0) {
            DbTableRelationSubjectSubject.deleteRelationWhereSubjectIsChild(subject.get_subjectName());
            DbTableRelationQuestionSubject.removeRelationWithSubject(subject.get_subjectName());
            DbTableSubject.deleteSubject(subject.get_subjectName());
            this.getTreeItem().getParent().getChildren().remove(this.getTreeItem());
        } else {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Koeko.leftBarController);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("Sorry, it is not possible to delete a subject with sub-subject(s)."));
            Scene dialogScene = new Scene(dialogVbox, 400, 40);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }

    public void filterQuestionsWithSubject(Subject subject) {
        Vector<String> questionIds;
        if (subject.get_subjectName().contentEquals("All subjects")) {
            questionIds = DbTableQuestionGeneric.getAllGenericQuestionsIds();
        } else {
            questionIds = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subject.get_subjectName());
        }
        Koeko.questionSendingControllerSingleton.populateTree(questionIds);
    }

    public void createSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateSubject.fxml"));
        Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }
}