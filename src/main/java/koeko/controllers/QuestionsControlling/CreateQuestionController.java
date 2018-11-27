package koeko.controllers.QuestionsControlling;


import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.Tools.FilesHandler;
import koeko.functionalTesting;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import koeko.database_management.*;
import org.controlsfx.control.textfield.TextFields;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by maximerichard on 11.03.18.
 */
public class CreateQuestionController implements Initializable {
    private ArrayList<HBox> hBoxArrayList;
    private ArrayList<CheckBox> checkBoxArrayList;
    private ArrayList<TextField> textFieldArrayList;
    private ArrayList<ComboBox> subjectsComboBoxArrayList;
    private ArrayList<ComboBox> objectivesComboBoxArrayList;
    private List<QuestionGeneric> genericQuestionsList;
    private TreeView<QuestionGeneric> allQuestionsTree;
    private int buttonImageSize = 20;

    @FXML private VBox vBox;
    @FXML private VBox vBoxSubjects;
    @FXML private VBox vBoxObjectives;
    @FXML private ComboBox typeOfQuestion;
    @FXML private HBox firstAnswer;
    @FXML private TextArea questionText;
    @FXML private TextField imagePath;
    @FXML private TextField timerTextView;

    public void initVariables(List<QuestionGeneric> argGenericQuestionsList, TreeView<QuestionGeneric> argAllQuestionsTree) {
        genericQuestionsList = argGenericQuestionsList;
        allQuestionsTree = argAllQuestionsTree;
    }

