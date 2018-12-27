package koeko.controllers.LeftBar;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import koeko.controllers.CreateClassController;
import koeko.controllers.EditEvaluationController;
import koeko.controllers.GenericPopUpController;
import koeko.controllers.StudentsVsQuestions.ChooseTestController;
import koeko.controllers.StudentsVsQuestions.CreateStudentController;
import koeko.controllers.SubjectsBrowsing.CreateSubjectController;
import koeko.controllers.SubjectsBrowsing.EditSubjectController;
import koeko.controllers.SubjectsBrowsing.SubjectTreeCell;
import koeko.controllers.controllers_tools.SingleStudentAnswersLine;
import koeko.database_management.*;
import koeko.questions_management.QuestionGeneric;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
import koeko.view.Subject;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

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

    public void initialize(URL location, ResourceBundle resources) {
        Koeko.leftBarController = this;
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
        subject.set_subjectName("All subjects");
        rootSubjectSingleton = new TreeItem<>(subject);
        rootSubjectSingleton.setExpanded(true);
        subjectsTree.setShowRoot(true);
        Task<Void> loadQuestions = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                populateSubjectTree(rootSubjectSingleton);
                return null;
            }
        };
        new Thread(loadQuestions).start();
        subjectsTree.setRoot(rootSubjectSingleton);
        subjectsTree.setCellFactory(stringTreeView -> {
            SubjectTreeCell treeCell = new SubjectTreeCell();

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
            Platform.runLater(() -> labelIP.setText("students should connect \nto the following address:\n" + ipAddresses.get(0)));
            if (Koeko.recordLogs) {
                DbTableLogs.insertLog("IPs", ipAddresses.get(0));
            }
        } else if (ipAddresses.size() == 2) {
            Platform.runLater(() -> labelIP.setText("students should connect \nto the following addresses:\n" + ipAddresses.get(0) +
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
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateSubjectController controller = fxmlLoader.getController();

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Subject");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void editSubject() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSubject.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditSubjectController controller = fxmlLoader.getController();
        controller.initializeSubject(subjectsTree.getSelectionModel().getSelectedItem().getValue().get_subjectName(), subjectsTree);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Edit the Subject");
        stage.setScene(new Scene(root1));
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
        if (subject.get_subjectName().contentEquals("All subjects")) {
            questionIds = DbTableQuestionGeneric.getAllGenericQuestionsIds();
        } else {
            questionIds = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subject.get_subjectName());
        }
        Koeko.questionSendingControllerSingleton.populateTree(questionIds);
    }

    public void promptGenericPopUp(String message, String title) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/GenericPopUp.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenericPopUpController controller = fxmlLoader.getController();
        controller.initParameters(message);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(title);
        stage.setScene(new Scene(root1));
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
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CreateStudentController controller = fxmlLoader.<CreateStudentController>getController();
            controller.initClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Edit Evaluation");
            stage.setScene(new Scene(root1));
            stage.show();
        } else {
            promptGenericPopUp("No class is currently selected", "No Class");
        }
    }

    public void createClass() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateClass.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateClassController controller = fxmlLoader.<CreateClassController>getController();
        controller.initializeParameters(chooseClassComboBox);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Create a New Class");
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void saveStudentsToClass() {
        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
            for (int i = 0; i < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().size() - 1; i++) {
                String studentName = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudent();
                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
                DbTableRelationClassStudent.addClassStudentRelation(className, studentName);
            }
        }
    }

    public void removeStudentFromClass() {
        removeStudentFromClass(0);
    }
    public void removeStudentFromClass(Integer group) {
        if (!Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudent().contentEquals("CLASS")) {
            //adapt table height
            Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(group).setPrefHeight(Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(group).getPrefHeight() - cellHeight * 1.1);

            String studentName = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudent();
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
                            System.out.println(t.getRowValue().getStudent());
                            System.out.println(t.getTableColumn().getText());
                            String idObjective = DbTableLearningObjectives.getObjectiveIdFromName(t.getTableColumn().getText());
                            DbTableIndividualQuestionForStudentResult.addIndividualObjectiveForStudentResult(idObjective,
                                    t.getRowValue().getStudent(),t.getNewValue(),"CERTIFICATIVE", chooseTestCombo.getSelectionModel().getSelectedItem().toString());
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
                    String eval = DbTableIndividualQuestionForStudentResult.getResultForStudentForObjectiveInTest(singleStudentAnswersLine.getStudent(),
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
                String studentName = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getStudent();
                for (int j = 3; j < Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().size(); j++) {
                    String objective = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getColumns().get(j).getText();
                    String evaluation = Koeko.studentsVsQuestionsTableControllerSingleton.tableViewArrayList.get(0).getItems().get(i).getAnswers().get(j - 3).getValue();
                    NetworkCommunication.networkCommunicationSingleton.sendTestEvaluation(studentName, test, objective, evaluation);
                }
            }
        }
    }

    public void launchChooseTest() {
        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/ChooseTest.fxml"));
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChooseTestController controller = fxmlLoader.<ChooseTestController>getController();
            controller.initializeParameters(chooseTestCombo, chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Assign a Certificative Test to the Class");
            stage.setScene(new Scene(root1));
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
            Parent root1 = null;
            try {
                root1 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            EditEvaluationController controller = fxmlLoader.<EditEvaluationController>getController();
            controller.initializeVariable(globalID, studentID);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle("Edit Evaluation");
            stage.setScene(new Scene(root1));
            stage.show();
        }
    }
}
