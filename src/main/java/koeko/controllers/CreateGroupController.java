package koeko.controllers;

import koeko.Koeko;
import koeko.database_management.DbTableClasses;
import koeko.database_management.DbTableRelationClassStudent;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CreateGroupController extends Window implements Initializable {
    private String className;
    private ComboBox groupsCombobox;
    private ArrayList<String> students;
    private ArrayList<HBox> hBoxArrayList;

    @FXML private VBox createGroupVBox;
    @FXML private TextField groupName;

    public void initParameters(String className, ComboBox groupsCombo, ArrayList<String> students) {
        this.className = className;
        this.groupsCombobox = groupsCombo;
        this.students = students;
        hBoxArrayList = new ArrayList<>();
    }

    public void addStudentToGroup() {
        HBox studentHBox = new HBox();
        ComboBox<String> studentComboBox = new ComboBox(FXCollections.observableList(students));
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
        //add group to class
        DbTableClasses.addGroupToClass(groupName.getText(), className);
        groupsCombobox.getItems().add(groupName.getText());

        //add studentGroupsAndClass to group
        //initialize students array and add it to the static singleton
        Classroom newGroup = new Classroom();
        Koeko.studentGroupsAndClass.add(newGroup);

        for (HBox hBox : hBoxArrayList) {
            String student = ((ComboBox)hBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
            DbTableRelationClassStudent.addClassStudentRelation(groupName.getText(),student);

            Student studentObject = Koeko.studentGroupsAndClass.get(Koeko.studentGroupsAndClass.size() - 1).getStudentWithName(student);
            Koeko.studentGroupsAndClass.get(Koeko.studentGroupsAndClass.size() - 1).addStudent(studentObject);
        }

        Stage stage = (Stage) groupName.getScene().getWindow();
        stage.close();
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
