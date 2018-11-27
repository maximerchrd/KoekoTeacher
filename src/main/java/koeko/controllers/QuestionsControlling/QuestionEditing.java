package koeko.controllers.QuestionsControlling;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.database_management.DbTableLearningObjectives;
import koeko.database_management.DbTableSubject;

public class QuestionEditing {
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
