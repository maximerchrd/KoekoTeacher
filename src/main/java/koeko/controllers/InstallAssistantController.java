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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String bal =  System.getProperty("os.name");
        if (System.getProperty("os.name").contains("OS X")) {
            instructionsLabel.setText("Will you connect all the students to a router or do you want to setup a hotspot wifi with" +
                    " your computer?");
            option1Button.setText("I will use a Router");
            option1Button.setOnAction(e -> {
                useRouter();
            });
            option2Button.setText("I will use a Hotspot");
            option2Button.setOnAction(e -> {
                useHotspot();
            });
        } else {
            instructionsLabel.setText("Sorry, the assistant doesn't support your Operating System (OS) :-(");
            option1Button.setVisible(false);
            option2Button.setVisible(false);
        }
    }

    private void useRouter() {
        option1Button.setVisible(false);
        option2Button.setVisible(false);
        instructionsLabel.setText("Great! Enjoy using Koeko!");
    }

    private void useHotspot() {
        instructionsLabel.setText("Do you want the assistant to help you setting up the hotspot? (You will need an admin password)");
        option1Button.setText("No thanks, I'll do it later.");
        option1Button.setOnAction(e -> {
            useRouter();
        });
        option2Button.setText("All right, please proceed.");
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
        instructionsLabel.setText("Once this step is finished, press the button bellow.");
        option1Button.setVisible(false);
        option2Button.setText("Done");
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
        instructionsLabel.setText("Once this step is finished, press the button bellow.");
        option1Button.setVisible(false);
        option2Button.setText("Done");
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
        instructionsLabel.setText("Your Hotspot should be configured now. To start it, go to \n->\"System Preferences\" \n-> \"Sharing\" \n-> " +
                "select the row \"Internet Sharing\", \n-> share your connection from: \"Loopback\" \n-> To computers using \"Wi-Fi\" \n-> " +
                "setup the wifi network in \"Wi-Fi Options\"(choose a WPA2 security) \n-> start internet sharing by ticking the box." +
                "\nEnjoy!");
        option2Button.setVisible(false);
    }
}
