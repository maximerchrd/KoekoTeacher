package com.wideworld.learningtrackerteacher.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TextArea logTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
