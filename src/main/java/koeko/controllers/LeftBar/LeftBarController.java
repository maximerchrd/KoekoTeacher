package koeko.controllers.LeftBar;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.Tools.FilesHandler;
import koeko.controllers.EditEvaluationController;
import koeko.controllers.GenericPopUpController;
import koeko.controllers.LeftBar.ClassesControlling.ClassesTreeTasks;
import koeko.controllers.controllers_tools.ControllerUtils;
import koeko.view.Homework;
import koeko.controllers.LeftBar.HomeworkControlling.HomeworkListTasks;
import koeko.controllers.StudentsVsQuestions.ChooseTestController;
import koeko.controllers.StudentsVsQuestions.CreateStudentController;
import koeko.controllers.SubjectsBrowsing.CreateSubjectController;
import koeko.controllers.SubjectsBrowsing.EditSubjectController;
import koeko.controllers.SubjectsBrowsing.SubjectTreeCell;
import koeko.controllers.controllers_tools.SingleStudentAnswersLine;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
import koeko.view.Subject;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static koeko.controllers.controllers_tools.ControllerUtils.openFXMLResource;

/**
 * Created by maximerichard on 13.03.18.
 */
public class LeftBarController extends Window implements Initializable {
    static public TreeItem<Subject> rootSubjectSingleton;
    public ArrayList<String> ipAddresses;
    private Subject draggedSubject;
    private TreeItem<Subject> draggedItem;

    private Vector<Subject> subjects;
    private Vector<String> subjectsIds;
    private Vector<String> parentIds;
    private Vector<String> childIds;

    private final Double cellHeight = 25.0;

    @FXML private Label labelIP;
    @FXML public TreeView<Subject> subjectsTree;
    @FXML public Accordion browseSubjectsAccordion;

    @FXML private ComboBox chooseClassComboBox;
    @FXML private ComboBox chooseTestCombo;
    @FXML public Button editEvalButton;

    @FXML private TreeView<Classroom> classesTree;
    public TreeItem<Classroom> rootClassSingleton;

    @FXML public ListView<Homework> homeworksList;

    @FXML private ComboBox uiChoiceBox;

    private ResourceBundle bundle;

