package koeko.controllers;

import koeko.database_management.DbTableQuestionGeneric;
import koeko.database_management.DbTableQuestionMultipleChoice;
import koeko.database_management.DbTableSettings;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.QuestionMultipleChoice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class SettingsController implements Initializable {

    static public Integer nearbyMode = -1;
    static public Integer correctionMode = -1;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TextField teacherName;
    @FXML
    private ToggleButton nearbyModeButton;
    @FXML
    private ToggleButton correctionModeButton;

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

    public void sendToWebsite() {
        try {
            String url = "http://localhost:8080/post-mcq";

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);

            // add header
            post.setHeader("User-Agent", USER_AGENT);

            ArrayList<QuestionGeneric> questionGenericArrayList = DbTableQuestionGeneric.getAllGenericQuestions();

            for (QuestionGeneric questionGeneric : questionGenericArrayList) {
                postQuestionMultipleChoice(questionGeneric, client, post);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postQuestionMultipleChoice(QuestionGeneric questionGeneric, HttpClient client, HttpPost post) throws Exception {
        List<NameValuePair> urlParameters = new ArrayList<>();
        if (questionGeneric.getIntTypeOfQuestion() == 0) {
            QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionGeneric.getGlobalID());
            urlParameters.add(new BasicNameValuePair("question_text", questionMultipleChoice.getQUESTION()));
            Vector<String> answers = questionMultipleChoice.getAnswers();
            for (String answer : answers) {
                urlParameters.add(new BasicNameValuePair("question_answer", answer));
            }
            urlParameters.add(new BasicNameValuePair("question_type", "question multiple choice"));


            File file = new File(questionMultipleChoice.getIMAGE());
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            String encodedImage = new String(Base64.encodeBase64(bytes), "UTF-8");
            urlParameters.add(new BasicNameValuePair("question_image", encodedImage));
            urlParameters.add(new BasicNameValuePair("question_image_name", questionMultipleChoice.getIMAGE().split("/")[1]));
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));


        HttpResponse response = client.execute(post);

        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
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

        /*try {
            String outFile = "LT_" + LocalDateTime.now().toString() + ".out";
            outFile = outFile.replace(":", "-");
            String errFile = "LT_" + LocalDateTime.now().toString() + ".err";
            errFile = errFile.replace(":", "-");
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)), true));
            System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(errFile)), true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }
}
