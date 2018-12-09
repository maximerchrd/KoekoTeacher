package koeko.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.Tools.FilesHandler;
import koeko.controllers.QuestionSending.QuestionTreeCell;
import koeko.controllers.QuestionsControlling.CreateQuestionController;
import koeko.controllers.TestControlling.CreateTestController;
import koeko.questions_management.*;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
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
import koeko.database_management.*;
import koeko.view.QuestionView;
import koeko.view.Utilities;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.lang.*;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by maximerichard on 01.03.18.
 */
public class QuestionSendingController extends Window implements Initializable {
    //all questions tree (left panel)
    private QuestionGeneric draggedQuestion = null;
    private TreeItem<QuestionGeneric> draggedTreeItem = null;
    private TreeItem<QuestionGeneric> root;
    private String activeClass = "";
    private ContextMenu studentsContextMenu;
    public ArrayList<String> questionsForTest = new ArrayList<>();
    public List<QuestionGeneric> testsNodeList = new ArrayList<>();
    public Map<String, QuestionGeneric> testsNodeMap = new LinkedHashMap<>();
    public List<QuestionGeneric> genericQuestionsList = new ArrayList<>();
    public Map<String, QuestionGeneric> genericQuestionsMap = new LinkedHashMap<>();
    public List<Test> testsList = new ArrayList<>();
    public TreeItem<QuestionGeneric> editedTestItem = null;

    @FXML
    public TreeView<QuestionGeneric> allQuestionsTree;
    @FXML
    public ListView<QuestionGeneric> readyQuestionsList;
    @FXML
    private ComboBox groupsCombobox;
    @FXML
    private ComboBox uiChoiceBox;
    @FXML
    private Accordion questionSendingAccordion;
    @FXML
    private Button createQuestionButton;
    @FXML
    private Button createTestButton;
    @FXML
    private Button broadcastQuestionForStudentsButton;
    @FXML
    private Button activateQuestionForStudentsButton;

