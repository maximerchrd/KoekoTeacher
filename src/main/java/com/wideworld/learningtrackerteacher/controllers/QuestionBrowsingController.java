package com.wideworld.learningtrackerteacher.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

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
                Platform.runLater(() -> labelIP.setText("studentsConnected should connect \nto the following address: " + ip_address[0]));
                return null;
            }
        };
        new Thread(getIPTask).start();
    }

    public void refreshIP() {
        final String[] ip_address = {""};
        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ip_address[0] = InetAddress.getLocalHost().getHostAddress();
                Platform.runLater(() -> labelIP.setText("studentsConnected should connect \nto the following address: " + ip_address[0]));
                return null;
            }
        };
        new Thread(getIPTask).start();
    }
}
