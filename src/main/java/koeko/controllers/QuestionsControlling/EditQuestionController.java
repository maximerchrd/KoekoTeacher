package koeko.controllers.QuestionsControlling;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.Tools.FilesHandler;
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
public class EditQuestionController implements Initializable {
    private ArrayList<HBox> hBoxArrayList;
    private ArrayList<CheckBox> checkBoxArrayList;
    private ArrayList<TextField> textFieldArrayList;
    private ArrayList<ComboBox> subjectsComboBoxArrayList;
    private ArrayList<ComboBox> objectivesComboBoxArrayList;
    private List<QuestionGeneric> genericQuestionsList;
    private TreeView<QuestionGeneric> allQuestionsTree;
    private QuestionGeneric questionGeneric;
    private int timerSecondsInt = 0;
    private TreeItem treeItem;
    private TreeView treeView;
    private int buttonImageSize = 20;

    @FXML private VBox vBox;
    @FXML private VBox vBoxSubjects;
    @FXML private VBox vBoxObjectives;
    @FXML private ComboBox typeOfQuestion;
    @FXML private TextArea questionText;
    @FXML private TextField imagePath;
    @FXML private ComboBox correctionComboBox;
    @FXML private TextArea customCorrection;
    @FXML private TextField timerSeconds;

    private ResourceBundle bundle;

    public void initVariables(List<QuestionGeneric> argGenericQuestionsList, TreeView<QuestionGeneric> argAllQuestionsTree,
                              QuestionGeneric questionGeneric, TreeItem treeItem, TreeView treeView) {
        genericQuestionsList = argGenericQuestionsList;
        allQuestionsTree = argAllQuestionsTree;
        this.questionGeneric = questionGeneric;
        this.treeItem = treeItem;
        this.treeView = treeView;
        fillFields();
    }

    public void addAnswerOption() {
        addAnswerOption("", false);
    }
    private void addAnswerOption(String option, Boolean isChecked) {
        QuestionEditing.addAnswerOption(option, typeOfQuestion, bundle, hBoxArrayList, vBox);
    }

    public void addSubject() {
        addSubject("");
    }
    private void addSubject(String subject) {
        QuestionEditing.addSubject(subject, subjectsComboBoxArrayList, buttonImageSize, vBoxSubjects);
    }

    public void addObjective() {
        addObjective("");
    }
    public void addObjective(String objective) {
        QuestionEditing.addObjective(objective, objectivesComboBoxArrayList, buttonImageSize, vBoxObjectives);
    }