    public void initialize(URL location, ResourceBundle resources) {
        Koeko.questionSendingControllerSingleton = this;

        //setup UI choicebox
        uiChoiceBox.setItems(FXCollections.observableArrayList(
                "Basic Commands", "Advanced Commands")
        );
        int uiMode = DbTableSettings.getUIMode();
        uiChoiceBox.getSelectionModel().select(uiMode);

        //set tooltips for buttons
        Tooltip tooltipcreateQuestion = new Tooltip("Create a new Question");
        createQuestionButton.setTooltip(tooltipcreateQuestion);
        Tooltip tooltipcreateTest = new Tooltip("Create a new Test");
        createTestButton.setTooltip(tooltipcreateTest);
        Tooltip tooltipbroadcastQuestion = new Tooltip("Synchronize selected Question or Test with students' devices");
        broadcastQuestionForStudentsButton.setTooltip(tooltipbroadcastQuestion);
        Tooltip tooltipactivateQuestion = new Tooltip("Activate selected Question or Test on students' devices");
        activateQuestionForStudentsButton.setTooltip(tooltipactivateQuestion);

        //all questions tree (left panel)
        //retrieve data from db
        try {
            genericQuestionsList = DbTableQuestionGeneric.getAllGenericQuestions();
            for (QuestionGeneric questionGeneric : genericQuestionsList) {
                genericQuestionsMap.put(questionGeneric.getGlobalID(), questionGeneric);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        testsList = DbTableTest.getAllTests();
        for (Test test : testsList) {
            QuestionGeneric testGeneric = new QuestionGeneric();
            testGeneric.setGlobalID(QuestionGeneric.changeIdSign(test.getIdTest()));
            testGeneric.setQuestion(test.getTestName());
            if (test.getTestMode() == QuestionGeneric.CERTIFICATIVE_TEST) {
                testGeneric.setIntTypeOfQuestion(QuestionGeneric.CERTIFICATIVE_TEST);
            } else if (test.getTestMode() == QuestionGeneric.FORMATIVE_TEST) {
                testGeneric.setIntTypeOfQuestion(QuestionGeneric.FORMATIVE_TEST);
            } else if (test.getTestMode() == QuestionGeneric.GAME) {
                testGeneric.setIntTypeOfQuestion(QuestionGeneric.GAME);
            } else if (test.getTestMode() == QuestionGeneric.GAME_QUESTIONSET) {
                testGeneric.setIntTypeOfQuestion(QuestionGeneric.GAME_QUESTIONSET);
            }
            testsNodeList.add(testGeneric);
            testsNodeMap.put(testGeneric.getGlobalID(), testGeneric);
        }
        //create root
        root = new TreeItem<>(new QuestionGeneric());
        root.setExpanded(true);
        allQuestionsTree.setShowRoot(false);
        //populate the tree with tests and questions on the javafx thread
        Platform.runLater(() -> populateTree(null));
        allQuestionsTree.setRoot(root);
        allQuestionsTree.getStylesheets().add("/style/treeview.css");
        allQuestionsTree.setCellFactory(stringTreeView -> {
            QuestionTreeCell treeCell = new QuestionTreeCell();

            treeCell.setOnDragDetected(mouseEvent -> {
                if (Long.valueOf(treeCell.getTreeItem().getValue().getGlobalID()) > 0
                        || treeCell.getTreeItem().getValue().getIntTypeOfQuestion() == QuestionGeneric.GAME_QUESTIONSET) {
                    draggedQuestion = treeCell.getTreeItem().getValue();
                    draggedTreeItem = treeCell.getTreeItem();
                    Dragboard db = allQuestionsTree.startDragAndDrop(TransferMode.ANY);

                    // Put a string on a dragboard
                    ClipboardContent content = new ClipboardContent();
                    content.putString(treeCell.getText());
                    db.setContent(content);

                    mouseEvent.consume();
                }
            });

            treeCell.setOnDragOver(event -> {
                // data is dragged over the target
                // accept it only if it is not dragged from the same node
                // and if it has a string data
                if (event.getGestureSource() != treeCell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY);
                }

                event.consume();
            });

            treeCell.setOnDragEntered(event -> {
                // the drag-and-drop gesture entered the target
                // show to the user that it is an actual gesture target
                if (event.getGestureSource() != treeCell &&
                        event.getDragboard().hasString()) {
                    treeCell.setTextFill(Color.LIGHTGREEN);
                }
                event.consume();
            });
            treeCell.setOnDragExited(event -> {
                // mouse moved away, remove the graphical cues
                treeCell.setTextFill(Color.BLACK);
                event.consume();
            });


            treeCell.setOnDragDropped(event -> {
                // data dropped
                if (Long.valueOf(treeCell.getTreeItem().getValue().getGlobalID()) < 0) {
                    if (treeCell.getTreeItem().getValue().getIntTypeOfQuestion() == QuestionGeneric.CERTIFICATIVE_TEST) {
                        System.out.println("Trying to drag on certificative test");
                    } else {
                        if (((treeCell.getTreeItem().getValue().getIntTypeOfQuestion() == QuestionGeneric.FORMATIVE_TEST
                                || treeCell.getTreeItem().getValue().getIntTypeOfQuestion() == QuestionGeneric.GAME_QUESTIONSET)
                                && draggedQuestion.getIntTypeOfQuestion() != QuestionGeneric.GAME_QUESTIONSET)
                                || (treeCell.getTreeItem().getValue().getIntTypeOfQuestion() == QuestionGeneric.GAME
                                && draggedQuestion.getIntTypeOfQuestion() == QuestionGeneric.GAME_QUESTIONSET)) {
                            //add a horizontal relation with the question before in the list
                            int bigBrotherIndex = treeCell.getTreeItem().getChildren().size() - 1;
                            TreeItem<QuestionGeneric> questionBefore = null;
                            if (bigBrotherIndex >= 0) {
                                questionBefore = treeCell.getTreeItem().getChildren().get(bigBrotherIndex);
                            }
                            if (questionBefore != null) {
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                                        String.valueOf(Utilities.setPositiveIdSign(draggedQuestion.getGlobalID())), treeCell.getTreeItem().getValue().getQuestion(),
                                        treeCell.getTreeItem().getValue().getGlobalID(), "");
                            } else {
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion("0",
                                        String.valueOf(Utilities.setPositiveIdSign(draggedQuestion.getGlobalID())), treeCell.getTreeItem().getValue().getQuestion(),
                                        treeCell.getTreeItem().getValue().getGlobalID(), "");
                            }

                            //add the node to the tree
                            TreeItem<QuestionGeneric> newTreeItem = new TreeItem<>(draggedQuestion);
                            if (newTreeItem.getValue().getIntTypeOfQuestion() == QuestionGeneric.GAME_QUESTIONSET) {
                                newTreeItem.getChildren().addAll(draggedTreeItem.getChildren());
                            }
                            treeCell.getTreeItem().getChildren().add(newTreeItem);
                            event.setDropCompleted(true);
                            treeCell.getTreeItem().setExpanded(true);
                            event.consume();
                        }
                    }
                } else if (treeCell.getTreeItem().getChildren() != draggedQuestion
                        && treeCell.getTreeItem().getParent().getValue().getIntTypeOfQuestion() != QuestionGeneric.GAME_QUESTIONSET) {
                    Boolean tryingChildOfItself = false;
                    TreeItem<QuestionGeneric> parent = treeCell.getTreeItem();
                    while (parent != null) {
                        if (parent.getValue().getGlobalID().contentEquals(draggedQuestion.getGlobalID())) {
                            tryingChildOfItself = true;
                            break;
                        }
                        parent = parent.getParent();
                    }
                    if (tryingChildOfItself) {
                        System.out.println("Trying to link question with itself: not allowed, would make an infinite recursion");
                    } else {
                        TreeItem<QuestionGeneric> treeItemTest = treeCell.getTreeItem();
                        while (treeItemTest.getParent() != root) {
                            treeItemTest = treeItemTest.getParent();
                        }
                        if (Long.valueOf(treeItemTest.getValue().getGlobalID()) < 0) {
                            int bigBrotherIndex = treeCell.getTreeItem().getChildren().size() - 1;
                            TreeItem<QuestionGeneric> questionBefore = null;
                            if (bigBrotherIndex >= 0) {
                                questionBefore = treeCell.getTreeItem().getChildren().get(bigBrotherIndex);
                            }
                            if (questionBefore != null) {
                                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                                        String.valueOf(draggedQuestion.getGlobalID()), treeItemTest.getValue().getQuestion(),
                                        treeItemTest.getValue().getGlobalID(), "");
                            }

                            //add the node to the tree and set the vertical relation
                            treeCell.getTreeItem().getChildren().add(new TreeItem<>(draggedQuestion));
                            DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(treeCell.getTreeItem().getValue().getGlobalID()),
                                    String.valueOf(draggedQuestion.getGlobalID()), treeItemTest.getValue().getQuestion(),
                                    treeItemTest.getValue().getGlobalID(), "EVALUATION<60");
                            event.setDropCompleted(true);
                            treeCell.getTreeItem().setExpanded(true);
                            event.consume();
                        }
                    }
                } else {
                    System.out.println("Trying to drag on self or on question not belonging to any test");
                }
                draggedQuestion = null;
            });

            treeCell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (editedTestItem != null && Long.valueOf(treeCell.getTreeItem().getValue().getGlobalID()) > 0
                        && treeCell.getTreeItem().getParent().getParent() == null && event.getClickCount() == 2) {
                    questionsForTest.add(treeCell.getTreeItem().getValue().getGlobalID());
                    addQuestionToTest(treeCell.getTreeItem().getValue());
                    allQuestionsTree.refresh();
                }
            });


            return treeCell;
        });

        allQuestionsTree.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2 && editedTestItem == null) {
                broadcastQuestionForStudents();
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
                    setStyle("");
                } else {
                    //set correct background for activation
                    if (questionGeneric.getActivated()) {
                        setStyle("-fx-background-color:#ff9999;");
                    } else {
                        setStyle("");
                    }
                    HBox hBox = new HBox();
                    hBox.setSpacing(5);
                    javafx.scene.control.Button buttonDelete = new javafx.scene.control.Button("X");
                    buttonDelete.setTooltip(
                            new Tooltip("Deactivate question")
                    );
                    buttonDelete.setOnAction((event) -> {
                        deactivateQuestion(this);
                    });

                    imageView.setImage(new Image("file:" + questionGeneric.getImagePath(), 40, 40, true, false));

                    if (questionGeneric.getIntTypeOfQuestion() == QuestionGeneric.GAME) {
                        javafx.scene.control.Button buttonGame = new javafx.scene.control.Button("G");
                        buttonGame.setTooltip(
                                new Tooltip("Open game controller")
                        );
                        buttonGame.setOnAction((event) -> {
                            openGameController();
                        });
                        hBox.getChildren().addAll(buttonDelete, buttonGame, imageView);
                    } else {
                        hBox.getChildren().addAll(buttonDelete, imageView);
                    }
                    setText(questionGeneric.getQuestion());
                    setGraphic(hBox);
                }
            }
        });

        studentsContextMenu = new ContextMenu();
        readyQuestionsList.setContextMenu(studentsContextMenu);
        readyQuestionsList.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2 && readyQuestionsList.getSelectionModel().getSelectedItem() != null) {
                activateQuestionForStudents();
            }
        });
    }

    private void addQuestionToTest(QuestionGeneric questionToAdd) {
        //add a horizontal relation with the question before in the list
        int bigBrotherIndex = editedTestItem.getChildren().size() - 1;
        TreeItem<QuestionGeneric> questionBefore = null;
        if (bigBrotherIndex >= 0) {
            questionBefore = editedTestItem.getChildren().get(bigBrotherIndex);
        }
        if (questionBefore != null) {
            DbTableRelationQuestionQuestion.addRelationQuestionQuestion(String.valueOf(questionBefore.getValue().getGlobalID()),
                    String.valueOf(questionToAdd.getGlobalID()), editedTestItem.getValue().getQuestion(),
                    editedTestItem.getValue().getGlobalID(), "");
        }

        //add the node to the tree
        editedTestItem.getChildren().add(new TreeItem<>(questionToAdd));
        editedTestItem.setExpanded(true);
    }

    public void addSudentToContextMenu(Student student) {
        Boolean alreadyAdded = false;
        for (MenuItem menuItem : studentsContextMenu.getItems()) {
            if (menuItem.getText().contentEquals(student.getName())) {
                alreadyAdded = true;
            }
        }
        if (Koeko.studentGroupsAndClass.get(0) != null && !alreadyAdded) {
            MenuItem studentItem = new MenuItem(student.getName());
            studentItem.setOnAction(event -> {
                if (readyQuestionsList.getSelectionModel().getSelectedItem() != null) {
                    String questionID = readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID();
                    ArrayList<Student> singleStudent = new ArrayList<>();
                    singleStudent.add(student);
                    NetworkCommunication.networkCommunicationSingleton.sendQuestionID(questionID, singleStudent);
                }
            });
            studentsContextMenu.getItems().add(studentItem);
        }
    }


    public void populateTree(Vector<String> IDs) {
        //populate tree

        //filter according to selected subject
        if (IDs != null) {
            root.getChildren().clear();
            genericQuestionsList.clear();
            for (String id : IDs) {
                QuestionGeneric questionGeneric = new QuestionGeneric();
                questionGeneric.setGlobalID(id);
                genericQuestionsList.add(questionGeneric);
            }

            for (QuestionGeneric testGeneric : testsNodeList) {
                Set<String> questionIDs = DbTableRelationQuestionQuestion.getQuestionsLinkedToTest(testGeneric.getQuestion());

                //apply subject filter
                for (String id : questionIDs) {
                    if (IDs.contains(String.valueOf(id))) {
                        for (String id2 : questionIDs) {
                            QuestionGeneric questionGeneric = QuestionGeneric.searchForQuestionWithID(genericQuestionsList, id2);
                            if (questionGeneric == null) {
                                questionGeneric = new QuestionGeneric();
                                questionGeneric.setGlobalID(id2);
                                genericQuestionsList.add(questionGeneric);
                            }
                        }
                        break;
                    }
                }
            }
        }

        //ONE: add the tests
        for (QuestionGeneric testGeneric : testsNodeList) {
            ArrayList<String> questionIDs = DbTableRelationQuestionQuestion.getFirstLayerQuestionIdsFromTestName(testGeneric.getQuestion());

            //apply subject filter
            Boolean addTest = false;
            if (IDs != null) {
                for (String id : questionIDs) {
                    if (IDs.contains(id)) {
                        addTest = true;
                        break;
                    }
                }
            } else {
                addTest = true;
            }
            if (addTest) {
                TreeItem newTest = new TreeItem<>(testGeneric);
                root.getChildren().add(newTest);
                for (String id : questionIDs) {
                    if (testGeneric.getIntTypeOfQuestion() != QuestionGeneric.GAME) {
                        Boolean found = false;
                        for (int i = 0; i < genericQuestionsList.size() && !found; i++) {
                            if (genericQuestionsList.get(i).getGlobalID().contentEquals(id)) {
                                found = true;
                                TreeItem questionItem = new TreeItem<>(genericQuestionsList.get(i));
                                newTest.getChildren().add(questionItem);

                                //add the questions linked to the test questions
                                populateWithLinkedQuestions(testGeneric, id, questionItem);
                            }
                        }
                    } else {
                        QuestionGeneric questionSetGeneric = testsNodeMap.get("-" + id);
                        if (questionSetGeneric != null) {
                            TreeItem questionSetItem = new TreeItem<>(questionSetGeneric);
                            ArrayList<String> questionsSetIDs = DbTableRelationQuestionQuestion.getFirstLayerQuestionIdsFromTestName(questionSetGeneric.getQuestion());
                            for (String id2 : questionsSetIDs) {
                                QuestionGeneric questionGeneric2 = genericQuestionsMap.get(id2);
                                if (questionGeneric2 != null) {
                                    TreeItem questionItem = new TreeItem<>(questionGeneric2);
                                    questionSetItem.getChildren().add(questionItem);
                                }
                            }
                            newTest.getChildren().add(questionSetItem);
                        }
                    }
                }
            }
        }

        //then add the questions
        for (int i = 0; i < genericQuestionsList.size(); i++) {
            try {
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(genericQuestionsList.get(i).getGlobalID());
                Node questionImage = null;
                if (questionMultipleChoice.getQUESTION().length() < 1) {
                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(genericQuestionsList.get(i).getGlobalID());
                    genericQuestionsList.get(i).setQuestion(questionShortAnswer.getQUESTION());
                    genericQuestionsList.get(i).setImagePath(questionShortAnswer.getIMAGE());
                    genericQuestionsList.get(i).setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);
                    questionImage = new ImageView(new Image("file:" + questionShortAnswer.getIMAGE(), 20, 20, true, false));
                } else {
                    genericQuestionsList.get(i).setQuestion(questionMultipleChoice.getQUESTION());
                    if (questionMultipleChoice.getIMAGE().length() > 0) {
                        genericQuestionsList.get(i).setImagePath(questionMultipleChoice.getIMAGE());
                        genericQuestionsList.get(i).setIntTypeOfQuestion(QuestionGeneric.MCQ);
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

    private void populateWithLinkedQuestions(QuestionGeneric testGeneric, String id, TreeItem questionItem) {
        Vector<String> linkedQuestionsIds = DbTableRelationQuestionQuestion.getQuestionsLinkedToQuestion(String.valueOf(id), testGeneric.getQuestion());
        for (String questionID : linkedQuestionsIds) {
            if (DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(questionID) == 0) {
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionID);
                QuestionGeneric questionGeneric = QuestionGeneric.mcqToQuestionGeneric(questionMultipleChoice);
                TreeItem questionChildren = new TreeItem<>(questionGeneric);
                questionItem.getChildren().add(questionChildren);
                Vector<String> linkedQuestionsIds2 = DbTableRelationQuestionQuestion.getQuestionsLinkedToQuestion(questionID, testGeneric.getQuestion());
                if (linkedQuestionsIds2.size() > 0 && !questionID.contentEquals(id)) {
                    populateWithLinkedQuestions(testGeneric, questionID, questionChildren);
                }
            } else {
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionID);
                QuestionGeneric questionGeneric = QuestionGeneric.shrtaqToQuestionGeneric(questionShortAnswer);
                TreeItem questionChildren = new TreeItem<>(questionGeneric);
                questionItem.getChildren().add(questionChildren);
                Vector<String> linkedQuestionsIds2 = DbTableRelationQuestionQuestion.getQuestionsLinkedToQuestion(questionID, testGeneric.getQuestion());
                if (linkedQuestionsIds2.size() > 0  && !questionID.contentEquals(id)) {
                    populateWithLinkedQuestions(testGeneric, questionID, questionChildren);
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
        if (Long.valueOf(questionGeneric.getGlobalID()) > 0) {
            sendQuestionToStudents(questionGeneric, groupsCombobox.getSelectionModel().getSelectedIndex(), true, false);
        } else if (Long.valueOf(questionGeneric.getGlobalID()) < 0) {
            // send test infos and linked objectives
            sendTestToStudents(questionGeneric, groupsCombobox.getSelectionModel().getSelectedIndex(), allQuestionsTree.getSelectionModel().getSelectedItem());
        } else {
            System.out.println("Trying to broadcast question or test but ID == 0.");
        }
    }

    public void activateQuestionForStudents() {
        //START build the students vector
        String group = "";
        if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
            group = groupsCombobox.getSelectionModel().getSelectedItem().toString();
        }
        ArrayList<Student> students = new ArrayList<>();
        if (group.length() > 0) {
            students = DbTableClasses.getStudentsInClass(group);
        } else {
            students = DbTableClasses.getStudentsInClass(activeClass);
        }

        if (students.size() == 0) {
            //if there is no class selected
            students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents();
        } else if (groupsCombobox.getSelectionModel().getSelectedIndex() == 0) {
            //if a class (and not a group!) is selected, make sure that all students connected get the question
            Vector<Student> tableStudents = (Vector<Student>) NetworkCommunication.networkCommunicationSingleton.aClass.getStudents().clone();
            for (int i = 0; i < tableStudents.size(); i++) {
                for (int j = 0; j < students.size(); j++) {
                    if (tableStudents.get(i).getName().contentEquals(students.get(j).getName())) {
                        tableStudents.remove(tableStudents.get(i));
                    }
                }
            }
            students.addAll(tableStudents);
        }
        //END build the students vector

        //get the selected questions
        QuestionGeneric questionGeneric = readyQuestionsList.getSelectionModel().getSelectedItem();

        if (!NetworkCommunication.networkCommunicationSingleton.checkIfQuestionsOnDevices()) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/QuestionsNotReadyPopUp.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QuestionsNotReadyPopUpController controller = fxmlLoader.getController();
            controller.initParameters(questionGeneric, students);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Send anyway?");
            stage.setScene(new Scene(root1));
            stage.show();
        } else {
            if (Long.valueOf(questionGeneric.getGlobalID()) > 0) {
                NetworkCommunication.networkCommunicationSingleton.sendQuestionID(questionGeneric.getGlobalID(), students);
            } else {
                activateTestSynchroneousQuestions();
            }
            //mark the question/test as activated
            for (QuestionGeneric questionGeneric1 : readyQuestionsList.getItems()) {
                questionGeneric1.setActivated(false);
            }
            readyQuestionsList.getSelectionModel().getSelectedItem().setActivated(true);
            readyQuestionsList.refresh();
        }

        //keep track of which questions are activated for which students in which group
        for (int i = 0; i < Koeko.studentGroupsAndClass.size(); i++) {
            if (group.contentEquals(Koeko.studentGroupsAndClass.get(i).getClassName())) {
                for (int j = 0; j < students.size(); j++) {
                    Vector<String> questionIds = Koeko.studentGroupsAndClass.get(i).
                            getOngoingQuestionsForStudent().get(students.get(j).getName());

                    if (questionIds == null) {
                        questionIds = new Vector<>();

                    }
                    questionIds.add(String.valueOf(questionGeneric.getGlobalID()));

                    //if the id corresponds to a test, also add the questions linked to it
                    if (Long.valueOf(questionGeneric.getGlobalID()) < 0) {
                        Set<String> questions = DbTableRelationQuestionQuestion.getQuestionsLinkedToTest(DbTableTest.getTestWithID(QuestionGeneric.changeIdSign(questionGeneric.getGlobalID())).getTestName());
                        for (String question : questions) {
                            questionIds.add(question);
                        }
                    }

                    Koeko.studentGroupsAndClass.get(i).getOngoingQuestionsForStudent().put(students.get(j).getName(), questionIds);
                }
            }
        }
        if (Koeko.recordLogs) {
            DbTableLogs.insertLog("NBSDTS", String.valueOf(students.size()));
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

    public void deactivateQuestion(ListCell<QuestionGeneric> questionCell) {
        Integer group = groupsCombobox.getSelectionModel().getSelectedIndex();
        if (group < 1) {
            group = 0;
        }

        //remove question from sentQuestions
        try {
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getQuestionIdsToSend().remove(questionCell.getItem().getGlobalID());
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("network state: " + NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton());
            System.out.println("questionIdsToSend: " + NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getQuestionIdsToSend());
            System.out.println("questionCell: " + questionCell);
            System.out.println("questionCell.getItem(): " + questionCell.getItem());
        }
        //remove question from table
        int index = questionCell.getIndex();
        if (Long.valueOf(questionCell.getItem().getGlobalID()) > 0) {
            Koeko.studentsVsQuestionsTableControllerSingleton.removeQuestion(index, group);
        }

        //remove question from database
        if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
            //remove question - class/group relation
            DbTableRelationClassQuestion.removeClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(),
                    String.valueOf(Koeko.studentGroupsAndClass.get(group).getActiveIDs().get(index)));
        }
        if (index >= 0) {
            Koeko.studentGroupsAndClass.get(group).getActiveIDs().remove(index);
            //remove activation if it was activated
            if (NetworkCommunication.networkCommunicationSingleton
                    .getNetworkStateSingleton().getActiveID().contentEquals(readyQuestionsList.getItems().get(index).getGlobalID())) {
                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().setActiveID("");
                readyQuestionsList.getItems().get(index).setActivated(false);
            }
            readyQuestionsList.getItems().remove(index);
            readyQuestionsList.refresh();
        }
    }

    public void removeQuestion(int index) {
        Integer group = groupsCombobox.getSelectionModel().getSelectedIndex();
        NetworkCommunication.networkCommunicationSingleton.removeQuestion(index);
        DbTableRelationClassQuestion.removeClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(),
                String.valueOf(Koeko.studentGroupsAndClass.get(group).getActiveIDs().get(index)));
        Koeko.studentGroupsAndClass.get(group).getActiveIDs().remove(index);
        readyQuestionsList.getItems().remove(index);
    }

    public void sendCorrection() {
        NetworkCommunication.networkCommunicationSingleton.sendCorrection(readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID());
    }

    public void importQuestions() {
        String importDoneMessage = "Import Done.\n";

        List<String> input = readFile(FilesHandler.exportDirectory + "questions.csv");
        input.remove(0);
        for (int i = 0; i < input.size(); i++) {
            input.set(i, input.get(i) + "END");
            String[] question = input.get(i).split(";");

            if (question.length >= 6) {
                //insert subjects
                String[] subjects = question[5].split("///");
                for (int j = 0; j < subjects.length; j++) {
                    try {
                        DbTableSubject.addSubject(subjects[j]);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

                //insert objectives
                String[] objectives = question[6].split("///");
                for (int j = 0; j < objectives.length; j++) {
                    try {
                        DbTableLearningObjectives.addObjective(objectives[j], 1);
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
                importDoneMessage += "problem importing following question (missing fields)\n";
                System.out.println("problem importing following question (missing fields)");
                for (String questionPart : question) {
                    importDoneMessage += questionPart;
                    System.out.println(questionPart);
                }
            }
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/GenericPopUp.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenericPopUpController controller = fxmlLoader.getController();
        controller.initParameters(importDoneMessage);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Import");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void exportQuestions() {
        Boolean exportOK = true;
        File directory = new File("questions");
        if (!directory.exists()) {
            try {
                Files.createDirectories(Paths.get("questions"));
            } catch (IOException e) {
                exportOK = false;
                e.printStackTrace();
            }
        }
        ArrayList<QuestionGeneric> questionGenericArrayList = new ArrayList<>();
        try {
            questionGenericArrayList = DbTableQuestionGeneric.getAllGenericQuestions();
        } catch (Exception e) {
            exportOK = false;
            e.printStackTrace();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(FilesHandler.exportDirectory + "questions.csv", "UTF-8");
            writer.println("Questions Type (0 = question multiple choice, 1 = question short answer);Question text;Right Answers;Other Options;Picture;Subjects;Objectives");
        } catch (FileNotFoundException e) {
            exportOK = false;
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            exportOK = false;
            e.printStackTrace();
        }
        for (int i = 0; i < questionGenericArrayList.size(); i++) {
            if (questionGenericArrayList.get(i).getIntTypeOfQuestion() == 1) {
                String question = "1;";
                QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionGenericArrayList.get(i).getGlobalID());

                //copy image file to correct directory
                if (questionShortAnswer.getIMAGE().length() > 0 && !questionShortAnswer.getIMAGE().contentEquals("none")) {
                    exportOK = FilesHandler.createExportMediaDirIfNotExists();
                    File source = new File(FilesHandler.mediaDirectory + questionShortAnswer.getIMAGE());
                    File dest = new File(FilesHandler.exportDirectory + FilesHandler.mediaDirectory + questionShortAnswer.getIMAGE());
                    try {
                        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                        exportOK = false;
                    }
                }

                question += questionShortAnswer.getQUESTION().replace("\n"," ");
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
                try {
                    writer.println(question);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    exportOK = false;
                }
            } else if (questionGenericArrayList.get(i).getIntTypeOfQuestion() == 0) {
                String question = "0;";
                QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
                try {
                    questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionGenericArrayList.get(i).getGlobalID());
                } catch (Exception e) {
                    e.printStackTrace();
                    exportOK = false;
                }

                //copy image file to correct directory
                if (questionMultipleChoice.getIMAGE().length() > 0 && !questionMultipleChoice.getIMAGE().contentEquals("none")) {
                    exportOK = FilesHandler.createExportMediaDirIfNotExists();
                    File source = new File(FilesHandler.mediaDirectory + questionMultipleChoice.getIMAGE());
                    if (source.exists()) {
                        File dest = new File(FilesHandler.exportDirectory + FilesHandler.mediaDirectory + questionMultipleChoice.getIMAGE());
                        try {
                            Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("Exporting question: image not found");
                        exportOK = false;
                    }
                }

                question += questionMultipleChoice.getQUESTION().replace("\n"," ");;
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
                try {
                    writer.println(question);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    exportOK = false;
                }
            }

        }
        try {
            writer.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
            exportOK = false;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/GenericPopUp.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenericPopUpController controller = fxmlLoader.getController();
        if (exportOK) {
            controller.initParameters("Export Done!");
        } else {
            controller.initParameters("There was a problem during export :-(");
        }
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Export");
        stage.setScene(new Scene(root1));
        stage.show();
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
        testsList = DbTableTest.getAllTests();
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
            for (Student singleStudent : Koeko.studentGroupsAndClass.get(0).getStudents()) {
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
            Koeko.studentGroupsAndClass.remove(groupIndex);
            Koeko.studentsVsQuestionsTableControllerSingleton.removeGroup(groupIndex);
        }
    }

    public void activeClassChanged(String argActiveClass) {
        //change combobox content
        this.activeClass = argActiveClass;
        groupsCombobox.getItems().remove(0, groupsCombobox.getItems().size());
        ArrayList<String> groups = DbTableClasses.getGroupsFromClass(activeClass);
        groups.add(0, activeClass);
        groupsCombobox.getItems().addAll(groups);


        //remove the groups of the former active class
        while (Koeko.studentGroupsAndClass.size() > 1) {
            Koeko.studentGroupsAndClass.remove(Koeko.studentGroupsAndClass.size() - 1);
        }
        for (int i = 0; i < groups.size(); i++) {
            if (i == 0) {
                Koeko.studentGroupsAndClass.get(0).setClassName(groups.get(i));
            } else {
                Classroom newGroup = new Classroom();
                newGroup.setClassName(groups.get(i));
                Koeko.studentGroupsAndClass.add(newGroup);
            }
        }

        //clean ready questions list or assign them to the new class
        if (Koeko.studentGroupsAndClass.get(0).getActiveIDs().size() > 0) {
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
            ArrayList<String> questionIds = DbTableRelationClassQuestion.getQuestionsIDsForClass(activeClass);
            for (String id : questionIds) {
                Koeko.studentGroupsAndClass.get(0).getActiveIDs().add(id);
                Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(id);
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
            for (Student singleStudent : Koeko.studentGroupsAndClass.get(0).getStudents()) {
                studentsList.add(singleStudent.getName());
            }
            if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
                ArrayList<Student> studentsInGroup = DbTableClasses.getStudentsInClass(groupsCombobox.getSelectionModel().getSelectedItem().toString());
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

    public void activateTestSynchroneousQuestions() {
        if (Long.valueOf(readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID()) < 0) {
            String group = "";
            if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
                group = groupsCombobox.getSelectionModel().getSelectedItem().toString();
            }
            String testName = readyQuestionsList.getSelectionModel().getSelectedItem().getQuestion();
            ArrayList<Student> students = new ArrayList<>();
            if (group.length() > 0) {
                students = DbTableClasses.getStudentsInClass(group);
            } else {
                students = DbTableClasses.getStudentsInClass(activeClass);
            }

            //if there is a problem with the students
            if (students.size() == 0) {
                students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents();
            }

            NetworkCommunication.networkCommunicationSingleton.sendQuestionID(readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID(), students);
        } else {
            System.out.println("No test is selected");
        }
    }

    public void changeUI() {
        if (uiChoiceBox.getSelectionModel().getSelectedIndex() == 0) {
            questionSendingAccordion.setVisible(false);
            Koeko.questionBrowsingControllerSingleton.browseSubjectsAccordion.setVisible(false);
            Koeko.studentsVsQuestionsTableControllerSingleton.editEvalButton.setVisible(false);
            Koeko.studentsVsQuestionsTableControllerSingleton.tableAccordion.setVisible(false);
            DbTableSettings.insertUIMode(0);
        } else {
            questionSendingAccordion.setVisible(true);
            Koeko.questionBrowsingControllerSingleton.browseSubjectsAccordion.setVisible(true);
            Koeko.studentsVsQuestionsTableControllerSingleton.editEvalButton.setVisible(true);
            Koeko.studentsVsQuestionsTableControllerSingleton.tableAccordion.setVisible(true);
            DbTableSettings.insertUIMode(1);
        }
    }

    //OTHER METHODS
    public void loadQuestions() {
        readyQuestionsList.getItems().clear();
        Integer group = groupsCombobox.getSelectionModel().getSelectedIndex();
        if (group < 1) group = 0;

        ArrayList<String> questionIds = Koeko.studentGroupsAndClass.get(group).getActiveIDs();
        //for (int i = 0; i < IDsFromBroadcastedQuestionsSize; i++) {
        //    deactivateQuestion(0);
        //}
        //Koeko.studentGroupsAndClass.get(group).getActiveIDs().clear();
        if (Koeko.studentGroupsAndClass.get(group).getActiveIDs().size() == 0 && groupsCombobox.getSelectionModel().getSelectedItem() != null) {
            Koeko.studentGroupsAndClass.get(group).setActiveIDs(DbTableRelationClassQuestion.getQuestionsIDsForClass(groupsCombobox.getSelectionModel().getSelectedItem().toString()));
        } else {
            System.out.println("no active id or groupsCombobox selectedItem is null");
        }
        //refreshReadyQuestionsList(group);
        for (int i = 0; i < questionIds.size(); i++) {
            //Koeko.studentGroupsAndClass.get(group).getActiveIDs().add(questionIds.get(i));
            Integer typeOfQuestion = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(questionIds.get(i)));
            if (typeOfQuestion == 0) {
                try {
                    QuestionMultipleChoice questionMultipleChoice =
                            DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionIds.get(i));
                    QuestionGeneric questionGeneric = new QuestionGeneric();
                    questionGeneric.setQuestion(questionMultipleChoice.getQUESTION());
                    questionGeneric.setGlobalID(questionMultipleChoice.getID());
                    questionGeneric.setIntTypeOfQuestion(QuestionGeneric.MCQ);
                    questionGeneric.setImagePath(questionMultipleChoice.getIMAGE());
                    //sendQuestionToStudentsNoDuplicateCheck(questionGeneric);
                    readyQuestionsList.getItems().add(questionGeneric);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (typeOfQuestion == 1) {
                QuestionShortAnswer questionShortAnswer =
                        DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionIds.get(i));
                QuestionGeneric questionGeneric = new QuestionGeneric();
                questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
                questionGeneric.setGlobalID(questionShortAnswer.getID());
                questionGeneric.setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);
                questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
                //sendQuestionToStudentsNoDuplicateCheck(questionGeneric);
                readyQuestionsList.getItems().add(questionGeneric);
            } else {
                //it is a test
                Test test = DbTableTest.getTestWithID(Koeko.studentGroupsAndClass.get(0).getActiveIDs().get(i));
                QuestionGeneric questionGeneric = new QuestionGeneric();
                questionGeneric.setQuestion(test.getTestName());
                questionGeneric.setGlobalID(QuestionGeneric.changeIdSign(test.getIdTest()));
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
        Vector<String> oldIDs = new Vector<>();
        for (QuestionGeneric questionGeneric : formerQuestions) {
            oldIDs.add(questionGeneric.getGlobalID());
        }

        //remove all questions from broadcasted list
        //readyQuestionsList.getItems().remove(0,readyQuestionsList.getItems().size());

        //compare old and new questions and delete or add them to the table
        for (int i = 0; i < oldIDs.size(); i++) {
            if (!Koeko.studentGroupsAndClass.get(group).getActiveIDs().contains(oldIDs.get(i))) {
                NetworkCommunication.networkCommunicationSingleton.removeQuestion(i);
                readyQuestionsList.getItems().remove(i);
            }
        }

        //fill readyQuestionsList with new ids from activeIDs
        Boolean oneQuestionSent = false;
        for (int i = 0; i < Koeko.studentGroupsAndClass.get(group).getActiveIDs().size(); i++) {
            if (!oldIDs.contains(Koeko.studentGroupsAndClass.get(group).getActiveIDs().get(i))) {
                Integer typeOfQuestion = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(Koeko.studentGroupsAndClass.get(group).getActiveIDs().get(i)));
                if (typeOfQuestion == 0) {
                    try {
                        QuestionMultipleChoice questionMultipleChoice =
                                DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(Koeko.studentGroupsAndClass.get(group).getActiveIDs().get(i));
                        QuestionGeneric questionGeneric = new QuestionGeneric();
                        questionGeneric.setQuestion(questionMultipleChoice.getQUESTION());
                        questionGeneric.setGlobalID(questionMultipleChoice.getID());
                        questionGeneric.setIntTypeOfQuestion(QuestionGeneric.MCQ);
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
                            DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(Koeko.studentGroupsAndClass.get(0).getActiveIDs().get(i));
                    QuestionGeneric questionGeneric = new QuestionGeneric();
                    questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
                    questionGeneric.setGlobalID(questionShortAnswer.getID());
                    questionGeneric.setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);
                    questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
                    if (oneQuestionSent) {
                        sendQuestionToStudentsNoDuplicateCheck(questionGeneric, false);
                    } else {
                        sendQuestionToStudentsNoDuplicateCheck(questionGeneric, true);
                        oneQuestionSent = true;
                    }
                } else {
                    //it is a test
                    Test test = DbTableTest.getTestWithID(Koeko.studentGroupsAndClass.get(0).getActiveIDs().get(i));
                    QuestionGeneric questionGeneric = new QuestionGeneric();
                    questionGeneric.setQuestion(test.getTestName());
                    questionGeneric.setGlobalID(QuestionGeneric.changeIdSign(test.getIdTest()));
                    sendTestToStudents(questionGeneric, groupsCombobox.getSelectionModel().getSelectedIndex(), allQuestionsTree.getSelectionModel().getSelectedItem());
                }
            }
        }
    }

    private void sendQuestionToStudents(QuestionGeneric questionGeneric, Integer group, Boolean actualSending, Boolean silenceQuestionCollision) {
        String globalID = questionGeneric.getGlobalID();
        if (group < 1) group = 0;
        //check for presence of question
        if (!Koeko.studentGroupsAndClass.get(group).getActiveIDs().contains(globalID)) {
            readyQuestionsList.getItems().add(questionGeneric);
            Koeko.studentGroupsAndClass.get(group).getActiveIDs().add(globalID);
            if (!Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(globalID)) {
                Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(globalID);
            }
            if (questionGeneric.getIntTypeOfQuestion() == 0) {
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

    private void sendTestToStudents(QuestionGeneric questionGeneric, Integer group, TreeItem<QuestionGeneric> testItem) {
        ArrayList<String> questionIDs = new ArrayList<>();
        String globalID = questionGeneric.getGlobalID();
        if (group < 1) group = 0;

        //one send the test
        if (!Koeko.studentGroupsAndClass.get(group).getActiveIDs().contains(globalID)) {
            readyQuestionsList.getItems().add(questionGeneric);
            Koeko.studentGroupsAndClass.get(group).getActiveIDs().add(globalID);
            if (!Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(globalID)) {
                Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(globalID);
            }
            questionIDs = NetworkCommunication.networkCommunicationSingleton.sendTestWithID(QuestionGeneric.changeIdSign(globalID), null);
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
                questionIDs = NetworkCommunication.networkCommunicationSingleton.sendTestWithID(QuestionGeneric.changeIdSign(globalID), null); //do I need that?

            }
        }
        //add the id to the stored questions on devices for all students
        for (Student student : Koeko.studentGroupsAndClass.get(0).getStudents()) {
            student.getDeviceQuestions().add(globalID);
        }

        //send questions linked to the test
        for (String questionID : questionIDs) {
            QuestionGeneric questionGeneric2 = new QuestionGeneric();
            Boolean found = false;
            for (int i = 0; i < genericQuestionsList.size() && !found; i++) {
                if (genericQuestionsList.get(i).getGlobalID().contentEquals(questionID)) {
                    found = true;
                    questionGeneric2 = genericQuestionsList.get(i);
                    sendQuestionToStudents(questionGeneric2, groupsCombobox.getSelectionModel().getSelectedIndex(), true, true);
                }
            }

            //add a relation in the database between the class/group and the question in case it's not yet here
            if (groupsCombobox.getSelectionModel().getSelectedItem() != null) {
                DbTableRelationClassQuestion.addClassQuestionRelation(groupsCombobox.getSelectionModel().getSelectedItem().toString(),
                        String.valueOf(questionID));
            }
        }

        //send the media file linked to the test
        String mediaFileName = DbTableTest.getMediaFileName(globalID);
        if (mediaFileName.length() > 0) {
            File mediaFile = FilesHandler.getMediaFile(mediaFileName);
            NetworkCommunication.networkCommunicationSingleton.sendMediaFile(mediaFile, null);
        }

        //if it's a game, send the questions sets linked to it
        if (testItem.getValue().getIntTypeOfQuestion() == QuestionGeneric.GAME) {
            for (TreeItem<QuestionGeneric> treeItem : testItem.getChildren()) {
                sendTestToStudents(treeItem.getValue(), group, treeItem);
            }
            openGameController();
        }
    }

    private void openGameController() {
        //open game controlling window
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/GameView.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Game");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    /* TODO: fix need to use the no duplicate */
    private void sendQuestionToStudentsNoDuplicateCheck(QuestionGeneric questionGeneric, Boolean actualSending) {
        String globalID = questionGeneric.getGlobalID();
        readyQuestionsList.getItems().add(questionGeneric);
        if (questionGeneric.getIntTypeOfQuestion() == 0) {
            try {
                broadcastQuestionMultipleChoice(DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(globalID), actualSending);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            broadcastQuestionShortAnswer(DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(globalID), actualSending);
        }
    }

    public void insertQuestionView(QuestionView questionMultipleChoiceView) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                QuestionGeneric questionGeneric = new QuestionGeneric();
                questionGeneric.setGlobalID(questionMultipleChoiceView.getID());
                questionGeneric.setQuestion(questionMultipleChoiceView.getQUESTION());
                questionGeneric.setImagePath(questionMultipleChoiceView.getIMAGE());
                questionGeneric.setIntTypeOfQuestion(questionMultipleChoiceView.getTYPE());
                genericQuestionsList.add(questionGeneric);
                Node questionImage = null;
                questionImage = new ImageView(new Image("file:" + questionMultipleChoiceView.getIMAGE(), 20, 20, true, false));
                TreeItem<QuestionGeneric> itemChild;
                if (questionMultipleChoiceView.getIMAGE().length() < 1) {
                    itemChild = new TreeItem<>(questionGeneric);
                } else {
                    itemChild = new TreeItem<>(questionGeneric, questionImage);
                }
                allQuestionsTree.getRoot().getChildren().add(itemChild);
            }
        });
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
        QuestionMultipleChoice new_questmultchoice = new QuestionMultipleChoice("1", question[1], options_vector.get(0),
                options_vector.get(1), options_vector.get(2), options_vector.get(3), options_vector.get(4),
                options_vector.get(5), options_vector.get(6), options_vector.get(7), options_vector.get(8),
                options_vector.get(9), question[4]);
        new_questmultchoice.setNB_CORRECT_ANS(number_correct_answers);
        new_questmultchoice.setTimerSeconds(-1);

        //copy image file to correct directory
        if (new_questmultchoice.getIMAGE().length() > 0 && !new_questmultchoice.getIMAGE().contains("none")) {
            File source = new File(FilesHandler.exportDirectory + FilesHandler.mediaDirectory + new_questmultchoice.getIMAGE());
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
        questionGeneric.setIntTypeOfQuestion(QuestionGeneric.MCQ);
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
                DbTableRelationQuestionSubject.addRelationQuestionSubject(subjects[i]);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        //adding objectives relations
        String[] objectives = question[6].split("///");
        for (int i = 0; i < objectives.length; i++) {
            try {
                DbTableRelationQuestionObjective.addRelationQuestionObjective(objectives[i]);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void insertQuestionShortAnswer(String[] question) {
        QuestionShortAnswer new_questshortanswer = new QuestionShortAnswer();
        new_questshortanswer.setQUESTION(question[1]);
        if (question[4].length() > 0) {
            new_questshortanswer.setIMAGE(question[4]);
        }
        ArrayList<String> answerOptions = new ArrayList<String>();
        String[] rightAnswers = question[2].split("///");
        for (int i = 0; i < rightAnswers.length; i++) {
            String answerOption = rightAnswers[i];
            if (answerOption.length() > 0) {
                answerOptions.add(answerOption);
            }
        }
        new_questshortanswer.setANSWER(answerOptions);
        new_questshortanswer.setTimerSeconds(-1);

        //copy image file to correct directory
        if (new_questshortanswer.getIMAGE().length() > 0 && !new_questshortanswer.getIMAGE().contains("none")) {
            File source = new File(FilesHandler.exportDirectory + FilesHandler.mediaDirectory + new_questshortanswer.getIMAGE());
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
        new_questshortanswer.setID(idGlobal);


        //put the question in the treeView
        QuestionGeneric questionGeneric = new QuestionGeneric(new_questshortanswer.getID(), 1);
        questionGeneric.setQuestion(new_questshortanswer.getQUESTION());
        questionGeneric.setImagePath(new_questshortanswer.getIMAGE());
        questionGeneric.setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);
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
                DbTableRelationQuestionSubject.addRelationQuestionSubject(subjects[i]);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        //adding objectives relations
        String[] objectives = question[6].split("///");
        for (int i = 0; i < objectives.length; i++) {
            try {
                DbTableRelationQuestionObjective.addRelationQuestionObjective(objectives[i]);
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