package koeko.controllers.QuestionSending;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.controllers.EditQuestionController;
import koeko.controllers.EditTestController;
import koeko.controllers.controllers_tools.Toast;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.Test;
import koeko.view.Utilities;
import net.glxn.qrgen.javase.QRCode;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class QuestionTreeCell  extends TreeCell<QuestionGeneric> {
    private double imageSize = 60;
    private double buttonSize = 30;

    @Override
    protected void updateItem(QuestionGeneric item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            if (Koeko.questionSendingControllerSingleton.editedTestItem != null
                    && Long.valueOf(item.getGlobalID()) > 0 && this.getTreeItem().getParent().getParent() == null
                    && Koeko.questionSendingControllerSingleton.questionsForTest.contains(item.getGlobalID())) {
                this.setStyle("-fx-background-color: #ff8080;");
            } else {
                this.setStyle("");
            }

            HBox hbox = new HBox(10);

            VBox buttonsBox = new VBox(3);

            Button buttonEdit = new Button("Ed");
            buttonEdit.setOnAction((event) -> {
                editItem(item);
            });
            Button buttonDelete = new Button("X");
            buttonDelete.setOnAction((event) -> {
                deleteItem(item);
            });
            buttonsBox.getChildren().addAll(buttonEdit, buttonDelete);

            VBox buttonsBox2 = new VBox(3);
            Button buttonQRCode = new Button("QR");
            buttonQRCode.setOnAction((event) -> {
                saveQRCode(item, buttonDelete);
            });
            Button buttonBuildTest = new Button("+Q");
            if (this.getTreeItem() == Koeko.questionSendingControllerSingleton.editedTestItem) {
                buttonBuildTest.setStyle("-fx-background-color: #ff8080;");
            } else {
                buttonBuildTest.setStyle("");
            }
            buttonBuildTest.setOnAction((event) -> {
                toggleTestEditMode(this.getTreeItem(), buttonBuildTest);
            });

            if (item.getTypeOfQuestion().contentEquals("TEFO")) {
                buttonsBox2.getChildren().addAll(buttonQRCode, buttonBuildTest);
            } else {
                buttonsBox2.getChildren().addAll(buttonQRCode);
            }



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

    private void editItem(QuestionGeneric item) {
        if (item.getIntTypeOfQuestion() == 0 || item.getIntTypeOfQuestion() == 1) {
            //case if we want to edit a question
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditQuestion.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditQuestionController controller = fxmlLoader.getController();
            TreeItem selectedItem = this.getTreeItem();
            controller.initVariables(Koeko.questionSendingControllerSingleton.genericQuestionsList,
                    Koeko.questionSendingControllerSingleton.allQuestionsTree, item, selectedItem,
                    Koeko.questionSendingControllerSingleton.allQuestionsTree);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Edit Question");
            stage.setScene(new Scene(root1));
            stage.show();
        } else {
            //case if we want to edit a test
            if (Long.valueOf(item.getGlobalID()) < 0) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditTest.fxml"));
                Parent root1 = null;
                try {
                    root1 = fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EditTestController controller = fxmlLoader.getController();
                ArrayList<String> testNames = new ArrayList<>();
                Koeko.questionSendingControllerSingleton.testsList = DbTableTests.getAllTests();
                for (Test test : Koeko.questionSendingControllerSingleton.testsList) {
                    testNames.add(test.getTestName());
                }
                ArrayList<String> objectives = DbTableRelationObjectiveTest.getObjectivesFromTestName(item.getQuestion());
                controller.initParameters(Koeko.questionSendingControllerSingleton.allQuestionsTree, testNames,
                        QuestionGeneric.changeIdSign(item.getGlobalID()), objectives);
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initStyle(StageStyle.DECORATED);
                stage.setTitle("Edit Test");
                stage.setScene(new Scene(root1));
                stage.show();
            } else {
                System.out.println("Cannot edit test: no test selected");
            }
        }
    }

    private void deleteItem(QuestionGeneric item) {
        if (this.getTreeItem().getParent().getParent() == null) {
            //case if test or question not in test -> we delete the question/test itself
            if (Long.valueOf(item.getGlobalID()) < 0) {
                DbTableTests.removeTestWithName(item.getQuestion());
            } else {
                //only sets a flag for the question generic, leave the whole question inside database and doesn't delete image
                DbTableQuestionGeneric.removeQuestion(item.getGlobalID());
            }
        } else {
            //case if question in test -> we delete the relations
            QuestionGeneric parentTest = this.getTreeItem().getParent().getValue();
            QuestionGeneric questionGeneric = item;
            DbTableRelationQuestionQuestion.removeRelationsWithQuestion(item.getGlobalID());
            DbTableRelationQuestionTest.removeQuestionFromTest(parentTest.getQuestion(), questionGeneric.getGlobalID());
        }
        Koeko.questionSendingControllerSingleton.testsNodeList.remove(item);
        this.getTreeItem().getParent().getChildren().remove(this.getTreeItem());
    }

    private void saveQRCode(QuestionGeneric item, Button buttonDelete) {
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
    }

    private void toggleTestEditMode(TreeItem<QuestionGeneric> item, Button button) {
        if (Koeko.questionSendingControllerSingleton.editedTestItem != null) {
            Koeko.questionSendingControllerSingleton.editedTestItem = null;
            button.setStyle("");
        } else {
            Koeko.questionSendingControllerSingleton.editedTestItem = item;
            button.setStyle("-fx-background-color: #ff8080;");
            String toastMsg = "Double click on a question to add it to this test";
            int toastMsgTime = 2500; //3.5 seconds
            int fadeInTime = 500; //0.5 seconds
            int fadeOutTime= 500; //0.5 seconds
            Toast.makeText((Stage) button.getScene().getWindow(), toastMsg, toastMsgTime, fadeInTime, fadeOutTime);
        }
    }
}