    public void comboAction() {
        if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().contentEquals(bundle.getString("string.shrtaq"))) {
            for (int i = 0; i < hBoxArrayList.size(); i++) {
                hBoxArrayList.get(i).getChildren().get(0).setVisible(false);
            }

            //fill the correction mode combobox
            ObservableList<String> correctionModes =
                    FXCollections.observableArrayList("Cannot change Correction Mode");
            correctionComboBox.setItems(correctionModes);
            correctionComboBox.getSelectionModel().select(0);
        } else {
            for (int i = 0; i < hBoxArrayList.size(); i++) {
                hBoxArrayList.get(i).getChildren().get(0).setVisible(true);
            }

            //fill the correction mode combobox
            ObservableList<String> correctionModes =
                    FXCollections.observableArrayList(bundle.getString("string.all_or_nothing"), bundle.getString("string.proportion"),
                            bundle.getString("string.custom"));
            correctionComboBox.setItems(correctionModes);
            correctionComboBox.getSelectionModel().select(0);
        }
    }

    public void correctionModeChanged() {
        if (correctionComboBox.getSelectionModel().getSelectedItem() != null &&
                correctionComboBox.getSelectionModel().getSelectedItem().toString().contains("Custom")) {
            customCorrection.setVisible(true);
        } else {
            customCorrection.setVisible(false);
        }
    }

    public void addPicture(String path) {
        imagePath.setText(path);
    }
    public void addPicture() {
        QuestionEditing.addPicture(vBox, imagePath);
    }

    public void saveQuestion() {
        for (int i = 0; i < subjectsComboBoxArrayList.size(); i++) {
            try {
                DbTableSubject.addSubject(subjectsComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString().replace("'","''"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        for (int i = 0; i < objectivesComboBoxArrayList.size(); i++) {
            try {
                DbTableLearningObjectives.addObjective(objectivesComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString().replace("'","''"),1);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        //add question to database according to question type
        if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().equals(bundle.getString("string.shrtaq"))) {
            QuestionShortAnswer new_questshortanswer = new QuestionShortAnswer();
            new_questshortanswer.setQUESTION(questionText.getText().replace("'","''"));
            if (imagePath.getText().length() > 0) {
                new_questshortanswer.setIMAGE(imagePath.getText());
            }
            ArrayList<String> answerOptions = new ArrayList<String>();
            for (int i = 0; i < hBoxArrayList.size(); i++) {
                TextField textField = (TextField)  hBoxArrayList.get(i).getChildren().get(1);
                String answerOption = textField.getText();
                if (answerOption.length() > 0) {
                    answerOptions.add(answerOption);
                }
            }
            new_questshortanswer.setANSWER(answerOptions);
            new_questshortanswer.setID(questionGeneric.getGlobalID());
            try {
                new_questshortanswer.setTimerSeconds(Integer.valueOf(timerSeconds.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Incorrect format for timer seconds");
                new_questshortanswer.setTimerSeconds(timerSecondsInt);
            }
            try {
                DbTableQuestionShortAnswer.updateShortAnswerQuestion(new_questshortanswer);
                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().unsyncIdAfterUpdate(new_questshortanswer.getID());
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            //update question in treeView
            questionGeneric.setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);
            questionGeneric.setQuestion(new_questshortanswer.getQUESTION());
            questionGeneric.setImagePath(new_questshortanswer.getIMAGE());
            Node questionImage = null;
            questionImage = new ImageView(new Image("file:" + new_questshortanswer.getIMAGE(), 20, 20, true, false));
            if (new_questshortanswer.getIMAGE().length() < 1 || new_questshortanswer.getIMAGE().contentEquals("none")) {
                treeItem.setValue(questionGeneric);
            } else {
                treeItem.setValue(questionGeneric);
                treeItem.setGraphic(questionImage);
            }
            treeView.refresh();

            DbTableRelationQuestionSubject.removeRelationsWithQuestion(questionGeneric.getGlobalID());
            for (int i = 0; i < subjectsComboBoxArrayList.size(); i++) {
                try {
                    DbTableRelationQuestionSubject.addRelationQuestionSubject(questionGeneric.getGlobalID(),subjectsComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString().replace("'","''"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            DbTableRelationQuestionObjective.removeRelationsWithQuestion(questionGeneric.getGlobalID());
            for (int i = 0; i < objectivesComboBoxArrayList.size(); i++) {
                try {
                    DbTableRelationQuestionObjective.addRelationQuestionObjective(questionGeneric.getGlobalID(),objectivesComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString().replace("'","''"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } else if (typeOfQuestion.getSelectionModel().getSelectedItem().toString().equals(bundle.getString("string.qmc"))) {
            Vector<String> options_vector = new Vector<String>();
            for (int i = 0; i < 10; i++) options_vector.add(" ");
            for (int i = 0; i < 10 && i < hBoxArrayList.size() && !((TextField) hBoxArrayList.get(i).getChildren().get(1)).getText().contentEquals(" "); i++) {
                options_vector.set(i,((TextField) hBoxArrayList.get(i).getChildren().get(1)).getText());
            }
            int number_correct_answers = 0;
            String temp_option;
            for (int i = 0; i < hBoxArrayList.size(); i++) {
                CheckBox checkBox = (CheckBox) hBoxArrayList.get(i).getChildren().get(0);
                if (checkBox.isSelected()) {
                    temp_option = options_vector.get(number_correct_answers);
                    options_vector.set(number_correct_answers,options_vector.get(i));
                    options_vector.set(i,temp_option);
                    number_correct_answers++;
                }
            }
            QuestionMultipleChoice new_questmultchoice = new QuestionMultipleChoice("1", questionText.getText(), options_vector.get(0),
                    options_vector.get(1), options_vector.get(2), options_vector.get(3), options_vector.get(4),
                    options_vector.get(5), options_vector.get(6), options_vector.get(7), options_vector.get(8),
                    options_vector.get(9), imagePath.getText());
            new_questmultchoice.setNB_CORRECT_ANS(number_correct_answers);
            new_questmultchoice.setID(questionGeneric.getGlobalID());
            try {
                new_questmultchoice.setTimerSeconds(Integer.valueOf(timerSeconds.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Incorrect format for timer seconds");
                new_questmultchoice.setTimerSeconds(timerSecondsInt);
            }

            try {
                DbTableQuestionMultipleChoice.updateMultipleChoiceQuestion(new_questmultchoice);
                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().unsyncIdAfterUpdate(new_questmultchoice.getID());
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            //update question in treeView
            questionGeneric.setIntTypeOfQuestion(QuestionGeneric.MCQ);
            questionGeneric.setQuestion(new_questmultchoice.getQUESTION());
            questionGeneric.setImagePath(new_questmultchoice.getIMAGE());
            Node questionImage = null;
            questionImage = new ImageView(new Image("file:" + new_questmultchoice.getIMAGE(), 20, 20, true, false));
            if (new_questmultchoice.getIMAGE().length() < 1 || new_questmultchoice.getIMAGE().contentEquals("none")) {
                treeItem.setValue(questionGeneric);
            } else {
                treeItem.setValue(questionGeneric);
                treeItem.setGraphic(questionImage);
            }
            treeView.refresh();

            DbTableRelationQuestionSubject.removeRelationsWithQuestion(questionGeneric.getGlobalID());
            for (int i = 0; i < subjectsComboBoxArrayList.size(); i++) {
                try {
                    DbTableRelationQuestionSubject.addRelationQuestionSubject(questionGeneric.getGlobalID(),subjectsComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString().replace("'","''"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            DbTableRelationQuestionObjective.removeRelationsWithQuestion(questionGeneric.getGlobalID());
            for (int i = 0; i < objectivesComboBoxArrayList.size(); i++) {
                try {
                    DbTableRelationQuestionObjective.addRelationQuestionObjective(questionGeneric.getGlobalID(),objectivesComboBoxArrayList.get(i).getSelectionModel().getSelectedItem().toString().replace("'","''"));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            //set the correction type
            if (correctionComboBox.getSelectionModel().getSelectedIndex() == 0) {
                DbTableQuestionMultipleChoice.setCorrectionMode("AllOrNothing", questionGeneric.getGlobalID());
            } else if (correctionComboBox.getSelectionModel().getSelectedIndex() == 1) {
                DbTableQuestionMultipleChoice.setCorrectionMode("PercentCorrectDecisions", questionGeneric.getGlobalID());
            } else if (correctionComboBox.getSelectionModel().getSelectedIndex() == 2) {
                DbTableQuestionMultipleChoice.setCorrectionMode("Custom#" + customCorrection.getText(), questionGeneric.getGlobalID());
            }
        } else {
            System.out.println("Problem saving question: question type not supported");
        }
        Stage stage = (Stage) vBox.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    private void fillFields() {
        //fill the corresponding fields
        if (questionGeneric.getIntTypeOfQuestion() == QuestionGeneric.MCQ) {
            typeOfQuestion.getSelectionModel().select(0);
            QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
            try {
                questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionGeneric.getGlobalID());
            } catch (Exception e) {
                e.printStackTrace();
            }
            questionText.setText(questionMultipleChoice.getQUESTION());
            for (int i = 0; i < questionMultipleChoice.getAnswers().size(); i++) {
                if (i < questionMultipleChoice.getNB_CORRECT_ANS()) {
                    addAnswerOption(questionMultipleChoice.getAnswers().get(i), true);
                } else {
                    addAnswerOption(questionMultipleChoice.getAnswers().get(i), false);
                }
            }
            addPicture(questionMultipleChoice.getIMAGE());
            for (int i = 0; i < questionMultipleChoice.getSubjects().size(); i++) {
                addSubject(questionMultipleChoice.getSubjects().get(i));
            }
            for (int i = 0; i < questionMultipleChoice.getObjectives().size(); i++) {
                addObjective(questionMultipleChoice.getObjectives().get(i));
            }

            //fill the correction mode combobox
            ObservableList<String> correctionModes =
                    FXCollections.observableArrayList(bundle.getString("string.all_or_nothing"), bundle.getString("string.proportion"),
                            bundle.getString("string.custom"));
            correctionComboBox.setItems(correctionModes);
            String questionCorrectionMode = DbTableQuestionMultipleChoice.getCorrectionMode(questionMultipleChoice.getID());
            if (questionCorrectionMode.contentEquals("AllOrNothing") || questionCorrectionMode.contentEquals("")) {
                correctionComboBox.getSelectionModel().select(0);
            } else if (questionCorrectionMode.contentEquals("PercentCorrectDecisions")) {
                correctionComboBox.getSelectionModel().select(1);
            } else if (questionCorrectionMode.contains("Custom")) {
                correctionComboBox.getSelectionModel().select(2);
                customCorrection.setVisible(true);
                customCorrection.setText(DbTableQuestionMultipleChoice.getCorrectionMode(questionGeneric.getGlobalID()).replace("Custom#", ""));
            }

            //fill timer seconds
            timerSeconds.setText(String.valueOf(questionMultipleChoice.getTimerSeconds()));
            timerSecondsInt = questionMultipleChoice.getTimerSeconds();
        } else {
            typeOfQuestion.getSelectionModel().select(1);
            QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionGeneric.getGlobalID());
            questionText.setText(questionShortAnswer.getQUESTION());
            for (int i = 0; i < questionShortAnswer.getANSWER().size(); i++) {
                addAnswerOption(questionShortAnswer.getANSWER().get(i),false);
            }
            addPicture(questionShortAnswer.getIMAGE());
            for (int i = 0; i < questionShortAnswer.getSubjects().size(); i++) {
                addSubject(questionShortAnswer.getSubjects().get(i));
            }
            for (int i = 0; i < questionShortAnswer.getObjectives().size(); i++) {
                addObjective(questionShortAnswer.getObjectives().get(i));
            }

            //fill the correction mode combobox
            ObservableList<String> correctionModes =
                    FXCollections.observableArrayList("Cannot change Correction Mode");
            correctionComboBox.setItems(correctionModes);
            correctionComboBox.getSelectionModel().select(0);

            //fill timer seconds
            timerSeconds.setText(String.valueOf(questionShortAnswer.getTimerSeconds()));
            timerSecondsInt = questionShortAnswer.getTimerSeconds();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        subjectsComboBoxArrayList = new ArrayList<>();
        objectivesComboBoxArrayList = new ArrayList<>();
        hBoxArrayList = new ArrayList<>();
        ObservableList<String> options =
                FXCollections.observableArrayList(bundle.getString("string.qmc"),
                        bundle.getString("string.shrtaq"));
        typeOfQuestion.setItems(options);
        typeOfQuestion.getSelectionModel().selectFirst();
        questionText.setWrapText(true);
    }
}
