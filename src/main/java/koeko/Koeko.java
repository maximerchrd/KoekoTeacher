package koeko;

import java.io.IOException;
import java.util.ArrayList;

import koeko.Networking.NetworkCommunication;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Koeko extends Application {
    static public QuestionSendingController questionSendingControllerSingleton = null;
    static public StudentsVsQuestionsTableController studentsVsQuestionsTableControllerSingleton = null;
    static public QuestionBrowsingController questionBrowsingControllerSingleton = null;
    static public ArrayList<Classroom> studentGroupsAndClass;

    public static void main(String[] args) throws Exception {


        //does db stuffs
        DBManager dao = new DBManager();
        dao.createDBIfNotExists();
        dao.createTablesIfNotExists();

        Application.launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
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


        /*for (Long i = 0L; i < 3; i++) {
            functionalTesting.mainTesting(3, 2, 10, 4000 - i * 100);
        }*/
    }

}
