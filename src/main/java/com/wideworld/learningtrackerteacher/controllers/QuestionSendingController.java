package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.LearningTracker;
import com.wideworld.learningtrackerteacher.Networking.NetworkCommunication;
import com.wideworld.learningtrackerteacher.questions_management.Test;
import com.wideworld.learningtrackerteacher.database_management.*;
import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;
import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;
import com.wideworld.learningtrackerteacher.questions_management.QuestionShortAnswer;
import com.wideworld.learningtrackerteacher.students_management.Classroom;
import com.wideworld.learningtrackerteacher.students_management.Student;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.lang.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by maximerichard on 01.03.18.
 */
public class QuestionSendingController extends Window implements Initializable {
    //all questions tree (left panel)
    private QuestionGeneric draggedQuestion = null;
    private TreeItem<QuestionGeneric> root;
    private QuestionMultipleChoice questionMultChoiceSelectedNodeTreeFrom;
    private QuestionShortAnswer questionShortAnswerSelectedNodeTreeFrom;
    private Test testSelectedNodeTreeFrom;
    private List<Test> testsList = new ArrayList<Test>();
    private List<QuestionGeneric> genericQuestionsList = new ArrayList<QuestionGeneric>();
    private List<QuestionGeneric> testsNodeList = new ArrayList<QuestionGeneric>();
    private String activeClass = "";
    private ContextMenu studentsContextMenu;
    static public Boolean readyToActivate = true;

    @FXML
    private TreeView<QuestionGeneric> allQuestionsTree;

    //questions ready for activation (right panel)
    static public Vector<String> IDsFromBroadcastedQuestions = new Vector<>();
    @FXML
    private ListView<QuestionGeneric> readyQuestionsList;

    @FXML private ComboBox groupsCombobox;