    public void addAnswerOption() {
        HBox hBox = new HBox();
        TextField textField = new TextField("");

        CheckBox checkBox = new CheckBox();
        if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().contentEquals("Question with Short Answer")) {
            checkBox.setVisible(false);
        }
        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            hBoxArrayList.remove(hBox);
            (( VBox)hBox.getParent()).getChildren().remove(hBox);
        });
        hBox.getChildren().add(checkBox);
        hBox.getChildren().add(textField);
        hBox.getChildren().add(removeButton);
        vBox.getChildren().add(vBox.getChildren().size() - 2, hBox);
        hBoxArrayList.add(hBox);
    }

    public void removeAnswerOption() {
        hBoxArrayList.remove(firstAnswer);
        vBox.getChildren().removeAll(firstAnswer);
    }

    public void addSubject() {
        Vector<String> subjectsVector = DbTableSubject.getAllSubjectsAsStrings();
        String[] subjects = subjectsVector.toArray(new String[subjectsVector.size()]);;
        ObservableList<String> options =
                FXCollections.observableArrayList(subjects);
        ComboBox comboBox = new ComboBox(options);
        comboBox.setEditable(true);
        subjectsComboBoxArrayList.add(comboBox);

        HBox hBox = new HBox();
        Button editButton = new Button();
        ImageView editImage = new ImageView(new Image("/drawable/editImage.png", buttonImageSize, buttonImageSize, true, true));
        editButton.setGraphic(editImage);
        editButton.setOnAction(event -> {
            if (comboBox.getSelectionModel().getSelectedItem() != null) {
                QuestionEditing.promptEditSubject(comboBox.getSelectionModel().getSelectedItem().toString(), comboBox);
            }
        });

        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            subjectsComboBoxArrayList.remove(comboBox);
            (( VBox)hBox.getParent()).getChildren().remove(hBox);
        });
        hBox.getChildren().addAll(comboBox, editButton, removeButton);
        vBoxSubjects.getChildren().add(hBox);
        TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());
    }

    public void addObjective() {
        Vector<String> objectivessVector = DbTableLearningObjectives.getAllObjectives();
        String[] objectives = objectivessVector.toArray(new String[objectivessVector.size()]);;
        ObservableList<String> options =
                FXCollections.observableArrayList(objectives);
        ComboBox comboBox = new ComboBox(options);
        comboBox.setEditable(true);
        objectivesComboBoxArrayList.add(comboBox);

        HBox hBox = new HBox();

        Button editButton = new Button();
        ImageView editImage = new ImageView(new Image("/drawable/editImage.png", buttonImageSize, buttonImageSize, true, true));
        editButton.setGraphic(editImage);
        editButton.setOnAction(event -> {
            if (comboBox.getSelectionModel().getSelectedItem() != null) {
                QuestionEditing.promptEditObjective(comboBox.getSelectionModel().getSelectedItem().toString(), comboBox);
            }
        });

        Button removeButton = new Button("X");
        removeButton.setOnAction(event -> {
            objectivesComboBoxArrayList.remove(comboBox);
            (( VBox)hBox.getParent()).getChildren().remove(hBox);
        });
        hBox.getChildren().addAll(comboBox, editButton, removeButton);
        vBoxObjectives.getChildren().add(hBox);
        TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());
    }

    public void comboAction() {
        if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().contentEquals("Question with Short Answer")) {
            for (int i = 0; i < hBoxArrayList.size(); i++) {
                hBoxArrayList.get(i).getChildren().get(0).setVisible(false);
            }
        } else {
            for (int i = 0; i < hBoxArrayList.size(); i++) {
                hBoxArrayList.get(i).getChildren().get(0).setVisible(true);
            }
        }
    }

    public void addPicture() {
        File theDir = new File(FilesHandler.mediaDirectoryNoSlash);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                System.out.println("DIR created");
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image file");
        Stage stage = (Stage) vBox.getScene().getWindow();
        File source_file = fileChooser.showOpenDialog(stage);
        File dest_file = new File(FilesHandler.mediaDirectory + source_file.getName());
        File hashedFileName = new File(FilesHandler.mediaDirectory + source_file.getName());
        try {
            Files.copy(source_file.toPath(), dest_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = Files.readAllBytes(dest_file.toPath());
            messageDigest.update(hashedBytes);
            String encryptedString = DatatypeConverter.printHexBinary(messageDigest.digest());
            if (encryptedString.length() > 14) {
                encryptedString = encryptedString.substring(0, 14);
            }
            hashedFileName = new File(FilesHandler.mediaDirectory + encryptedString);
            Files.move(dest_file.toPath(), hashedFileName.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        imagePath.setText(hashedFileName.getName());
        imagePath.setEditable(false);
    }

    public void saveQuestion() {
        if (functionalTesting.testMode == false) {
            ArrayList<String> subjects = new ArrayList<>();
            ArrayList<String> objectives = new ArrayList<>();
            QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
            QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
            int questionType = -1;

            Integer timerSeconds = null;
            try {
                timerSeconds = Integer.valueOf(timerTextView.getText());
            } catch (NumberFormatException e) {
                System.out.println("timerSeconds not an int: setting to infinite");
            }

            for (int i = 0; i < subjectsComboBoxArrayList.size(); i++) {
                subjects.add(subjectsComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString());
            }

            for (int i = 0; i < objectivesComboBoxArrayList.size(); i++) {
                try {
                    objectives.add(objectivesComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().equals("Question with Short Answer")) {
                questionType = 1;
                questionShortAnswer.setQUESTION(questionText.getText());
                if (imagePath.getText().length() > 0) {
                    questionShortAnswer.setIMAGE(imagePath.getText());
                }
                if (timerSeconds != null) {
                    questionShortAnswer.setTimerSeconds(timerSeconds);
                } else {
                    questionShortAnswer.setTimerSeconds(-1);
                }

                ArrayList<String> answerOptions = new ArrayList<String>();
                for (int i = 0; i < hBoxArrayList.size(); i++) {
                    TextField textField = (TextField) hBoxArrayList.get(i).getChildren().get(1);
                    String answerOption = textField.getText();
                    if (answerOption.length() > 0) {
                        answerOptions.add(answerOption);
                    }
                }
                questionShortAnswer.setANSWER(answerOptions);
            } else if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().equals("Question Multiple Choice")) {
                questionType = 0;

                Vector<String> options_vector = new Vector<String>();
                for (int i = 0; i < 10; i++) options_vector.add(" ");
                for (int i = 0; i < 10 && i < hBoxArrayList.size() && !((TextField) hBoxArrayList.get(i).getChildren().get(1)).getText().contentEquals(" "); i++) {
                    options_vector.set(i, ((TextField) hBoxArrayList.get(i).getChildren().get(1)).getText());
                }
                int number_correct_answers = 0;
                String temp_option;
                for (int i = 0; i < hBoxArrayList.size(); i++) {
                    CheckBox checkBox = (CheckBox) hBoxArrayList.get(i).getChildren().get(0);
                    if (checkBox.isSelected()) {
                        temp_option = options_vector.get(number_correct_answers);
                        options_vector.set(number_correct_answers, options_vector.get(i));
                        options_vector.set(i, temp_option);
                        number_correct_answers++;
                    }
                }
                questionMultipleChoice = new QuestionMultipleChoice("1", questionText.getText(), options_vector.get(0),
                        options_vector.get(1), options_vector.get(2), options_vector.get(3), options_vector.get(4),
                        options_vector.get(5), options_vector.get(6), options_vector.get(7), options_vector.get(8),
                        options_vector.get(9), imagePath.getText());
                questionMultipleChoice.setNB_CORRECT_ANS(number_correct_answers);
                if (timerSeconds != null) {
                    questionMultipleChoice.setTimerSeconds(timerSeconds);
                } else {
                    questionMultipleChoice.setTimerSeconds(-1);
                }
            }

            saveQuestion(subjects, objectives, questionType, questionMultipleChoice, questionShortAnswer);

        } else if (functionalTesting.testMode && functionalTesting.testCodeGlobal == 5) {
            Vector<String> questionids = DbTableQuestionGeneric.getAllGenericQuestionsIds();
            for (int i = 0; i < functionalTesting.nbCopiesGlobal; i++) {
                for (String questionId : questionids) {
                    QuestionMultipleChoice questionMultipleChoicetest = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionId);
                    QuestionShortAnswer questionShortAnswertest = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionId);
                    int questionTypetest = 0;
                    if (!questionShortAnswertest.getQUESTION().contentEquals("")) {
                        questionTypetest = 1;
                        questionShortAnswertest.setQUESTION("Copy " + i + " " + questionShortAnswertest.getQUESTION());
                    } else {
                        questionMultipleChoicetest.setQUESTION("Copy " + i + " " + questionMultipleChoicetest.getQUESTION());
                    }
                    saveQuestion(new ArrayList<String>(), new ArrayList<String>(),questionTypetest,questionMultipleChoicetest,questionShortAnswertest);
                }
            }
        }
    }

    public void saveQuestion(ArrayList<String> subjects, ArrayList<String> objectives, int questionType, QuestionMultipleChoice questionMultipleChoice,
                             QuestionShortAnswer questionShortAnswer) {


        for (String subject : subjects) {
            DbTableSubject.addSubject(subject);
        }

        for (String objective : objectives) {
            DbTableLearningObjectives.addObjective(objective, -1);
        }

        //add question to database according to question type
        if (questionType == 1) {
            String idGlobal = "-1";
            idGlobal = DbTableQuestionShortAnswer.addShortAnswerQuestion(questionShortAnswer);
            questionShortAnswer.setID(idGlobal);

            //put the question in the treeView
            QuestionGeneric questionGeneric = new QuestionGeneric(1, questionShortAnswer.getID());
            questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
            questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
            questionGeneric.setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);
            genericQuestionsList.add(questionGeneric);
            Node questionImage = null;
            questionImage = new ImageView(new Image("file:" + questionShortAnswer.getIMAGE(), 20, 20, true, false));
            TreeItem<QuestionGeneric> itemChild;
            if (questionShortAnswer.getIMAGE().length() < 1) {
                itemChild = new TreeItem<>(questionGeneric);
            } else {
                itemChild = new TreeItem<>(questionGeneric, questionImage);
            }
            allQuestionsTree.getRoot().getChildren().add(itemChild);

            for (String subject : subjects) {
                DbTableRelationQuestionSubject.addRelationQuestionSubject(subject);
            }
            for (String objective : objectives) {
                DbTableRelationQuestionObjective.addRelationQuestionObjective(objective);
            }
        } else if (questionType == 0) {
            try {
                DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(questionMultipleChoice);
                questionMultipleChoice.setID(DbTableQuestionMultipleChoice.getLastIDGlobal());

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            //insert question in tree view
            QuestionGeneric questionGeneric = new QuestionGeneric(0, questionMultipleChoice.getID());
            questionGeneric.setQuestion(questionMultipleChoice.getQUESTION());
            questionGeneric.setImagePath(questionMultipleChoice.getIMAGE());
            questionGeneric.setIntTypeOfQuestion(QuestionGeneric.MCQ);
            genericQuestionsList.add(questionGeneric);
            Node questionImage = null;
            questionImage = new ImageView(new Image("file:" + questionMultipleChoice.getIMAGE(), 20, 20, true, false));
            TreeItem<QuestionGeneric> itemChild;
            if (questionMultipleChoice.getIMAGE().length() < 1) {
                itemChild = new TreeItem<>(questionGeneric);
            } else {
                itemChild = new TreeItem<>(questionGeneric, questionImage);
            }
            allQuestionsTree.getRoot().getChildren().add(itemChild);

            for (String subject : subjects) {
                DbTableRelationQuestionSubject.addRelationQuestionSubject(subject);
            }
            for (String objective : objectives) {
                DbTableRelationQuestionObjective.addRelationQuestionObjective(objective);
            }
        } else {
            System.out.println("Problem saving question: question type not supported");
        }
        Stage stage = (Stage) vBox.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        subjectsComboBoxArrayList = new ArrayList<>();
        objectivesComboBoxArrayList = new ArrayList<>();
        hBoxArrayList = new ArrayList<>();
        ObservableList<String> options =
                FXCollections.observableArrayList("Question Multiple Choice", "Question with Short Answer");
        typeOfQuestion.setItems(options);
        typeOfQuestion.getSelectionModel().selectFirst();
        hBoxArrayList.add(firstAnswer);
    }
}
