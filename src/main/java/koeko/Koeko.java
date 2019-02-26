package koeko;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.scene.Parent;
import javafx.stage.*;
import koeko.Networking.NetworkCommunication;
import koeko.controllers.Game.Game;
import koeko.controllers.Game.GameController;
import koeko.controllers.LearningTrackerController;
import koeko.controllers.LeftBar.LeftBarController;
import koeko.controllers.QuestionSendingController;
import koeko.controllers.StudentsVsQuestions.StudentsVsQuestionsTableController;
import koeko.controllers.SubjectsBrowsing.QuestionBrowsingController;
import koeko.controllers.controllers_tools.ControllerUtils;
import koeko.database_management.DBManager;
import koeko.database_management.DbTableSettings;
import koeko.database_management.DbUtils;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.students_management.Classroom;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class Koeko extends Application {
    static public QuestionSendingController questionSendingControllerSingleton = null;
    static public StudentsVsQuestionsTableController studentsVsQuestionsTableControllerSingleton = null;
    static public QuestionBrowsingController questionBrowsingControllerSingleton = null;
    static public LeftBarController leftBarController = null;
    static public GameController gameControllerSingleton = null;
    static public ArrayList<Classroom> studentGroupsAndClass;
    static public Boolean recordLogs = false;
    static public ArrayList<Game> activeGames = new ArrayList<>();
    static public Stage mainStage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        Boolean firstAppLaunch = false;
        File f = new File(DBManager.databaseName);
        if(!f.exists()) {
            firstAppLaunch = true;
        }

        //does db stuffs
        DBManager dao = new DBManager();
        dao.createDBIfNotExists();
        dao.createTablesIfNotExists();

        mainStage.setTitle("Koeko");

        FXMLLoader loader = loadView(new Locale(DbTableSettings.getLanguage()), getClass());

        //start server
        LearningTrackerController learningTrackerController = loader.getController();
        NetworkCommunication CommunicationWithClients = new NetworkCommunication(learningTrackerController);
        try {
            CommunicationWithClients.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }



        mainStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        if (firstAppLaunch) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/InstallAssistant.fxml"));
            Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle(ResourceBundle.getBundle("bundles.LangBundle", new Locale(DbTableSettings.getLanguage()))
                    .getString("string.starting_assistant"));
            stage.setScene(new Scene(root1));
            stage.show();
        }

//        for (Long i = 0L; i < 9; i++) {
//            functionalTesting.mainTesting(3, 22, 20, 10000L, 30);
//            Thread.sleep(760000);
//        }
    }

    public static FXMLLoader loadView(Locale locale, Class currentClass) throws IOException {
        Scene scene = new Scene(new StackPane());

        FXMLLoader loader = new FXMLLoader(currentClass.getResource("/views/LearningTracker.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.LangBundle", locale));
        scene.setRoot(loader.load());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to visible bounds of the main screen
        mainStage.setX(primaryScreenBounds.getMinX());
        mainStage.setY(primaryScreenBounds.getMinY());
        mainStage.setWidth(primaryScreenBounds.getWidth());
        mainStage.setHeight(primaryScreenBounds.getHeight());

        //change UI mode (do it here because before, some elements are still null
        Koeko.leftBarController.changeUI();

        mainStage.setScene(scene);
        mainStage.show();

        return loader;
    }
}
