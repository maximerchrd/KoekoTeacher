package koeko.controllers.QuestionsControlling;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.Tools.FilesHandler;
import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableSubject;
import org.controlsfx.control.textfield.TextFields;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

public class QuestionEditing {
    static void addAnswerOption(String option, ComboBox typeOfQuestion, ResourceBundle bundle, ArrayList<HBox> hBoxArrayList, VBox vBox) {
        HBox hBox = new HBox();
        TextField textField = new TextField(option);
        hBox.setHgrow(textField, Priority.ALWAYS);
        CheckBox checkBox = new CheckBox();
        if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().contentEquals(bundle.getString("string.shrtaq"))) {
            checkBox.setVisible(false);
        }
        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            hBoxArrayList.remove(hBox);
            (( VBox)hBox.getParent()).getChildren().remove(hBox);
        });
        hBox.getChildren().add(checkBox);
        hBox.getChildren().add(textField);
        hBox.getChildren().add(removeButton);
        hBox.setMargin(textField, new Insets(0,5,0,0));
        vBox.getChildren().add(vBox.getChildren().size() - 2, hBox);
        vBox.setMargin(hBox, new Insets(0,0,5,0));
        hBoxArrayList.add(hBox);
    }

    static void addSubject(String subject, ArrayList<ComboBox> subjectsComboBoxArrayList, int buttonImageSize, VBox vBoxSubjects) {
        Vector<String> subjectsVector = DbTableSubject.getAllSubjectsAsStrings();
        addItem(subject, subjectsComboBoxArrayList, buttonImageSize, vBoxSubjects, subjectsVector);
    }

    static void addObjective(String objective, ArrayList<ComboBox> objectivesComboBoxArrayList, int buttonImageSize, VBox vBoxObjectives) {
        Vector<String> objectivessVector = DbTableLearningObjectives.getAllObjectives();
        addItem(objective, objectivesComboBoxArrayList, buttonImageSize, vBoxObjectives, objectivessVector);
    }

    private static void addItem(String objectiveOrSubjects, ArrayList<ComboBox> objectivesComboBoxArrayList, int buttonImageSize, VBox vBoxItems, Vector<String> objectivessVector) {
        String[] items = objectivessVector.toArray(new String[objectivessVector.size()]);
        ObservableList<String> options =
                FXCollections.observableArrayList(items);
        ComboBox comboBox = new ComboBox(options);
        comboBox.setEditable(true);
        comboBox.setValue(objectiveOrSubjects);
        objectivesComboBoxArrayList.add(comboBox);

        HBox hBox = new HBox(5);

        Button editButton = new Button();
        ImageView editImage = new ImageView(new Image("/drawable/editImage.png", buttonImageSize, buttonImageSize, true, true));
        editButton.setGraphic(editImage);
        editButton.setOnAction(event -> {
            if (comboBox.getSelectionModel().getSelectedItem() != null) {
                QuestionEditing.promptEditObjective(comboBox.getSelectionModel().getSelectedItem().toString(), comboBox);
            }
        });

        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            objectivesComboBoxArrayList.remove(comboBox);
            (( VBox)hBox.getParent()).getChildren().remove(hBox);
        });
        hBox.getChildren().addAll(comboBox,editButton,removeButton);
        hBox.setHgrow(comboBox, Priority.ALWAYS);
        vBoxItems.setMargin(hBox, new Insets(0,0,0,5));
        vBoxItems.getChildren().add(hBox);
        TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());
    }

    static void addPicture(VBox vBox, TextField imagePath) {
        File theDir = new File(FilesHandler.mediaDirectoryNoSlash);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                System.out.println("DIR created");
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image file");
        Stage stage = (Stage) vBox.getScene().getWindow();
        File source_file = fileChooser.showOpenDialog(stage);
        File dest_file = new File(FilesHandler.mediaDirectory + source_file.getName());
        File hashedFileName = new File(FilesHandler.mediaDirectory + source_file.getName());
        try {
            Files.copy(source_file.toPath(), dest_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = Files.readAllBytes(dest_file.toPath());
            messageDigest.update(hashedBytes);
            String encryptedString = DatatypeConverter.printHexBinary(messageDigest.digest());
            if (encryptedString.length() > 14) {
                encryptedString = encryptedString.substring(0, 14);
            }
            hashedFileName = new File(FilesHandler.mediaDirectory + encryptedString);
            Files.move(dest_file.toPath(), hashedFileName.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        imagePath.setText(hashedFileName.getName());
        imagePath.setEditable(false);
    }

    static public void promptEditSubject(String oldSubject, ComboBox subjectCombobox) {
        Platform.runLater(() -> {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Koeko.studentsVsQuestionsTableControllerSingleton);
            dialog.initStyle(StageStyle.DECORATED);

            HBox subjectHbox = new HBox(20);
            Label subjectLabel = new Label("Edit subject:");
            TextField subjectField = new TextField(oldSubject);
            subjectHbox.getChildren().addAll(subjectLabel, subjectField);
            Button saveButton = new Button("Edit Subject");
            saveButton.setOnAction(event -> {
                DbTableSubject.updateSubject(oldSubject, subjectField.getText());
                ObservableList<String> subjects = subjectCombobox.getItems();
                for (String subject : subjects) {
                    if (subject.contentEquals(oldSubject)) {
                        subjects.set(subjects.indexOf(subject), subjectField.getText());
                    }
                }
                subjectCombobox.setItems(subjects);
                subjectCombobox.getSelectionModel().select(subjectField.getText());
                dialog.close();
            });
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().addAll(subjectHbox, saveButton);
            Scene dialogScene = new Scene(dialogVbox, 450, 80);
            dialog.setScene(dialogScene);
            dialog.show();
        });
    }

    static public void promptEditObjective(String oldObjective, ComboBox objectiveCombobox) {
        Platform.runLater(() -> {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Koeko.studentsVsQuestionsTableControllerSingleton);
            dialog.initStyle(StageStyle.DECORATED);

            HBox objectiveHbox = new HBox(20);
            Label objectiveLabel = new Label("Edit objective:");
            TextField objectiveField = new TextField(oldObjective);
            objectiveHbox.getChildren().addAll(objectiveLabel, objectiveField);
            Button saveButton = new Button("Edit Objective");
            saveButton.setOnAction(event -> {
                DbTableLearningObjectives.updateObjective(oldObjective, objectiveField.getText());
                ObservableList<String> objectives = objectiveCombobox.getItems();
                for (String objective : objectives) {
                    if (objective.contentEquals(oldObjective)) {
                        objectives.set(objectives.indexOf(objective), objectiveField.getText());
                    }
                }
                objectiveCombobox.setItems(objectives);
                objectiveCombobox.getSelectionModel().select(objectiveField.getText());
                dialog.close();
            });
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().addAll(objectiveHbox, saveButton);
            Scene dialogScene = new Scene(dialogVbox, 450, 80);
            dialog.setScene(dialogScene);
            dialog.show();
        });
    }
}