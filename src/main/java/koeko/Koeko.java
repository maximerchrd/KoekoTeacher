package koeko;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.Parent;
import javafx.stage.*;
import koeko.Networking.NetworkCommunication;
import koeko.controllers.GenericPopUpController;
import koeko.controllers.InstallAssistantController;
import koeko.controllers.LearningTrackerController;
import koeko.controllers.QuestionSendingController;
import koeko.controllers.StudentsVsQuestions.StudentsVsQuestionsTableController;
import koeko.controllers.SubjectsBrowsing.QuestionBrowsingController;
import koeko.database_management.DBManager;
import koeko.students_management.Classroom;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

import javax.swing.*;

public class Koeko extends Application {
    static public QuestionSendingController questionSendingControllerSingleton = null;
    static public StudentsVsQuestionsTableController studentsVsQuestionsTableControllerSingleton = null;
    static public QuestionBrowsingController questionBrowsingControllerSingleton = null;
    static public ArrayList<Classroom> studentGroupsAndClass;
    static public Boolean recordLogs = false;

    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name").contains("OS X")) {
            try {
                com.apple.eawt.Application.getApplication().setDockIconImage(new ImageIcon("pictures/app-icon.png").getImage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Boolean firstAppLaunch = false;
        File f = new File(DBManager.databaseName);
        if(!f.exists()) {
            firstAppLaunch = true;
        }

        //does db stuffs
        DBManager dao = new DBManager();
        dao.createDBIfNotExists();
        dao.createTablesIfNotExists();

        primaryStage.setTitle("Koeko");

        Scene scene = new Scene(new StackPane());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LearningTracker.fxml"));
        scene.setRoot(loader.load());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to visible bounds of the main screen
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());

        //change UI mode (do it here because before, some elements are still null
        Koeko.questionSendingControllerSingleton.changeUI();

        primaryStage.setScene(scene);
        primaryStage.show();

        //start server

        LearningTrackerController learningTrackerController = loader.getController();
        NetworkCommunication CommunicationWithClients = new NetworkCommunication(learningTrackerController);
        try {
            CommunicationWithClients.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }



        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        if (firstAppLaunch) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/InstallAssistant.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Starting Assistant");
            stage.setScene(new Scene(root1));
            stage.show();
        }

        //for (Long i = 0L; i < 3; i++) {
            //functionalTesting.mainTesting(4, 3, 5, 5000L, 30);
        //}
    }

}
