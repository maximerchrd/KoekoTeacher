package koeko.controllers.QuestionSending;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.Tools.FilesHandler;
import koeko.controllers.QuestionsControlling.EditQuestionController;
import koeko.controllers.TestControlling.EditTestController;
import koeko.controllers.controllers_tools.ControllerUtils;
import koeko.controllers.controllers_tools.Toast;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.Test;
import koeko.view.Utilities;
import net.glxn.qrgen.javase.QRCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

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

            HBox hbox = new HBox(6);

            if (this.getTreeItem().getParent().getValue().getIntTypeOfQuestion() == QuestionGeneric.FORMATIVE_TEST) {
                VBox updownBox = new VBox(3);
                Button upButton = new Button("↑");
                upButton.setOnAction(event -> {
                    moveUpNode(item);
                });
                Button downButton = new Button("↓");
                downButton.setOnAction(event -> {
                    moveDownNode(item);
                });

                updownBox.getChildren().addAll(upButton, downButton);
                Separator separator = new Separator();
                separator.setVisible(false);
                hbox.getChildren().addAll(updownBox,separator);
            }

            VBox buttonsBox = new VBox(3);

            double buttonImageSize = 20;
            ImageView editImage = new ImageView(new Image("/drawable/editImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonEdit = new Button();
            buttonEdit.setGraphic(editImage);
            buttonEdit.setTooltip(
                    new Tooltip("Edit question")
            );
            buttonEdit.setOnAction((event) -> {
                editItem(item);
            });

            ImageView deleteImage = new ImageView(new Image("/drawable/deleteImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonDelete = new Button();
            buttonDelete.setGraphic(deleteImage);
            buttonDelete.setTooltip(
                    new Tooltip("Delete question")
            );
            buttonDelete.setOnAction((event) -> {
                deleteItem(item);
            });
            buttonsBox.getChildren().addAll(buttonEdit, buttonDelete);

            VBox buttonsBox2 = new VBox(3);

            ImageView qrImage = new ImageView(new Image("/drawable/qrImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonQRCode = new Button();
            buttonQRCode.setGraphic(qrImage);
            buttonQRCode.setTooltip(
                    new Tooltip("Download image of QR code corresponding to the question")
            );
            buttonQRCode.setOnAction((event) -> {
                saveQRCode(item, buttonDelete);
            });

            ImageView linkImage = new ImageView(new Image("/drawable/linkImage.png", buttonImageSize, buttonImageSize, true, true));
            Button buttonBuildTest = new Button();
            buttonBuildTest.setGraphic(linkImage);
            buttonBuildTest.setTooltip(
                    new Tooltip("Add questions to the test")
            );
            if (this.getTreeItem() == Koeko.questionSendingControllerSingleton.editedTestItem) {
                buttonBuildTest.setStyle("-fx-background-color: #ff8080;");
            } else {
                buttonBuildTest.setStyle("");
            }
            buttonBuildTest.setOnAction((event) -> {
                toggleTestEditMode(this.getTreeItem(), buttonBuildTest);
            });

            if (item.getIntTypeOfQuestion() == QuestionGeneric.FORMATIVE_TEST) {
                buttonsBox2.getChildren().addAll(buttonQRCode, buttonBuildTest);
            } else {
                buttonsBox2.getChildren().addAll(buttonQRCode);
            }



            ImageView image = new ImageView();

            setText(item.getQuestion());
            try {
                if (Long.valueOf(item.getGlobalID()) > 0) {
                    String url = "file:" + FilesHandler.mediaDirectory + item.getImagePath();
                    image = new ImageView(new Image(url, imageSize, imageSize, true, true));
                } else {
                    if (item.getIntTypeOfQuestion() == QuestionGeneric.FORMATIVE_TEST) {
                        image = new ImageView(new Image("/drawable/test.png", imageSize, imageSize, true, true));
                    } else if (item.getIntTypeOfQuestion() == QuestionGeneric.CERTIFICATIVE_TEST) {
                        image = new ImageView(new Image("/drawable/test_certificative.png", imageSize, imageSize, true, true));
                    } else if (item.getIntTypeOfQuestion() == QuestionGeneric.GAME) {
                        image = new ImageView(new Image("/drawable/game.png", imageSize, imageSize, true, true));
                    } else if (item.getIntTypeOfQuestion() == QuestionGeneric.GAME_QUESTIONSET) {
                        image = new ImageView(new Image("/drawable/game_question_set.png", imageSize, imageSize, true, true));
                    }
                }
                hbox.getChildren().addAll(buttonsBox, buttonsBox2, image);
                setGraphic(hbox);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void editItem(QuestionGeneric item) {
        TreeItem selectedItem = this.getTreeItem();
        if (item.getIntTypeOfQuestion() == 0 || item.getIntTypeOfQuestion() == 1) {
            //case if we want to edit a question
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditQuestion.fxml"));
            Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
            EditQuestionController controller = fxmlLoader.getController();
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
                Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
                EditTestController controller = fxmlLoader.getController();
                ArrayList<String> testNames = new ArrayList<>();
                Koeko.questionSendingControllerSingleton.testsList = DbTableTest.getAllTests();
                for (Test test : Koeko.questionSendingControllerSingleton.testsList) {
                    testNames.add(test.getTestName());
                }
                ArrayList<String> objectives = DbTableRelationObjectiveTest.getObjectivesFromTestName(item.getQuestion());
                controller.initParameters(Koeko.questionSendingControllerSingleton.allQuestionsTree, testNames,
                        QuestionGeneric.changeIdSign(item.getGlobalID()), objectives, item, selectedItem);
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
                DbTableTest.removeTestWithName(item.getQuestion());
            } else {
                //only sets a flag for the question generic, leave the whole question inside database and doesn't delete image
                DbTableQuestionGeneric.removeQuestion(item.getGlobalID());
            }
        } else {
            //case if question in test -> we delete the relations
            QuestionGeneric parentTest = this.getTreeItem().getParent().getValue();
            QuestionGeneric questionGeneric = item;
            DbTableRelationQuestionQuestion.removeRelationsWithQuestion(item.getGlobalID());
        }
        Koeko.questionSendingControllerSingleton.testsNodeList.remove(item);
        this.getTreeItem().getParent().getChildren().remove(this.getTreeItem());
    }

    private void saveQRCode(QuestionGeneric item, Button buttonDelete) {
        if (item.getIntTypeOfQuestion() == 4) {
            String identifier = String.valueOf(System.nanoTime());
            saveGameQrCode(buttonDelete, 1, identifier);
            saveGameQrCode(buttonDelete, 2, identifier);
        } else {
            String identifier = "0";
            if (item.getIntTypeOfQuestion() == 0) {
                identifier = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(item.getGlobalID()).getQCM_MUID();
            } else if (item.getIntTypeOfQuestion() == 1) {
                identifier = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(item.getGlobalID()).getUID();
            } else if (item.getIntTypeOfQuestion() == 2 || item.getIntTypeOfQuestion() == 5) {
                identifier = DbTableTest.getTestWithID(item.getGlobalID()).getIdTest();
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
    }

    private void saveGameQrCode(Button buttonDelete, int team, String identifier) {
        Stage stage = (Stage) buttonDelete.getScene().getWindow();
        File file = QRCode.from("GAME:" + team + ":" + identifier + ":").file();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file for team " + team);
        fileChooser.setInitialFileName("code_for_team_" + team);
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

    private QuestionGeneric getParentTest(TreeItem<QuestionGeneric> node) {
        TreeItem<QuestionGeneric> testItem = node.getParent();
        while (testItem.getValue().getIntTypeOfQuestion() != QuestionGeneric.FORMATIVE_TEST) {
            testItem = testItem.getParent();
            if (testItem == null) {
                return null;
            }
        }
        return testItem.getValue();
    }

    private void moveUpNode(QuestionGeneric item) {
        TreeItem<QuestionGeneric> parent = this.getTreeItem().getParent();
        TreeItem<QuestionGeneric> thisTreeItem = this.getTreeItem();
        int index = parent.getChildren().indexOf(thisTreeItem);
        if (index > 0) {
            TreeItem<QuestionGeneric> previousTreeItem = parent.getChildren().get(index - 1);
            QuestionGeneric parentTest = getParentTest(thisTreeItem);
            if (parentTest != null) {
                DbTableRelationQuestionQuestion.moveUp(item.getGlobalID(), previousTreeItem.getValue().getGlobalID(),
                        Utilities.setPositiveIdSign(parentTest.getGlobalID()));
                parent.getChildren().set(index - 1, thisTreeItem);
                parent.getChildren().set(index, previousTreeItem);
            } else {
                System.err.println("Error fetching parent test: is null");
            }
        }
    }

    private void moveDownNode(QuestionGeneric item) {
        TreeItem<QuestionGeneric> parent = this.getTreeItem().getParent();
        TreeItem<QuestionGeneric> thisTreeItem = this.getTreeItem();
        int index = parent.getChildren().indexOf(thisTreeItem);
        if (index < parent.getChildren().size() - 1) {
            TreeItem<QuestionGeneric> nextTreeItem = parent.getChildren().get(index + 1);
            QuestionGeneric parentTest = getParentTest(thisTreeItem);
            if (parentTest != null ) {
                DbTableRelationQuestionQuestion.moveDown(item.getGlobalID(), nextTreeItem.getValue().getGlobalID(),
                        Utilities.setPositiveIdSign(parentTest.getGlobalID()));
                parent.getChildren().set(index + 1, thisTreeItem);
                parent.getChildren().set(index, nextTreeItem);
            } else {
                System.err.println("Problem fetching parent test moving node down: is null");
            }
        }
    }
}
