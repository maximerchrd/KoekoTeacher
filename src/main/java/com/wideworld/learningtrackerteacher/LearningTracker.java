package com.wideworld.learningtrackerteacher;

import java.io.IOException;

import com.wideworld.learningtrackerteacher.controllers.LearningTrackerController;
import com.wideworld.learningtrackerteacher.database_management.DBManager;
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

public class LearningTracker extends Application {

    public static void main(String[] args) throws Exception {


        //does db stuffs
        DBManager dao = new DBManager();
        dao.createDBIfNotExists();
        dao.createTablesIfNotExists();

        Application.launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Learning Tracker");

        Scene scene = new Scene(new StackPane());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LearningTracker.fxml"));
        scene.setRoot(loader.load());

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to visible bounds of the main screen
        primaryStage.setX(primaryScreenBounds.getMinX());
        primaryStage.setY(primaryScreenBounds.getMinY());
        primaryStage.setWidth(primaryScreenBounds.getWidth());
        primaryStage.setHeight(primaryScreenBounds.getHeight());

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
    }

}
