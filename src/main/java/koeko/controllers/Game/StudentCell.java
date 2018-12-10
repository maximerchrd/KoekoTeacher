package koeko.controllers.Game;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import koeko.Koeko;
import koeko.students_management.Student;

public class StudentCell extends ListCell<StudentCellView> {
    private double imageSize = 60;
    private double buttonSize = 30;

    @Override
    protected void updateItem(StudentCellView item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            HBox hbox = new HBox(10);

            double buttonImageSize = 20;

            ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonDelete = new Button();
            buttonDelete.setGraphic(deleteImage);
            buttonDelete.setTooltip(
                    new Tooltip("Delete question")
            );
            buttonDelete.setOnAction((event) -> {
                deleteItem(item);
            });

            Label studentLabel = new Label(item.getStudent().getName());

            Label scoreLabel = new Label(String.valueOf(item.getScore()));

            hbox.getChildren().addAll(buttonDelete, studentLabel, scoreLabel);

            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void deleteItem(StudentCellView item) {
        Koeko.activeGames.remove(item.getStudent());
        this.getListView().getItems().remove(this.getIndex());
    }
}