    public void initialize(URL location, ResourceBundle resources) {
        Koeko.leftBarController = this;
        bundle = resources;
        ipAddresses = new ArrayList<>();

        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                getAndDisplayIpAddress();

                return null;
            }
        };
        new Thread(getIPTask).start();


        //build the subjects tree
        subjectsTree.getStylesheets().add("/style/treeview.css");

        //create rootSubjectSingleton
        Subject subject = new Subject();
        subject.set_subjectName(bundle.getString("string.all_subjects"));
        rootSubjectSingleton = new TreeItem<>(subject);
        rootSubjectSingleton.setExpanded(true);
        subjectsTree.setShowRoot(true);
        Task<Void> loadSubjects = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                populateSubjectTree(rootSubjectSingleton);
                return null;
            }
        };
        new Thread(loadSubjects).start();
        subjectsTree.setRoot(rootSubjectSingleton);
        subjectsTree.setCellFactory(stringTreeView -> {
            SubjectTreeCell treeCell = new SubjectTreeCell(bundle);

            treeCell.setOnDragDetected(mouseEvent -> {
                draggedSubject = treeCell.getTreeItem().getValue();
                draggedItem = treeCell.getTreeItem();
                Dragboard db = subjectsTree.startDragAndDrop(TransferMode.ANY);

                /* Put a string on a dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(treeCell.getText());
                db.setContent(content);

                mouseEvent.consume();
            });

            treeCell.setOnDragOver(event -> {
                /* data is dragged over the target */
                /* accept it only if it is not dragged from the same node
                 * and if it has a string data */
                if (event.getGestureSource() != treeCell &&
                        event.getDragboard().hasString()) {
                    //set the type of dropping: MOVE AND/OR COPY
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            treeCell.setOnDragEntered(event -> {
                /* the drag-and-drop gesture entered the target */
                /* show to the user that it is an actual gesture target */
                if (event.getGestureSource() != treeCell &&
                        event.getDragboard().hasString()) {
                    //treeCell.setStyle(String.format("-fx-background-color: green"));
                    treeCell.setTextFill(Color.LIGHTGREEN);
                }
                event.consume();
            });
            treeCell.setOnDragExited(event -> {
                /* mouse moved away, remove the graphical cues */
                //treeCell.setStyle(String.format("-fx-background-color: white"));
                treeCell.setTextFill(Color.BLACK);
                event.consume();
            });


            treeCell.setOnDragDropped(event -> {
                /* data dropped */
                if (!treeCell.getTreeItem().getValue().get_subjectName().contentEquals(draggedSubject.get_subjectName())) {
                    DbTableRelationSubjectSubject.addRelationSubjectSubject(treeCell.getTreeItem().getValue().get_subjectName(),draggedItem.getValue().get_subjectName(),
                            draggedItem.getParent().getValue().get_subjectName());
                    draggedItem.getParent().getChildren().remove(draggedItem);
                    treeCell.getTreeItem().getChildren().add(draggedItem);
                } else {
                    System.out.println("Trying to drag on self");
                }
                draggedSubject = null;
            });


            return treeCell;
        });

        subjectsTree.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                //filter with subject
            }
        });

        ClassesTreeTasks.populateClassesTree(classesTree, bundle.getString("string.all_classes"), bundle);

        //setup UI choicebox
        uiChoiceBox.setItems(FXCollections.observableArrayList(
                bundle.getString("string.basic_commands"), bundle.getString("string.advanced_commands"))
        );
        int uiMode = DbTableSettings.getUIMode();
        uiChoiceBox.getSelectionModel().select(uiMode);

        HomeworkListTasks.initHomeworkList(homeworksList);

        homeworksList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayQuestionsCorrespondingToHomework());
    }

    private void getAndDisplayIpAddress() throws SocketException, UnknownHostException {
        ipAddresses.removeAll(ipAddresses);
        ArrayList<String> potentialIpAddresses = new ArrayList<>();
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                if (n.getName().contains("wlan") && !i.getHostAddress().contains(":")) {
                    ipAddresses.add(i.getHostAddress());
                } else if (!i.getHostAddress().contains(":") && i.getHostAddress().contains("192.168.")) {
                    potentialIpAddresses.add(i.getHostAddress());
                }
            }
        }
        if (ipAddresses.size() == 0) {
            if (potentialIpAddresses.size() == 1) {
                ipAddresses.addAll(potentialIpAddresses);
            } else {
                ipAddresses.add(InetAddress.getLocalHost().getHostAddress());
            }
        }
        if (ipAddresses.size() == 1) {
            Platform.runLater(() -> labelIP.setText(bundle.getString("leftbar.label_ip") + ipAddresses.get(0)));
            if (Koeko.recordLogs) {
                DbTableLogs.insertLog("IPs", ipAddresses.get(0));
            }
        } else if (ipAddresses.size() == 2) {
            Platform.runLater(() -> labelIP.setText(bundle.getString("leftbar.label_ip") + ipAddresses.get(0) +
                    "\nand " + ipAddresses.get(1)));
            if (Koeko.recordLogs) {
                DbTableLogs.insertLog("IPs", ipAddresses.get(0) + "/" + ipAddresses.get(1));
            }
        }
    }

    private void populateSubjectTree(TreeItem<Subject> root) {
        //BEGIN retrieve data from the db and prepare the vectors
        subjects = DbTableSubject.getAllSubjects();
        subjectsIds = new Vector<>();
        for (Subject subject : subjects) {
            String subjectID = "";
            if (subject.get_subjectMUID() != null && !subject.get_subjectMUID().contentEquals("")) {
                subjectID = subject.get_subjectMUID();
            } else {
                subjectID = String.valueOf(subject.get_subjectId());
            }
            subjectsIds.add(String.valueOf(subjectID));
        }

        Vector<Vector<String>> idsRelationPairs = DbTableRelationSubjectSubject.getAllSubjectIDsRelations();
        parentIds = new Vector<>();
        childIds = new Vector<>();
        for (Vector<String> pair : idsRelationPairs) {
            parentIds.add(pair.get(0));
            childIds.add(pair.get(1));
        }

        Vector<String> topSubjects = new Vector<>();
        for (String subjectId : subjectsIds) {
            if (!childIds.contains(subjectId)) {
                topSubjects.add(subjectId);
            }
        }
        //END retrieve data from the db and prepare the vectors

        for (String subjectId : topSubjects) {
            Subject subject = subjects.get(subjectsIds.indexOf(subjectId));
            TreeItem subjectTreeItem = new TreeItem<>(subject);
            root.getChildren().add(subjectTreeItem);

            populateWithChildren(subjectId, subjectTreeItem);
        }
    }

    private void populateWithChildren(String subjectID, TreeItem<Subject> subjectTreeItem) {
        Vector<String> childrenIds = new Vector<>();
        for (int i = 0; i < parentIds.size(); i++) {
            if (parentIds.get(i).contentEquals(subjectID)) {
                childrenIds.add(childIds.get(i));
            }
        }

        //
        for (String childrenId : childrenIds) {
            Subject subject = subjects.get(subjectsIds.indexOf(childrenId));
            TreeItem<Subject> newItem = new TreeItem<>(subject);
            subjectTreeItem.getChildren().add(newItem);
            populateWithChildren(childrenId, newItem);
        }
    }

    public void refreshIP() {
        Task<Void> getIPTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                getAndDisplayIpAddress();
                return null;
            }
        };
        new Thread(getIPTask).start();
    }

    public void createSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateSubject.fxml"));
        Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);
        CreateSubjectController controller = fxmlLoader.getController();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(bundle.getString("string.create_subject"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void editSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSubject.fxml"));
        Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);
        EditSubjectController controller = fxmlLoader.getController();
        controller.initializeSubject(subjectsTree.getSelectionModel().getSelectedItem().getValue().get_subjectName(), subjectsTree);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Edit the Subject");
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void deleteSubject() {
        if (subjectsTree.getSelectionModel().getSelectedItem() != null) {
            if (subjectsTree.getSelectionModel().getSelectedItem().getChildren().size() == 0) {
                Subject subject = subjectsTree.getSelectionModel().getSelectedItem().getValue();
                DbTableRelationSubjectSubject.deleteRelationWhereSubjectIsChild(subject.get_subjectName());
                DbTableRelationQuestionSubject.removeRelationWithSubject(subject.get_subjectName());
                DbTableSubject.deleteSubject(subject.get_subjectName());
                TreeItem<Subject> itemToDelete = subjectsTree.getSelectionModel().getSelectedItem();
                itemToDelete.getParent().getChildren().remove(itemToDelete);
            } else {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(this);
                VBox dialogVbox = new VBox(20);
                dialogVbox.getChildren().add(new Text("Sorry, it is not possible to delete a subject with sub-subject(s)."));
                Scene dialogScene = new Scene(dialogVbox, 400, 40);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        }
    }

    public void filterQuestionsWithSubject() {
        Subject subject = subjectsTree.getSelectionModel().getSelectedItem().getValue();
        Vector<String> questionIds;
        if (subject.get_subjectName().contentEquals(bundle.getString("string.all_subjects"))) {
            questionIds = DbTableQuestionGeneric.getAllGenericQuestionsIds();
        } else {
            questionIds = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subject.get_subjectName());
        }
        Koeko.questionSendingControllerSingleton.populateTree(questionIds);
    }

    public void promptGenericPopUp(String message, String title) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/GenericPopUp.fxml"));
        Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);
        GenericPopUpController controller = fxmlLoader.getController();
        controller.initParameters(message);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(title);
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void loadClass() {
        String activeClass = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
        Koeko.questionSendingControllerSingleton.activeClassChanged(activeClass);

        chooseTestCombo.getItems().clear();
        ArrayList<String> tests = DbTableRelationClassTest.getTestsForClass(activeClass);
        chooseTestCombo.getItems().add("No test");
        for (String test : tests) {
            chooseTestCombo.getItems().add(test);
        }

        loadGroups();
    }

    public void loadGroups() {
        ArrayList<String> groups = DbTableClasses.getGroupsFromClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
        groups.add(0,chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
        Integer nbGroups = Koeko.studentGroupsAndClass.size();
        //Integer nbGroupsToAdd = groups.size() - groupsAlreadyAdded;

        int groupsLoaded = 0;
        if (Koeko.studentsVsQuestionsTableControllerSingleton.tableVBox.getChildren().size() > 1) {
            int vboxSize = Koeko.studentsVsQuestionsTableControllerSingleton.tableVBox.getChildren().size();
            Koeko.studentsVsQuestionsTableControllerSingleton.tableVBox.getChildren().remove(1, vboxSize);
            groupsLoaded = 1;
        }

        for (int i = 0; i < nbGroups; i++) {
            if (i > 0) {
                Koeko.studentsVsQuestionsTableControllerSingleton.addGroup(groups.get(i), i);
            }

            //add studentGroupsAndClass for group
            ArrayList<Student> students = DbTableClasses.getStudentsInClass(groups.get(i));
            for (Student student : students) {
                Koeko.studentsVsQuestionsTableControllerSingleton.addUser(student, false, i);
            }
        }
    }

    public void addNewStudentToClass() {
        if (chooseClassComboBox.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateStudent.fxml"));
            Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);
            CreateStudentController controller = fxmlLoader.<CreateStudentController>getController();
            controller.initClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Edit Evaluation");
            stage.setScene(new Scene(parent));
            stage.show();
        } else {
            promptGenericPopUp("No class is currently selected", "No Class");
        }
    }

    public void saveStudentsToClass() {
        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
            for (int i = 0; i < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size() - 1; i++) {
                String studentName = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudentName();
                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
                DbTableRelationClassStudent.addClassStudentRelation(className, studentName);
            }
        }
    }

    public void removeStudentFromClass(Integer group) {
        if (!Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudentName().contentEquals("CLASS")) {
            //adapt table height
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(group).setPrefHeight(Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(group).getPrefHeight() - cellHeight * 1.1);

            String studentName = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudentName();
            if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
                try {
                    DbTableRelationClassStudent.removeStudentFromClass(studentName, className);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int studentIndex = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getSelectionModel().getSelectedIndex();
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().remove(Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getSelectionModel().getSelectedItem());
            Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().remove(studentIndex);
            if (Koeko.studentGroupsAndClass.get(group).getStudents().size() > studentIndex &&
                    studentName.contentEquals(Koeko.studentGroupsAndClass.get(group).getStudents().get(studentIndex).getName())) {
                Koeko.studentGroupsAndClass.get(group).getStudents().remove(studentIndex);
            }
        }
    }

    public void removeStudentFromClass(String studentName, int index) {
        Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).setPrefHeight(Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getPrefHeight() - cellHeight * 1.1);

        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
            String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
            try {
                DbTableRelationClassStudent.removeStudentFromClass(studentName, className);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().remove(Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(index));
        Koeko.studentGroupsAndClass.get(0).getActiveEvaluations().remove(index);
        if (Koeko.studentGroupsAndClass.get(0).getStudents().size() > index &&
                studentName.contentEquals(Koeko.studentGroupsAndClass.get(0).getStudents().get(index).getName())) {
            Koeko.studentGroupsAndClass.get(0).getStudents().remove(index);
        }
    }

    public void certificativeTestSelected() {

        //remove questions or objectives columns if some are present
        String firstQuestion = "";
        if (Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().size() > 0 &&
                Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().get(0) != null) {
            firstQuestion = Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().get(0).getQuestion();

            //remove questions if some are present in the ready list
            Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().clear();
        }
        if (Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().size() > 3 && !Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().get(3).getText().contentEquals(firstQuestion)) {
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().remove(3, Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().size());
        } else {
            while (Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().size() > 3) {
                Koeko.studentsVsQuestionsTableControllerSingleton.removeQuestion(0);
            }
        }

        //load questions if the selected certificative test is "No test"
        if (chooseTestCombo.getSelectionModel().getSelectedItem().toString().contentEquals("No test")) {
            for (QuestionGeneric questionGeneric : Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems()) {
                Koeko.studentsVsQuestionsTableControllerSingleton.addQuestion(questionGeneric.getQuestion(), questionGeneric.getGlobalID(), 0);
            }
        }

        //BEGIN display objectives as questions in table
        ArrayList<String> objectives = DbTableRelationObjectiveTest.getObjectivesFromTestName(chooseTestCombo.getSelectionModel().getSelectedItem().toString());
        for (int k = 0; k < objectives.size(); k++) {
            TableColumn column = new TableColumn(objectives.get(k));
            column.setPrefWidth(180);
            column.setEditable(true);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<SingleStudentAnswersLine, String>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<SingleStudentAnswersLine, String> t) {
                            System.out.println(t.getRowValue().getStudentName());
                            System.out.println(t.getTableColumn().getText());
                            String idObjective = DbTableLearningObjectives.getObjectiveIdFromName(t.getTableColumn().getText());
                            DbTableIndividualQuestionForStudentResult.addIndividualObjectiveForStudentResult(idObjective,
                                    t.getRowValue().getStudentName(),t.getNewValue(),"CERTIFICATIVE", chooseTestCombo.getSelectionModel().getSelectedItem().toString());
                        }
                    }
            );
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).setEditable(true);
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().add(column);
            for (int i = 0; i < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size(); i++) {
                Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).addAnswer();
            }

            final int objectiveIndex = k;
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SingleStudentAnswersLine, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<SingleStudentAnswersLine, String> p) {
                    // p.getValue() returns the instance for a particular TableView row
                    return p.getValue().getAnswers().get(objectiveIndex);
                }
            });
        }
        //END display objectives as questions in table

        //BEGIN fill table with results for objectives
        if (!chooseTestCombo.getSelectionModel().getSelectedItem().toString().contentEquals("No test")) {
            ArrayList<Integer> objectivesIDs = DbTableRelationObjectiveTest.getObjectivesIDsFromTestName(chooseTestCombo.getSelectionModel().getSelectedItem().toString());

            for (int j = 0; j < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size(); j++) {
                SingleStudentAnswersLine singleStudentAnswersLine = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(j);
                for (int i = 0; i < objectivesIDs.size(); i++) {
                    String eval = DbTableIndividualQuestionForStudentResult.getResultForStudentForObjectiveInTest(singleStudentAnswersLine.getStudentName(),
                            String.valueOf(objectivesIDs.get(i)), chooseTestCombo.getSelectionModel().getSelectedItem().toString());
                    singleStudentAnswersLine.setAnswer(eval, i);
                }
            }
        } else {
            for (SingleStudentAnswersLine singleStudentAnswersLine : Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems()) {
                for (int i = 0; i < singleStudentAnswersLine.getAnswers().size(); i++) {
                    singleStudentAnswersLine.setAnswer("", i);
                }
            }
        }
        //END fill table with results for objectives
    }

    public void sendObjectiveEvaluationToStudents() {
        String test = chooseTestCombo.getSelectionModel().getSelectedItem().toString();
        if (test != null && !test.contentEquals("No test")) {
            for (int i = 0; i < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size() - 1; i++) {
                String studentName = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudentName();
                for (int j = 3; j < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().size(); j++) {
                    String objective = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().get(j).getText();
                    String evaluation = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getAnswers().get(j - 3).getValue();
                    try {
                        NetworkCommunication.networkCommunicationSingleton.sendTestEvaluation(studentName, test, objective, evaluation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void launchChooseTest() {
        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/ChooseTest.fxml"));
            Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);
            ChooseTestController controller = fxmlLoader.<ChooseTestController>getController();
            controller.initializeParameters(chooseTestCombo, chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Assign a Certificative Test to the Class");
            stage.setScene(new Scene(parent));
            stage.show();
        }
    }

    public void editEvaluation() {
        editEvaluation(0);
    }
    public void editEvaluation(Integer group) {
        TablePosition tablePosition = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getFocusModel().getFocusedCell();
        String globalID = Koeko.studentGroupsAndClass.get(group).getActiveIDs().get(tablePosition.getColumn() - 3);
        String studentID = Koeko.studentGroupsAndClass.get(group).getStudents().get(tablePosition.getRow()).getStudentID();
        if (Long.valueOf(globalID) >= 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditEvaluation.fxml"));
            Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);
            EditEvaluationController controller = fxmlLoader.<EditEvaluationController>getController();
            controller.initializeVariable(globalID, studentID);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Edit Evaluation");
            stage.setScene(new Scene(parent));
            stage.show();
        }
    }

    public void createHomework() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateHomework.fxml"));
        Parent parent = ControllerUtils.openFXMLResource(fxmlLoader);

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(bundle.getString("createhomework.create"));
        stage.setScene(new Scene(parent));
        stage.show();
    }

    public void sendCorrection() {
        try {
            NetworkCommunication.networkCommunicationSingleton.sendCorrection(Koeko.questionSendingControllerSingleton
                    .readyQuestionsList.getSelectionModel().getSelectedItem().getGlobalID());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    Koeko.questionSendingControllerSingleton.insertQuestionMultipleChoice(question);
                } else {
                    Koeko.questionSendingControllerSingleton.insertQuestionShortAnswer(question);
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
        Parent parent = openFXMLResource(fxmlLoader);
        GenericPopUpController controller = fxmlLoader.getController();
        controller.initParameters(importDoneMessage);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Import");
        stage.setScene(new Scene(parent));
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
        Parent parent = openFXMLResource(fxmlLoader);
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
        stage.setScene(new Scene(parent));
        stage.show();
    }

    private void displayQuestionsCorrespondingToHomework() {
        if (DbTableHomework.checkIfNameAlreadyExists(homeworksList.getSelectionModel().getSelectedItem().getName())) {
            ArrayList<String> questionIds = DbTableRelationHomeworkQuestion.getQuestionIdsFromHomeworkName(homeworksList.getSelectionModel().getSelectedItem().getName());
            Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().clear();
            Koeko.studentGroupsAndClass.get(0).getActiveIDs().clear();
            for (String questionId : questionIds) {
                QuestionGeneric questionGeneric;
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionId);
                if (questionMultipleChoice.getQUESTION().length() == 0) {
                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionId);
                    questionGeneric = new QuestionGeneric(questionShortAnswer);
                } else {
                    questionGeneric = new QuestionGeneric(questionMultipleChoice);
                }
                Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().add(questionGeneric);
                Koeko.studentGroupsAndClass.get(0).getActiveIDs().add(questionId);
            }
            Koeko.questionSendingControllerSingleton.readyQuestionsList.refresh();
        }
    }

    public void changeUI() {
        if (uiChoiceBox.getSelectionModel().getSelectedIndex() == 0) {
            browseSubjectsAccordion.setVisible(false);
            editEvalButton.setVisible(false);
            DbTableSettings.insertUIMode(0);
        } else {
            browseSubjectsAccordion.setVisible(true);
            editEvalButton.setVisible(true);
            DbTableSettings.insertUIMode(1);
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
}