    public void initialize(URL location, ResourceBundle resources) {
        LearningTracker.questionSendingControllerSingleton = this;
        //all questions tree (left panel)
        //retrieve data from db
        try {
            genericQuestionsList = DbTableQuestionGeneric.getAllGenericQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        testsList = DbTableTests.getAllTests();
        for (Test test : testsList) {
            QuestionGeneric testGeneric = new QuestionGeneric();
            testGeneric.setGlobalID(-test.getIdTest());
            testGeneric.setQuestion(test.getTestName());
            testsNodeList.add(testGeneric);
        }
        //create root
        root = new TreeItem<>(new QuestionGeneric());
        root.setExpanded(true);
        allQuestionsTree.setShowRoot(false);
        populateTree(root);
        allQuestionsTree.setRoot(root);
        allQuestionsTree.setCellFactory(new Callback<TreeView<QuestionGeneric>, TreeCell<QuestionGeneric>>() {
            @Override
            public TreeCell<QuestionGeneric> call(TreeView<QuestionGeneric> stringTreeView) {
                TreeCell<QuestionGeneric> treeCell = new TreeCell<QuestionGeneric>() {
                    @Override
                    protected void updateItem(QuestionGeneric item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty && item != null) {
                            setText(item.getQuestion());
                            if (item.getGlobalID() > 0) {
                                setGraphic(getTreeItem().getGraphic());
                            } else {
                                setGraphic(new ImageView(new Image("/drawable/test.png", 30, 30, true, true)));
                            }
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };

                treeCell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if (treeCell.getTreeItem().getValue().getGlobalID() > 0) {
                            draggedQuestion = treeCell.getTreeItem().getValue();
                            Dragboard db = allQuestionsTree.startDragAndDrop(TransferMode.ANY);

                            /* Put a string on a dragboard */
                            ClipboardContent content = new ClipboardContent();
                            content.putString(treeCell.getText());
                            db.setContent(content);

                            mouseEvent.consume();
                        }
                    }
                });

                treeCell.setOnDragOver(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data is dragged over the target */
                        /* accept it only if it is not dragged from the same node
                         * and if it has a string data */
                        if (event.getGestureSource() != treeCell &&
                                event.getDragboard().hasString()) {
                            /* allow for both copying and moving, whatever user chooses */
                            event.acceptTransferModes(TransferMode.COPY);
                        }

                        event.consume();
                    }
                });

                treeCell.setOnDragEntered(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* the drag-and-drop gesture entered the target */
                        /* show to the user that it is an actual gesture target */
                        if (event.getGestureSource() != treeCell &&
                                event.getDragboard().hasString()) {
                            treeCell.setTextFill(Color.GREEN);
                        }
                        event.consume();
                    }
                });
                treeCell.setOnDragExited(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* mouse moved away, remove the graphical cues */
                        treeCell.setTextFill(Color.BLACK);
                        event.consume();
                    }
                });


                treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data dropped */
                        /* if there is a string data on dragboard, read it and use it */
                        if (treeCell.getTreeItem().getValue().getGlobalID() < 0) {
                            /*Dragboard db = event.getDragboard();
                            boolean success = false;
                            if (db.hasString()) {
                                treeCell.setText(db.getString());
                                success = true;
                            }
                            /* let the source know whether the string was successfully
                             * transferred and used */

                            //add a horizontal relation with the question before in the list
                            TreeItem<QuestionGeneric> questionBefore = treeCell.getTreeItem().getChildren().get(treeCell.getTreeItem().getChildren().size() - 1);
                            if (questionBefore != null) {
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                                        String.valueOf(draggedQuestion.getGlobalID()), treeCell.getTreeItem().getValue().getQuestion(), "");
                            }

                            //add the node to the tree
                            treeCell.getTreeItem().getChildren().add(new TreeItem<>(draggedQuestion));
                            DbTableRelationQuestionTest.addRelationQuestionTest(String.valueOf(draggedQuestion.getGlobalID()),
                                    treeCell.getTreeItem().getValue().getQuestion());
                            event.setDropCompleted(true);
                            treeCell.getTreeItem().setExpanded(true);
                            event.consume();
                        } else if (treeCell.getTreeItem().getChildren() != draggedQuestion) {
                            TreeItem<QuestionGeneric> treeItemTest = treeCell.getTreeItem();
                            while (treeItemTest.getParent() != root) {
                                treeItemTest = treeItemTest.getParent();
                            }
                            if (treeItemTest.getValue().getGlobalID() < 0) {
                                System.out.println("OK OK");

                                //add a horizontal relation with the question before in the list
                                TreeItem<QuestionGeneric> questionBefore = treeCell.getTreeItem().getChildren().get(treeCell.getTreeItem().getChildren().size() - 1);
                                if (questionBefore != null) {
                                    DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                                            String.valueOf(draggedQuestion.getGlobalID()), treeItemTest.getValue().getQuestion(), "");
                                }

                                //add the node to the tree and set the vertical relation
                                treeCell.getTreeItem().getChildren().add(new TreeItem<>(draggedQuestion));
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(treeCell.getTreeItem().getValue().getGlobalID()),
                                        String.valueOf(draggedQuestion.getGlobalID()), treeItemTest.getValue().getQuestion(), "EVALUATION<60");
                                event.setDropCompleted(true);
                                treeCell.getTreeItem().setExpanded(true);
                                event.consume();
                            }
                        } else {
                            System.out.println("Trying to drag on self or on question not belonging to any test");
                        }
                        draggedQuestion = null;
                    }
                });


                return treeCell;
            }
        });

        allQuestionsTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    broadcastQuestionForStudents();
                }
            }
        });

        //question ready (right panel)
        readyQuestionsList.setCellFactory(param -> new ListCell<QuestionGeneric>() {
            private ImageView imageView = new ImageView();

            public void updateItem(QuestionGeneric questionGeneric, boolean empty) {
                super.updateItem(questionGeneric, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(new Image("file:" + questionGeneric.getImagePath(), 40, 40, true, false));
                    setText(questionGeneric.getQuestion());
                    setGraphic(imageView);
                }
            }
        });

        studentsContextMenu = new ContextMenu();
        readyQuestionsList.setContextMenu(studentsContextMenu);
    }

    public void addSudentToContextMenu(Student student) {
        Boolean alreadyAdded = false;
        for (MenuItem menuItem : studentsContextMenu.getItems()) {
            if (menuItem.getText().contentEquals(student.getName())) {
                alreadyAdded = true;
            }
        }
        if (LearningTracker.studentGroupsAndClass.get(0) != null && !alreadyAdded) {
            MenuItem studentItem = new MenuItem(student.getName());
            studentItem.setOnAction(event -> {
                if (readyQuestionsList.getSelectionModel().getSelectedItem() != null) {
                    Integer questionID = readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID();
                    Vector<Student> singleStudent = new Vector<>();
                    singleStudent.add(student);
                    NetworkCommunication.networkCommunicationSingleton.SendQuestionID(questionID,singleStudent);
                }
            });
            studentsContextMenu.getItems().add(studentItem);
        }
    }


    private void populateTree(TreeItem<QuestionGeneric> root) {
        //populate tree
        for (QuestionGeneric testGeneric : testsNodeList) {
            TreeItem newTest = new TreeItem<>(testGeneric);
            root.getChildren().add(newTest);
            ArrayList<Integer> questionIDs = DbTableRelationQuestionTest.getQuestionIdsFromTestName(testGeneric.getQuestion());
            for (Integer id : questionIDs) {
                Boolean found = false;
                for (int i = 0; i < genericQuestionsList.size() && !found; i++) {
                    if (genericQuestionsList.get(i).getGlobalID() == id) {
                        found = true;
                        TreeItem questionItem = new TreeItem<>(genericQuestionsList.get(i));
                        newTest.getChildren().add(questionItem);

                        //add the questions linked to the test questions
                        populateWithLinkedQuestions(testGeneric, id, questionItem);
                    }
                }
            }
        }
        for (int i = 0; i < genericQuestionsList.size(); i++) {
            try {
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(genericQuestionsList.get(i).getGlobalID());
                Node questionImage = null;
                if (questionMultipleChoice.getQUESTION().length() < 1) {
                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(genericQuestionsList.get(i).getGlobalID());
                    genericQuestionsList.get(i).setQuestion(questionShortAnswer.getQUESTION());
                    genericQuestionsList.get(i).setImagePath(questionShortAnswer.getIMAGE());
                    genericQuestionsList.get(i).setTypeOfQuestion("1");
                    questionImage = new ImageView(new Image("file:" + questionShortAnswer.getIMAGE(), 20, 20, true, false));
                } else {
                    genericQuestionsList.get(i).setQuestion(questionMultipleChoice.getQUESTION());
                    if (questionMultipleChoice.getIMAGE().length() > 0) {
                        genericQuestionsList.get(i).setImagePath(questionMultipleChoice.getIMAGE());
                        genericQuestionsList.get(i).setTypeOfQuestion("0");
                        questionImage = new ImageView(new Image("file:" + questionMultipleChoice.getIMAGE(), 20, 20, true, false));
                    }
                }
                TreeItem<QuestionGeneric> itemChild;
                if (questionImage != null) {
                    itemChild = new TreeItem<>(genericQuestionsList.get(i), questionImage);
                } else {
                    itemChild = new TreeItem<>(genericQuestionsList.get(i));
                }
                root.getChildren().add(itemChild);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void populateWithLinkedQuestions(QuestionGeneric testGeneric, Integer id, TreeItem questionItem) {
        Vector<String> linkedQuestionsIds = DbTableRelationQuestionQuestion.getQuestionsLinkedToQuestion(String.valueOf(id),testGeneric.getQuestion());
        for (String questionID : linkedQuestionsIds) {
            if (DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(questionID) == 0) {
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(Integer.valueOf(questionID));
                QuestionGeneric questionGeneric = QuestionGeneric.mcqToQuestionGeneric(questionMultipleChoice);
                TreeItem questionChildren = new TreeItem<>(questionGeneric);
                questionItem.getChildren().add(questionChildren);
                Vector<String> linkedQuestionsIds2 = DbTableRelationQuestionQuestion.getQuestionsLinkedToQuestion(String.valueOf(questionID),testGeneric.getQuestion());
                if (linkedQuestionsIds2.size() > 0) {
                    populateWithLinkedQuestions(testGeneric,Integer.valueOf(questionID),questionChildren);
                }
            } else {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(Integer.valueOf(questionID));
                QuestionGeneric questionGeneric = QuestionGeneric.shrtaqToQuestionGeneric(questionShortAnswer);
                TreeItem questionChildren = new TreeItem<>(questionGeneric);
                questionItem.getChildren().add(questionChildren);
                Vector<String> linkedQuestionsIds2 = DbTableRelationQuestionQuestion.getQuestionsLinkedToQuestion(String.valueOf(questionID),testGeneric.getQuestion());
                if (linkedQuestionsIds2.size() > 0) {
                    populateWithLinkedQuestions(testGeneric,Integer.valueOf(questionID),questionChildren);
                }
            }
        }
    }

    //BUTTONS
    public void broadcastQuestionForStudents() {
        QuestionGeneric questionGeneric = allQuestionsTree.getSelectionModel().getSelectedItem().getValue();
        if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
            DbTableRelationClassQuestion.addClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(), String.valueOf(questionGeneric.getGlobalID()));
        }
        if (questionGeneric.getGlobalID() > 0) {
            sendQuestionToStudents(questionGeneric, groupsCombobox.getSelectionModel().getSelectedIndex(), true, false);
        } else if (questionGeneric.getGlobalID() < 0) {
            // send test infos and linked objectives
            sendTestToStudents(questionGeneric, groupsCombobox.getSelectionModel().getSelectedIndex());
        } else {
            System.out.println("Trying to broadcast question or test but ID == 0.");
        }
    }

    public void activateQuestionForStudents() {
        QuestionGeneric questionGeneric = readyQuestionsList.getSelectionModel().getSelectedItem();

        System.out.println("ready? " + QuestionSendingController.readyToActivate);
        if (!QuestionSendingController.readyToActivate) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/QuestionsNotReadyPopUp.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QuestionsNotReadyPopUpController controller = fxmlLoader.getController();
            controller.initParameters(questionGeneric);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Send anyway?");
            stage.setScene(new Scene(root1));
            stage.show();
        } else {
            if (questionGeneric.getGlobalID() > 0) {
                NetworkCommunication.networkCommunicationSingleton.SendQuestionID(questionGeneric.getGlobalID());
            } else {
                activateTestSynchroneousQuestions();
            }
         }
    }

    public void createQuestion() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateQuestion.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateQuestionController controller = fxmlLoader.getController();
        controller.initVariables(genericQuestionsList, allQuestionsTree);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Question");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void editQuestion() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditQuestion.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditQuestionController controller = fxmlLoader.getController();
        QuestionGeneric questionGeneric = allQuestionsTree.getSelectionModel().getSelectedItem().getValue();
        TreeItem selectedItem = allQuestionsTree.getSelectionModel().getSelectedItem();
        controller.initVariables(genericQuestionsList, allQuestionsTree, questionGeneric, selectedItem);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Edit Question");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void removeQuestion() {
        Integer group = groupsCombobox.getSelectionModel().getSelectedIndex();
        if (group < 1) {
            group = 0;
        }
        int index = readyQuestionsList.getSelectionModel().getSelectedIndex();
        //remove question from table
        if (readyQuestionsList.getSelectionModel().getSelectedItem() != null && readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID() > 0) {
            NetworkCommunication.networkCommunicationSingleton.removeQuestion(index);
        }
        if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
            //remove question - class/group relation
            DbTableRelationClassQuestion.removeClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(),
                    String.valueOf(LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().get(index)));
        }
        if (index >= 0) {
            LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().remove(index);
            readyQuestionsList.getItems().remove(index);
        }
    }
    public void removeQuestion(int index) {
        Integer group = groupsCombobox.getSelectionModel().getSelectedIndex();
        NetworkCommunication.networkCommunicationSingleton.removeQuestion(index);
        DbTableRelationClassQuestion.removeClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(),
                String.valueOf(LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().get(index)));
        LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().remove(index);
        readyQuestionsList.getItems().remove(index);
    }

    public void sendCorrection() {
        NetworkCommunication.networkCommunicationSingleton.SendCorrection(readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID());
    }

    public void importQuestions() {
        List<String> input = readFile("questions/questions.csv");
        input.remove(0);
        for (int i = 0; i < input.size(); i++) {
            String[] question = input.get(i).split(";");

            if (question.length >= 6) {
                //insert subjects
                String[] subjects = question[5].split("///");
                for (int j = 0; j < subjects.length; j++) {
                    try {
                        DbTableSubject.addSubject(subjects[j].replace("'", "''"));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                //insert objectives
                String[] objectives = question[6].split("///");
                for (int j = 0; j < objectives.length; j++) {
                    try {
                        DbTableLearningObjectives.addObjective(objectives[j].replace("'", "''"), 1);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                if (question[0].contentEquals("0")) {
                    insertQuestionMultipleChoice(question);
                } else {
                    insertQuestionShortAnswer(question);
                }

            } else {
                System.out.println("problem importing following question (missing fields)");
                for (String questionPart : question) {
                    System.out.println(questionPart);
                }
            }
        }
    }

    public void exportQuestions() {
        ArrayList<QuestionGeneric> questionGenericArrayList = new ArrayList<>();
        try {
            questionGenericArrayList = DbTableQuestionGeneric.getAllGenericQuestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("questions/questions.csv", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.println("Questions Type (0 = question multiple choice, 1 = question short answer);Question text;Right Answers;Other Options;Picture;Subjects;Objectives");
        for (int i = 0; i < questionGenericArrayList.size(); i++) {
            if (questionGenericArrayList.get(i).getIntTypeOfQuestion() == 1) {
                String question = "1;";
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionGenericArrayList.get(i).getGlobalID());

                //copy image file to correct directory
                if (questionShortAnswer.getIMAGE().length() > 0 && !questionShortAnswer.getIMAGE().contentEquals("none")) {
                    File source = new File(questionShortAnswer.getIMAGE());
                    File dest = new File("questions/" + questionShortAnswer.getIMAGE());
                    try {
                        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                question += questionShortAnswer.getQUESTION();
                question += ";";
                ArrayList<String> answers = questionShortAnswer.getANSWER();
                for (int j = 0; j < answers.size(); j++) {
                    question += answers.get(j) + "///";
                }
                question += ";;";       //because short answer questions don't have "other options" -> double ;;
                question += questionShortAnswer.getIMAGE();
                question += ";";
                Vector<String> subjects = questionShortAnswer.getSubjects();
                for (int j = 0; j < subjects.size(); j++) {
                    question += subjects.get(j) + "///";
                }
                question += ";";
                Vector<String> objectives = questionShortAnswer.getObjectives();
                for (int j = 0; j < objectives.size(); j++) {
                    question += objectives.get(j) + "///";
                }
                question += ";";
                writer.println(question);
            } else if (questionGenericArrayList.get(i).getIntTypeOfQuestion() == 0) {
                String question = "0;";
                QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
                try {
                    questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionGenericArrayList.get(i).getGlobalID());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //copy image file to correct directory
                if (questionMultipleChoice.getIMAGE().length() > 0 && !questionMultipleChoice.getIMAGE().contentEquals("none")) {
                    File source = new File(questionMultipleChoice.getIMAGE());
                    File dest = new File("questions/" + questionMultipleChoice.getIMAGE());
                    try {
                        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                question += questionMultipleChoice.getQUESTION();
                question += ";";
                Vector<String> answers = questionMultipleChoice.getCorrectAnswers();
                for (int j = 0; j < answers.size(); j++) {
                    question += answers.get(j) + "///";
                }
                question += ";";
                Vector<String> incorrectOptions = questionMultipleChoice.getIncorrectAnswers();
                for (int j = 0; j < incorrectOptions.size(); j++) {
                    question += incorrectOptions.get(j) + "///";
                }
                question += ";";
                question += questionMultipleChoice.getIMAGE();
                question += ";";
                Vector<String> subjects = questionMultipleChoice.getSubjects();
                for (int j = 0; j < subjects.size(); j++) {
                    question += subjects.get(j) + "///";
                }
                question += ";";
                Vector<String> objectives = questionMultipleChoice.getObjectives();
                for (int j = 0; j < objectives.size(); j++) {
                    question += objectives.get(j) + "///";
                }
                question += ";";
                writer.println(question);
            }

        }
        writer.close();
    }

    public void createTest() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateTest.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateTestController controller = fxmlLoader.getController();
        ArrayList<String> testNames = new ArrayList<>();
        for (Test test : testsList) {
            testNames.add(test.getTestName());
        }
        controller.initParameters(root, testNames);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Test");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void editTest() {
        QuestionGeneric questionGeneric = allQuestionsTree.getSelectionModel().getSelectedItem().getValue();
        if (questionGeneric.getGlobalID() < 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditTest.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditTestController controller = fxmlLoader.getController();
            ArrayList<String> testNames = new ArrayList<>();
            for (Test test : testsList) {
                testNames.add(test.getTestName());
            }
            ArrayList<String> objectives = DbTableRelationObjectiveTest.getObjectivesFromTestName(questionGeneric.getQuestion());
            controller.initParameters(allQuestionsTree, testNames, questionGeneric.getQuestion(), objectives);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Create a New Test");
            stage.setScene(new Scene(root1));
            stage.show();
        } else {
            System.out.println("Cannot edit test: no test selected");
        }
    }

    public void removeTest() {
        TreeItem selectedItem = allQuestionsTree.getSelectionModel().getSelectedItem();
        if (((QuestionGeneric) selectedItem.getValue()).getGlobalID() < 0) {
            DbTableTests.removeTestWithName(((QuestionGeneric) selectedItem.getValue()).getQuestion());
        } else {
            //only sets a flag for the question generic, leave the whole question inside database and doesn't delete image
            DbTableQuestionGeneric.removeQuestion(((QuestionGeneric) selectedItem.getValue()).getGlobalID());
        }
        testsNodeList.remove(selectedItem.getValue());
        selectedItem.getParent().getChildren().remove(selectedItem);
    }

    public void removeQuestionFromTest() {
        TreeItem selectedItem = allQuestionsTree.getSelectionModel().getSelectedItem();
        QuestionGeneric parentTest = (QuestionGeneric) selectedItem.getParent().getValue();
        QuestionGeneric questionGeneric = (QuestionGeneric) selectedItem.getValue();
        DbTableRelationQuestionTest.removeQuestionFromTest(parentTest.getQuestion(), questionGeneric.getGlobalID());
        testsNodeList.remove(selectedItem.getValue());
        selectedItem.getParent().getChildren().remove(selectedItem);
    }

    public void createGroup() {
        if (activeClass.length() > 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateGroup.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CreateGroupController controller = fxmlLoader.getController();
            ArrayList<String> studentsList = new ArrayList<>();
            for (Student singleStudent : LearningTracker.studentGroupsAndClass.get(0).getStudents_vector()) {
                studentsList.add(singleStudent.getName());
            }
            controller.initParameters(activeClass, groupsCombobox, studentsList);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Create a New Group");
            stage.setScene(new Scene(root1));
            stage.show();
        }
    }

    public void deleteGroup() {
        if (groupsCombobox.getSelectionModel().getSelectedItem() != null && groupsCombobox.getSelectionModel().getSelectedIndex() != 0) {
            DbTableClasses.deleteGroup(groupsCombobox.getSelectionModel().getSelectedItem().toString());
            int groupIndex = groupsCombobox.getSelectionModel().getSelectedIndex();
            groupsCombobox.getItems().remove(groupIndex);
            LearningTracker.studentGroupsAndClass.remove(groupIndex);
            LearningTracker.studentsVsQuestionsTableControllerSingleton.removeGroup(groupIndex);
        }
    }

    public void activeClassChanged(String argActiveClass) {
        //change combobox content
        this.activeClass = argActiveClass;
        groupsCombobox.getItems().remove(0,groupsCombobox.getItems().size());
        ArrayList<String> groups = DbTableClasses.getGroupsFromClass(activeClass);
        groups.add(0,activeClass);
        groupsCombobox.getItems().addAll(groups);


        //remove the groups of the former active class
        while (LearningTracker.studentGroupsAndClass.size() > 1) {
            LearningTracker.studentGroupsAndClass.remove(LearningTracker.studentGroupsAndClass.size() - 1);
        }
        for (int i = 0; i < groups.size(); i++) {
            if (i == 0) {
                LearningTracker.studentGroupsAndClass.get(0).setClassName(groups.get(i));
            } else {
                Classroom newGroup = new Classroom();
                newGroup.setClassName(groups.get(i));
                LearningTracker.studentGroupsAndClass.add(newGroup);
            }
        }

        //clean ready questions list or assign them to the new class
        if (LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().size() > 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/AssignQuestionsToNewClassPopUp.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AssignQuestionsToNewClassPopUpController controller = fxmlLoader.getController();
            controller.initParameters(activeClass);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Assign questions?");
            stage.setScene(new Scene(root1));
            stage.show();
        } else {
            //load questions from class
            ArrayList<Integer> questionIds = DbTableRelationClassQuestion.getQuestionsIDsForClass(activeClass);
            for (Integer id : questionIds) {
                LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().add(id);
                LearningTracker.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(id);
            }
            refreshReadyQuestionsList();
        }

        //select class now, otherwise the application will think that we already have some questions to assign
        groupsCombobox.getSelectionModel().select(0);
    }

    public void editGroup() {
        if (activeClass.length() > 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditGroup.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditGroupController controller = fxmlLoader.getController();
            ArrayList<String> studentsList = new ArrayList<>();
            for (Student singleStudent : LearningTracker.studentGroupsAndClass.get(0).getStudents_vector()) {
                studentsList.add(singleStudent.getName());
            }
            if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
                Vector<Student> studentsInGroup = DbTableClasses.getStudentsInClass(groupsCombobox.getSelectionModel().getSelectedItem().toString());
                ArrayList<String> studentNames = new ArrayList<>();
                for (Student student : studentsInGroup) {
                    studentNames.add(student.getName());
                }
                controller.initParameters(activeClass, groupsCombobox, studentsList, studentNames);
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initStyle(StageStyle.DECORATED);
                stage.setTitle("Create a New Group");
                stage.setScene(new Scene(root1));
                stage.show();
            }
        }
    }

    public void activateQuestionsForGroup() {
        if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
            String group = groupsCombobox.getSelectionModel().getSelectedItem().toString();
            ArrayList<Integer> questionIDs = DbTableRelationClassQuestion.getQuestionsIDsForClass(group);
            Vector<Student> students = DbTableClasses.getStudentsInClass(group);
            ArrayList<String> studentNames = new ArrayList<>();
            for (Student student : students) {
                studentNames.add(student.getName());
            }

            NetworkCommunication.networkCommunicationSingleton.activateTestForGroup(questionIDs, studentNames, 0);
        }
    }

    public void activateTestSynchroneousQuestions() {
        if (readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID() < 0) {
            String group = "";
            if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
                group = groupsCombobox.getSelectionModel().getSelectedItem().toString();
            }
            String testName = readyQuestionsList.getSelectionModel().getSelectedItem().getQuestion();
            ArrayList<Integer> questionIDs = DbTableRelationQuestionTest.getQuestionIdsFromTestName(testName);
            Vector<Student> students = new Vector<>();
            if (group.length() > 0) {
                students = DbTableClasses.getStudentsInClass(group);
            } else {
                students = DbTableClasses.getStudentsInClass(activeClass);
            }
            ArrayList<String> studentNames = new ArrayList<>();
            for (Student student : students) {
                studentNames.add(student.getName());
            }

            NetworkCommunication.networkCommunicationSingleton.activateTestSynchroneousQuestions(questionIDs, studentNames,readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID());
        } else {
            System.out.println("No test is selected");
        }
    }

    //OTHER METHODS
    public void loadQuestions() {
        readyQuestionsList.getItems().clear();
        Integer group = groupsCombobox.getSelectionModel().getSelectedIndex();
        if (group < 1) group = 0;

        ArrayList<Integer> questionIds = LearningTracker.studentGroupsAndClass.get(group).getActiveIDs();
        //for (int i = 0; i < IDsFromBroadcastedQuestionsSize; i++) {
        //    removeQuestion(0);
        //}
        //LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().clear();
        if (LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().size() == 0) {
            LearningTracker.studentGroupsAndClass.get(group).setActiveIDs(DbTableRelationClassQuestion.getQuestionsIDsForClass(groupsCombobox.getSelectionModel().getSelectedItem().toString()));
        }
        //refreshReadyQuestionsList(group);
        for (int i = 0; i < questionIds.size(); i++) {
            //LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().add(questionIds.get(i));
            Integer typeOfQuestion = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(questionIds.get(i)));
            if (typeOfQuestion == 0) {
                try {
                    QuestionMultipleChoice questionMultipleChoice =
                            DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(Integer.valueOf(String.valueOf(questionIds.get(i))));
                    QuestionGeneric questionGeneric = new QuestionGeneric();
                    questionGeneric.setQuestion(questionMultipleChoice.getQUESTION());
                    questionGeneric.setGlobalID(questionMultipleChoice.getID());
                    questionGeneric.setIntTypeOfQuestion(0);
                    questionGeneric.setTypeOfQuestion("0");
                    questionGeneric.setImagePath(questionMultipleChoice.getIMAGE());
                    //sendQuestionToStudentsNoDuplicateCheck(questionGeneric);
                    readyQuestionsList.getItems().add(questionGeneric);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (typeOfQuestion == 1) {
                QuestionShortAnswer questionShortAnswer =
                        DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(Integer.valueOf(String.valueOf(questionIds.get(i))));
                QuestionGeneric questionGeneric = new QuestionGeneric();
                questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
                questionGeneric.setGlobalID(questionShortAnswer.getID());
                questionGeneric.setIntTypeOfQuestion(1);
                questionGeneric.setTypeOfQuestion("1");
                questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
                //sendQuestionToStudentsNoDuplicateCheck(questionGeneric);
                readyQuestionsList.getItems().add(questionGeneric);
            } else {
                //it is a test
                Test test = DbTableTests.getTestWithID(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(i));
                QuestionGeneric questionGeneric = new QuestionGeneric();
                questionGeneric.setQuestion(test.getTestName());
                questionGeneric.setGlobalID(-test.getIdTest());
                readyQuestionsList.getItems().add(questionGeneric);
            }
        }
    }

    public void refreshReadyQuestionsList() {
        refreshReadyQuestionsList(0);
    }
    public void refreshReadyQuestionsList(Integer group) {
        //save former broadcasted questions to compare later with new ones
        List<QuestionGeneric> formerQuestions = readyQuestionsList.getItems();
        Vector<Integer> oldIDs = new Vector<>();
        for (QuestionGeneric questionGeneric : formerQuestions) {
            oldIDs.add(questionGeneric.getGlobalID());
        }

        //remove all questions from broadcasted list
        //readyQuestionsList.getItems().remove(0,readyQuestionsList.getItems().size());

        //compare old and new questions and delete or add them to the table
        for (int i = 0; i < oldIDs.size(); i++) {
            if (!LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().contains(oldIDs.get(i))) {
                NetworkCommunication.networkCommunicationSingleton.removeQuestion(i);
                readyQuestionsList.getItems().remove(i);
            }
        }

        //fill readyQuestionsList with new ids from activeIDs
        Boolean oneQuestionSent = false;
        for (int i = 0; i < LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().size(); i++) {
            if (!oldIDs.contains(LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().get(i))) {
                Integer typeOfQuestion = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().get(i)));
                if (typeOfQuestion == 0) {
                    try {
                        QuestionMultipleChoice questionMultipleChoice =
                                DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(Integer.valueOf(LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().get(i)));
                        QuestionGeneric questionGeneric = new QuestionGeneric();
                        questionGeneric.setQuestion(questionMultipleChoice.getQUESTION());
                        questionGeneric.setGlobalID(questionMultipleChoice.getID());
                        questionGeneric.setIntTypeOfQuestion(0);
                        questionGeneric.setTypeOfQuestion("0");
                        questionGeneric.setImagePath(questionMultipleChoice.getIMAGE());
                        if (oneQuestionSent) {
                            sendQuestionToStudentsNoDuplicateCheck(questionGeneric, false);
                        } else {
                            sendQuestionToStudentsNoDuplicateCheck(questionGeneric, true);
                            oneQuestionSent = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (typeOfQuestion == 1) {
                    QuestionShortAnswer questionShortAnswer =
                            DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(Integer.valueOf(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(i)));
                    QuestionGeneric questionGeneric = new QuestionGeneric();
                    questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
                    questionGeneric.setGlobalID(questionShortAnswer.getID());
                    questionGeneric.setIntTypeOfQuestion(1);
                    questionGeneric.setTypeOfQuestion("1");
                    questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
                    if (oneQuestionSent) {
                        sendQuestionToStudentsNoDuplicateCheck(questionGeneric, false);
                    } else {
                        sendQuestionToStudentsNoDuplicateCheck(questionGeneric, true);
                        oneQuestionSent = true;
                    }
                } else {
                    //it is a test
                    Test test = DbTableTests.getTestWithID(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(i));
                    QuestionGeneric questionGeneric = new QuestionGeneric();
                    questionGeneric.setQuestion(test.getTestName());
                    questionGeneric.setGlobalID(-test.getIdTest());
                    sendTestToStudents(questionGeneric, groupsCombobox.getSelectionModel().getSelectedIndex());
                }
            }
        }
    }

    private void sendQuestionToStudents(QuestionGeneric questionGeneric, Integer group, Boolean actualSending, Boolean silenceQuestionCollision) {
        int globalID = questionGeneric.getGlobalID();
        if (group < 1) group = 0;
        //check for presence of question
        if (!LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().contains(globalID)) {
            readyQuestionsList.getItems().add(questionGeneric);
            LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().add(globalID);
            if (!LearningTracker.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(globalID)) {
                LearningTracker.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(globalID);
            }
            if (questionGeneric.getTypeOfQuestion().contentEquals("0")) {
                try {
                    broadcastQuestionMultipleChoice(DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(globalID), actualSending);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                broadcastQuestionShortAnswer(DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(globalID), actualSending);
            }
        } else {
            if (!silenceQuestionCollision) {
                popUpIfQuestionCollision();
            }
        }
    }

    private void sendTestToStudents(QuestionGeneric questionGeneric, Integer group) {
        int globalID = questionGeneric.getGlobalID();
        if (group < 1) group = 0;

        //first send the test
        if (!LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().contains(globalID)) {
            readyQuestionsList.getItems().add(questionGeneric);
            LearningTracker.studentGroupsAndClass.get(group).getActiveIDs().add(globalID);
            if (!LearningTracker.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(globalID)) {
                LearningTracker.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(globalID);
            }
            try {
                NetworkCommunication.networkCommunicationSingleton.sendTestWithID(-globalID, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //if the test isn't in the ready questions list (like when loading new class) add it. Otherwise, show the popup for questions collision
            Boolean testInList = false;
            for (QuestionGeneric singleQuestionGeneric : readyQuestionsList.getItems()) {
                if (singleQuestionGeneric.getGlobalID() == questionGeneric.getGlobalID()) {
                    testInList = true;
                }
            }
            if (testInList) {
                popUpIfQuestionCollision();
            } else {
                readyQuestionsList.getItems().add(questionGeneric);
                try {
                    NetworkCommunication.networkCommunicationSingleton.sendTestWithID(-globalID, null); //do I need that?
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //send questions linked to the test
        ArrayList<Integer> questionIDs = DbTableRelationQuestionTest.getQuestionIdsFromTestName(questionGeneric.getQuestion());
        for (Integer questionID : questionIDs) {
            QuestionGeneric questionGeneric2 = new QuestionGeneric();
            Boolean found = false;
            for (int i = 0; i < genericQuestionsList.size() && !found; i++) {
                if (genericQuestionsList.get(i).getGlobalID() == questionID) {
                    found = true;
                    questionGeneric2 = genericQuestionsList.get(i);
                    sendQuestionToStudents(questionGeneric2, groupsCombobox.getSelectionModel().getSelectedIndex(), false, true);
                }
            }

            //add a relation in the database between the class/group and the question in case it's not yet here
            if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
                DbTableRelationClassQuestion.addClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(),
                        String.valueOf(questionID));
            }
        }
    }

    /* TODO: fix need to use the no duplicate */
    private void sendQuestionToStudentsNoDuplicateCheck(QuestionGeneric questionGeneric, Boolean actualSending) {
        int globalID = questionGeneric.getGlobalID();
        readyQuestionsList.getItems().add(questionGeneric);
        if (questionGeneric.getTypeOfQuestion().contentEquals("0")) {
            try {
                broadcastQuestionMultipleChoice(DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(globalID), actualSending);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            broadcastQuestionShortAnswer(DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(globalID), actualSending);
        }
    }

    private void insertQuestionMultipleChoice(String[] question) {
        Vector<String> options_vector = new Vector<String>();
        for (int i = 0; i < 10; i++) options_vector.add(" ");
        String[] rightAnswers = question[2].split("///");
        String[] otherOptions = question[3].split("///");
        String[] allOptions = concatenate(rightAnswers, otherOptions);
        for (int i = 0; i < 10 && i < allOptions.length && !allOptions[i].contentEquals(" "); i++) {
            options_vector.set(i, allOptions[i]);
        }
        int number_correct_answers = rightAnswers.length;
        QuestionMultipleChoice new_questmultchoice = new QuestionMultipleChoice("1", question[1].replace("'", "''"), options_vector.get(0).replace("'", "''"),
                options_vector.get(1).replace("'", "''"), options_vector.get(2).replace("'", "''"), options_vector.get(3).replace("'", "''"), options_vector.get(4).replace("'", "''"),
                options_vector.get(5).replace("'", "''"), options_vector.get(6).replace("'", "''"), options_vector.get(7).replace("'", "''"), options_vector.get(8).replace("'", "''"),
                options_vector.get(9).replace("'", "''"), question[4].replace("'", "''"));
        new_questmultchoice.setNB_CORRECT_ANS(number_correct_answers);

        //copy image file to correct directory
        if (new_questmultchoice.getIMAGE().length() > 0 && !new_questmultchoice.getIMAGE().contains("none")) {
            File source = new File("questions/" + new_questmultchoice.getIMAGE());
            if (source.exists() && !source.isDirectory()) {
                File dest = new File(new_questmultchoice.getIMAGE());
                try {
                    Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("problem importing question: " + new_questmultchoice.getQUESTION() + ". Image file not found.");
            }
        } else {
            new_questmultchoice.setIMAGE("");
        }

        try {
            DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(new_questmultchoice);
            new_questmultchoice.setID(DbTableQuestionMultipleChoice.getLastIDGlobal());

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //insert question in tree view
        QuestionGeneric questionGeneric = new QuestionGeneric(new_questmultchoice.getID(), 0);
        questionGeneric.setQuestion(new_questmultchoice.getQUESTION());
        questionGeneric.setImagePath(new_questmultchoice.getIMAGE());
        questionGeneric.setTypeOfQuestion("0");
        genericQuestionsList.add(questionGeneric);
        Node questionImage = null;
        questionImage = new ImageView(new Image("file:" + new_questmultchoice.getIMAGE(), 20, 20, true, false));
        TreeItem<QuestionGeneric> itemChild;
        if (new_questmultchoice.getIMAGE().length() < 1) {
            itemChild = new TreeItem<>(questionGeneric);
        } else {
            itemChild = new TreeItem<>(questionGeneric, questionImage);
        }
        allQuestionsTree.getRoot().getChildren().add(itemChild);

        //adding subjects relations
        String[] subjects = question[5].split("///");
        for (int i = 0; i < subjects.length; i++) {
            try {
                DbTableRelationQuestionSubject.addRelationQuestionSubject(subjects[i].replace("'", "''"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        //adding objectives relations
        String[] objectives = question[6].split("///");
        for (int i = 0; i < objectives.length; i++) {
            try {
                DbTableRelationQuestionObjective.addRelationQuestionObjective(objectives[i].replace("'", "''"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void insertQuestionShortAnswer(String[] question) {
        QuestionShortAnswer new_questshortanswer = new QuestionShortAnswer();
        new_questshortanswer.setQUESTION(question[1].replace("'", "''"));
        if (question[4].length() > 0) {
            new_questshortanswer.setIMAGE(question[4]);
        }
        ArrayList<String> answerOptions = new ArrayList<String>();
        String[] rightAnswers = question[2].split("///");
        for (int i = 0; i < rightAnswers.length; i++) {
            String answerOption = rightAnswers[i];
            if (answerOption.length() > 0) {
                answerOptions.add(answerOption.replace("'", "''"));
            }
        }
        new_questshortanswer.setANSWER(answerOptions);

        //copy image file to correct directory
        if (new_questshortanswer.getIMAGE().length() > 0 && !new_questshortanswer.getIMAGE().contains("none")) {
            File source = new File("questions/" + new_questshortanswer.getIMAGE());
            if (source.exists() && !source.isDirectory()) {
                File dest = new File(new_questshortanswer.getIMAGE());
                try {
                    Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Problem inserting question: " + new_questshortanswer.getQUESTION() + ". Image file not found.");
            }
        } else {
            new_questshortanswer.setIMAGE("");
        }

        String idGlobal = "-1";
        try {
            idGlobal = DbTableQuestionShortAnswer.addShortAnswerQuestion(new_questshortanswer);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        new_questshortanswer.setID(Integer.valueOf(idGlobal));


        //put the question in the treeView
        QuestionGeneric questionGeneric = new QuestionGeneric(new_questshortanswer.getID(), 1);
        questionGeneric.setQuestion(new_questshortanswer.getQUESTION());
        questionGeneric.setImagePath(new_questshortanswer.getIMAGE());
        questionGeneric.setTypeOfQuestion("1");
        genericQuestionsList.add(questionGeneric);
        Node questionImage = null;
        questionImage = new ImageView(new Image("file:" + new_questshortanswer.getIMAGE(), 20, 20, true, false));
        TreeItem<QuestionGeneric> itemChild;
        if (new_questshortanswer.getIMAGE().length() < 1) {
            itemChild = new TreeItem<>(questionGeneric);
        } else {
            itemChild = new TreeItem<>(questionGeneric, questionImage);
        }
        allQuestionsTree.getRoot().getChildren().add(itemChild);

        //adding subjects relations
        String[] subjects = question[5].split("///");
        for (int i = 0; i < subjects.length; i++) {
            try {
                DbTableRelationQuestionSubject.addRelationQuestionSubject(subjects[i].replace("'", "''"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        //adding objectives relations
        String[] objectives = question[6].split("///");
        for (int i = 0; i < objectives.length; i++) {
            try {
                DbTableRelationQuestionObjective.addRelationQuestionObjective(objectives[i].replace("'", "''"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private List<String> readFile(String filename) {
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
            return records;
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }

    static public <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    private void broadcastQuestionMultipleChoice(QuestionMultipleChoice questionMultipleChoice, Boolean actualSending) {
        NetworkCommunication.networkCommunicationSingleton.getClassroom().addQuestMultChoice(questionMultipleChoice);
        try {
            System.out.println("broadcasting questions");
            if (actualSending) {
                NetworkCommunication.networkCommunicationSingleton.sendMultipleChoiceWithID(questionMultipleChoice.getID(), null);
            }
            NetworkCommunication.networkCommunicationSingleton.addQuestion(questionMultipleChoice.getQUESTION(), questionMultipleChoice.getID(), groupsCombobox.getSelectionModel().getSelectedIndex());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void broadcastQuestionShortAnswer(QuestionShortAnswer questionShortAnswer, Boolean actualSending) {
        NetworkCommunication.networkCommunicationSingleton.getClassroom().addQuestShortAnswer(questionShortAnswer);
        try {
            if (actualSending) {
                NetworkCommunication.networkCommunicationSingleton.sendShortAnswerQuestionWithID(questionShortAnswer.getID(), null);
            }
            NetworkCommunication.networkCommunicationSingleton.addQuestion(questionShortAnswer.getQUESTION(), questionShortAnswer.getID(), groupsCombobox.getSelectionModel().getSelectedIndex());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void popUpIfQuestionCollision() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Unfortunately, you cannot use a question twice in the same set"));
        Scene dialogScene = new Scene(dialogVbox, 400, 40);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}