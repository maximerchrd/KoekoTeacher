package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.controllers.LeftBar.ClassesControlling.EditClassController;
import koeko.database_management.DbTableHomework;
import koeko.questions_management.QuestionGeneric;

import java.io.IOException;

public class HomeworkListCell extends ListCell<Homework> {

    @Override
    public void updateItem(Homework homework, boolean empty) {
        super.updateItem(homework, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
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

            setText(homework.getName());
            setGraphic(hBox);
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