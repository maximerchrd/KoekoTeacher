package koeko.controllers.StudentsVsQuestions;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.Koeko;
import koeko.controllers.SubjectsBrowsing.QuestionBrowsingController;
import koeko.database_management.DbTableClasses;
import koeko.database_management.DbTableRelationClassStudent;
import koeko.database_management.DbTableStudents;
import koeko.database_management.DbTableSubject;
import koeko.students_management.Student;
import koeko.view.Subject;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

public class CreateStudentController extends Window implements Initializable {

    @FXML private TextField studentName;

    String className = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initClass(String className) {
        this.className = className;
    }

    public void saveStudent() {
        Vector<String> studentNames = DbTableStudents.getStudentNames();
        if (!studentNames.contains(studentName.getText())) {
            String studentId = DbTableStudents.addStudent("not initialized", studentName.getText());
            DbTableRelationClassStudent.addClassStudentRelation(className, studentName.getText());

            Student newStudent = new Student();
            newStudent.setName(studentName.getText());
            newStudent.setStudentID(studentId);
            Koeko.studentsVsQuestionsTableControllerSingleton.addUser(newStudent, false, 0);

            Stage stage = (Stage) studentName.getScene().getWindow();
            stage.close();
        } else {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("This student name already exists. Please choose an other name."));
            Scene dialogScene = new Scene(dialogVbox, 400, 40);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }
}
