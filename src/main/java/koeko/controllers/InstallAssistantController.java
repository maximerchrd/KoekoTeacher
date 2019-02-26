package koeko.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InstallAssistantController implements Initializable {

    @FXML private Label instructionsLabel;
    @FXML private Button option1Button;
    @FXML private Button option2Button;

    ResourceBundle bundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        if (System.getProperty("os.name").contains("OS X")) {
            instructionsLabel.setText(bundle.getString("installer.question1"));
            option1Button.setText(bundle.getString("installer.answer1a"));
            option1Button.setOnAction(e -> {
                useRouter();
            });
            option2Button.setText(bundle.getString("installer.answer1b"));
            option2Button.setOnAction(e -> {
                useHotspot();
            });
        }
    }

    private void useRouter() {
        option1Button.setVisible(false);
        option2Button.setVisible(false);
        instructionsLabel.setText(bundle.getString("installer.instruction1"));
    }

    private void useHotspot() {
        instructionsLabel.setText(bundle.getString("installer.question2"));
        option1Button.setText(bundle.getString("installer.answer2a"));
        option1Button.setOnAction(e -> {
            useRouter();
        });
        option2Button.setText(bundle.getString("installer.answer2b"));
        option2Button.setOnAction(e -> {
            setupHotspotMac1();
        });
    }

    private void setupHotspotMac1() {
        try {
            String[] cmdline = { "sh", "-c", "echo 'sudo networksetup -createnetworkservice Loopback lo0' > tmp.sh" };
            Runtime.getRuntime().exec(cmdline);
            Runtime.getRuntime().exec("chmod +x tmp.sh");
            String[] args = new String[] {"sh", "-c", "/usr/bin/open -a Terminal tmp.sh"};
            Process p = new ProcessBuilder(args).start();
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        instructionsLabel.setText(bundle.getString("installer.instruction2"));
        option1Button.setVisible(false);
        option2Button.setText(bundle.getString("installer.done"));
        option2Button.setOnAction(e -> {
            setupHotspotMac2();
        });
    }
    private void setupHotspotMac2() {
        try {
            String[] cmdline = { "sh", "-c", "echo 'sudo networksetup -setmanual Loopback 172.20.42.42 255.255.255.255' > tmp.sh" };
            Runtime.getRuntime().exec(cmdline);
            Runtime.getRuntime().exec("chmod +x tmp.sh");
            String[] args = new String[] {"sh", "-c", "/usr/bin/open -a Terminal tmp.sh"};
            Process p = new ProcessBuilder(args).start();
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        instructionsLabel.setText(bundle.getString("installer.instruction2"));
        option1Button.setVisible(false);
        option2Button.setText(bundle.getString("installer.done"));
        option2Button.setOnAction(e -> {
            setupHotspotMac3();
        });
    }
    private void setupHotspotMac3() {
        try {
            Runtime.getRuntime().exec("rm tmp.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
        instructionsLabel.setText(bundle.getString("installer.instruction3"));
        option2Button.setVisible(false);
    }
}
