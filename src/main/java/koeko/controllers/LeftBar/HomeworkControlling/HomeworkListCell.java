package koeko.controllers.LeftBar.HomeworkControlling;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import koeko.questions_management.QuestionGeneric;

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
            javafx.scene.control.Button buttonDelete = new javafx.scene.control.Button("X");
            buttonDelete.setTooltip(
                    new Tooltip("Delete Homework")
            );
            buttonDelete.setOnAction((event) -> {

            });

            setText(homework.getName());
            setGraphic(hBox);
        }
    }
}
