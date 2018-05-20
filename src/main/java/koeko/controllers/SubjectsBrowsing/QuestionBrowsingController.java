package koeko.controllers.SubjectsBrowsing;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.controllers.CreateQuestionController;
import koeko.database_management.DbTableSubject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.Vector;

import static koeko.database_management.DbTableSubject.getAllSubjects;

/**
 * Created by maximerichard on 13.03.18.
 */
public class QuestionBrowsingController implements Initializable {

    @FXML private Label labelIP;

    public void initialize(URL location, ResourceBundle resources) {
        final String[] ip_address = {""};
        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ip_address[0] = InetAddress.getLocalHost().getHostAddress();
                Platform.runLater(() -> labelIP.setText("studentGroupsAndClass should connect \nto the following address: " + ip_address[0]));
                return null;
            }
        };
        new Thread(getIPTask).start();

        //retrieve data from the db
        Vector<String> subjects = DbTableSubject.getAllSubjects();

        //build the subjects tree

    }

    public void refreshIP() {
        final String[] ip_address = {""};
        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ip_address[0] = InetAddress.getLocalHost().getHostAddress();
                Platform.runLater(() -> labelIP.setText("studentGroupsAndClass should connect \nto the following address: " + ip_address[0]));
                return null;
            }
        };
        new Thread(getIPTask).start();
    }

    public void createSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateSubject.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateQuestionController controller = fxmlLoader.getController();
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }
}
