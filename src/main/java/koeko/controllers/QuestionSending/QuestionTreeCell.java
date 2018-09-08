package koeko.controllers.QuestionSending;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import koeko.database_management.DbTableRelationQuestionQuestion;
import koeko.database_management.DbTableRelationQuestionTest;
import koeko.questions_management.QuestionGeneric;

public class QuestionTreeCell  extends TreeCell<QuestionGeneric> {
    private double imageSize = 60;
    private double buttonSize = 30;

    @Override
    protected void updateItem(QuestionGeneric item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            HBox hbox = new HBox(10);

            VBox buttonsBox = new VBox(3);

            Button buttonEdit = new Button("Ed");
            Button buttonDelete = new Button("X");
            buttonsBox.getChildren().addAll(buttonEdit, buttonDelete);

            VBox buttonsBox2 = new VBox(3);
            Button buttonQRCode = new Button("QR");
            buttonsBox2.getChildren().addAll(buttonQRCode);



            ImageView image = new ImageView();

            setText(item.getQuestion());
            if (Long.valueOf(item.getGlobalID()) > 0) {
                String url = "file:" + item.getImagePath();
                image = new ImageView(new Image(url, imageSize, imageSize, true, true));
            } else {
                if (item.getTypeOfQuestion().contentEquals("TEFO")) {
                   image = new ImageView(new Image("/drawable/test.png", imageSize, imageSize, true, true));
                } else if (item.getTypeOfQuestion().contentEquals("TECE")) {
                    image = new ImageView(new Image("/drawable/test_certificative.png", imageSize, imageSize, true, true));
                }
            }
            hbox.getChildren().addAll(buttonsBox, buttonsBox2, image);
            setGraphic(hbox);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
