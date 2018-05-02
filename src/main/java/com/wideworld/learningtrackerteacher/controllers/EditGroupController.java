package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.LearningTracker;
import com.wideworld.learningtrackerteacher.database_management.DbTableClasses;
import com.wideworld.learningtrackerteacher.database_management.DbTableRelationClassStudent;
import com.wideworld.learningtrackerteacher.students_management.Student;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EditGroupController extends Window implements Initializable {
    private String className;
    private ComboBox groupsCombobox;
    private ArrayList<String> students;
    private ArrayList<HBox> hBoxArrayList;
    private int groupIndex = -1;

    @FXML private VBox createGroupVBox;
    @FXML private TextField groupName;

    public void initParameters(String className, ComboBox groupsCombo, ArrayList<String> students, ArrayList<String> studentsInGroup) {
        this.className = className;
        this.groupsCombobox = groupsCombo;
        this.students = students;
        hBoxArrayList = new ArrayList<>();
        groupIndex = groupsCombobox.getSelectionModel().getSelectedIndex();

        groupName.setText(groupsCombobox.getSelectionModel().getSelectedItem().toString());
        for (String student : studentsInGroup) {
            addStudentToGroup(student);
        }
    }

    public void addStudentToGroup() {
        addStudentToGroup("");
    }
    public void addStudentToGroup(String student) {
        HBox studentHBox = new HBox();
        ComboBox<String> studentComboBox = new ComboBox(FXCollections.observableList(students));
        if (student.length() > 0) {
            studentComboBox.getSelectionModel().select(student);
        }
        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            hBoxArrayList.remove(studentHBox);
            ((VBox)studentHBox.getParent()).getChildren().remove(studentHBox);
        });
        studentHBox.getChildren().add(studentComboBox);
        studentHBox.getChildren().add(removeButton);
        createGroupVBox.getChildren().add(studentHBox);
        hBoxArrayList.add(studentHBox);
    }

    public void saveGroup() {
        if (groupIndex >= 0) {
            //add group to class
            DbTableClasses.updateGroup(groupName.getText(), groupsCombobox.getSelectionModel().getSelectedItem().toString());
            DbTableRelationClassStudent.removeAllStudentsFromClass(groupName.getText());
            groupsCombobox.getItems().set(groupIndex,groupName.getText());

            //add studentGroupsAndClass to group
            //initialize students array and add it to the static singleton

            LearningTracker.studentGroupsAndClass.get(groupIndex).setClassName(groupName.getText());
            LearningTracker.studentGroupsAndClass.get(groupIndex).getStudents_vector().clear();
            for (HBox hBox : hBoxArrayList) {
                String student = ((ComboBox) hBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
                DbTableRelationClassStudent.addClassStudentRelation(groupName.getText(), student);

                Student studentObject = LearningTracker.studentGroupsAndClass.get(groupIndex).getStudentWithName(student);
                LearningTracker.studentGroupsAndClass.get(groupIndex).addStudentIfNotInClass(studentObject);
            }
        }

        Stage stage = (Stage) groupName.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
