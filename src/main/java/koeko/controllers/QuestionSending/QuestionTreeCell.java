package koeko.controllers.QuestionSending;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.view.Utilities;
import net.glxn.qrgen.javase.QRCode;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
            buttonQRCode.setOnAction((event) -> {
                String identifier = "0";
                if (item.getIntTypeOfQuestion() == 0) {
                    identifier = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(item.getGlobalID()).getQCM_MUID();
                } else if (item.getIntTypeOfQuestion() == 1) {
                    identifier = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(item.getGlobalID()).getUID();
                } else if (item.getIntTypeOfQuestion() == 2) {
                    identifier = DbTableTests.getTestWithID(item.getGlobalID()).getIdTest();
                }
                Stage stage = (Stage) buttonDelete.getScene().getWindow();
                System.out.println(item.getGlobalID());
                File file = QRCode.from(item.getGlobalID() + ":" + identifier + ":" + DbTableSettings.getCorrectionMode() + ":").file();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save file");
                File dest = fileChooser.showSaveDialog(stage);
                if (dest != null) {
                    try {
                        Files.move(file.toPath(), dest.toPath(), REPLACE_EXISTING);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
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
