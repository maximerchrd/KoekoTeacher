package koeko.controllers.SubjectsBrowsing;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import koeko.database_management.DbTableSubject;
import koeko.students_management.Subject;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

public class EditSubjectController extends Window implements Initializable {

    @FXML private TextField subjectName;
    private String oldSubjectName = "";
    private TreeItem<Subject> subjectTreeItem;
    private TreeView<Subject> subjectTreeView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initializeSubject(String subject_Name, TreeView<Subject> subjectTreeView) {
        this.subjectTreeView = subjectTreeView;
        subjectName.setText(subject_Name);
        oldSubjectName = subject_Name;
        for (TreeItem<Subject> treeItem : QuestionBrowsingController.rootSubjectSingleton.getChildren()) {
            if (treeItem.getValue().get_subjectName().contentEquals(subject_Name)) {
                subjectTreeItem = treeItem;
                break;
            }
        }
    }

    public void saveSubject() {
        Vector<String> subjectNames = DbTableSubject.getAllSubjectsAsStrings();
        if (!subjectNames.contains(subjectName.getText())) {
            DbTableSubject.updateSubject(oldSubjectName,subjectName.getText());
            subjectTreeItem.getValue().set_subjectName(subjectName.getText());
            subjectTreeView.refresh();

            Stage stage = (Stage) subjectName.getScene().getWindow();
            stage.close();
        } else {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("This subject name already exists. Please choose an other name."));
            Scene dialogScene = new Scene(dialogVbox, 400, 40);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }
}
