package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.database_management.DbTableSettings;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    static public Integer nearbyMode = -1;
    static public Integer correctionMode = -1;
    @FXML private TextArea logTextArea;
    @FXML private TextField teacherName;
    @FXML private ToggleButton nearbyModeButton;
    @FXML private ToggleButton correctionModeButton;

    public void nearbyModeChanged() {
        if (nearbyModeButton.isSelected()) {
            nearbyMode = 1;
            DbTableSettings.insertNearbyMode(nearbyMode);
            nearbyModeButton.setText("ON");
        } else {
            nearbyMode = 0;
            DbTableSettings.insertNearbyMode(nearbyMode);
            nearbyModeButton.setText("OFF");
        }
    }
    public void correctionModeChanged() {
        if (correctionModeButton.isSelected()) {
            correctionMode = 1;
            DbTableSettings.insertCorrectionMode(correctionMode);
            correctionModeButton.setText("ON");
        } else {
            correctionMode = 0;
            DbTableSettings.insertCorrectionMode(correctionMode);
            correctionModeButton.setText("OFF");
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nearbyMode = DbTableSettings.getNearbyMode();
        correctionMode = DbTableSettings.getCorrectionMode();
        teacherName.setText(DbTableSettings.getTeacherName());

        if (nearbyMode == 0) {
            nearbyModeButton.setText("OFF");
        } else if (nearbyMode == 1) {
            nearbyModeButton.setText("ON");
            nearbyModeButton.setSelected(true);
        }

        if (correctionMode == 0) {
            correctionModeButton.setText("OFF");
        } else if (correctionMode == 1) {
            correctionModeButton.setText("ON");
            correctionModeButton.setSelected(true);
        }

        /*PipedOutputStream pOut = new PipedOutputStream();
        PipedOutputStream pErr = new PipedOutputStream();
        System.setOut(new PrintStream(pOut));
        System.setErr(new PrintStream(pErr));
        PipedInputStream pIn = null;
        PipedInputStream pIn2 = null;
        try {
            pIn = new PipedInputStream(pOut);
            pIn2 = new PipedInputStream(pErr);
            BufferedReader reader = new BufferedReader(new InputStreamReader(pIn));
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(pIn2));
            Task<Void> writeLog = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    while (true) {
                        String line = reader.readLine();
                        if(line != null) {
                            logTextArea.appendText(line + "\n");
                        }
                        Thread.sleep(50);
                    }
                }
            };
            Task<Void> writeLog2 = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    while (true) {
                        String line = reader2.readLine();
                        if(line != null) {
                            logTextArea.appendText(line + "\n");
                        }
                        Thread.sleep(50);
                    }
                }
            };
            new Thread(writeLog).start();
            new Thread(writeLog2).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
