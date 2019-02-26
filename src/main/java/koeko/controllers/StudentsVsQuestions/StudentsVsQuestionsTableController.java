package koeko.controllers.StudentsVsQuestions;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import koeko.Koeko;
import koeko.Networking.NetworkCommunication;
import koeko.controllers.controllers_tools.ControllerUtils;
import koeko.controllers.controllers_tools.SingleStudentAnswersLine;
import koeko.functionalTesting;
import koeko.questions_management.QuestionGeneric;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import koeko.database_management.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by maximerichard on 12.03.18.
 */
public class StudentsVsQuestionsTableController extends Window implements Initializable {
    public Boolean studentsCheckBoxes = false;
    private final Double cellHeight = 40.0;
    //private ArrayList<String> questions;
    //private ArrayList<Integer> questionsIDs;
    public ArrayList<TableView<SingleStudentAnswersLine>> tableViewArrayList;
    private Map<String,Integer> studentIdToStatusReceptionMap;
    private String columnIdentifiers = "";
    private MCQStats mcqStatsController;

    //@FXML private ComboBox chooseClassComboBox;
    @FXML public VBox tableVBox;
    @FXML public ScrollPane tablesScrollPane;

    //context menu for right click on row
    ContextMenu contextMenu = new ContextMenu();

    private ResourceBundle bundle;


    public void addQuestion(String question, String ID, Integer group) {
        if (Long.valueOf(ID) > 0) {
            if (group < 1) group = 0;
            Koeko.studentGroupsAndClass.get(group).getActiveQuestions().add(question);

            final int groupFinal = group;
            Platform.runLater(() -> {
                // Add extra columns if necessary:
                TableColumn column = new TableColumn();
                column.setPrefWidth(180);

                VBox vBox = new VBox(new Label(question));
                vBox.setAlignment(Pos.CENTER);
                int index = tableViewArrayList.get(groupFinal).getColumns().size();
                String columnIndex = groupFinal + "/" + index + "/" + question + "/" + ID;
                vBox.getProperties().put("index", columnIndex);
                column.setGraphic(vBox);
                column.setText("");

                //Add header onclick listener
                EventHandler<? super MouseEvent> handlerClick = event -> {
                    if (!columnIdentifiers.contentEquals("")) {
                        diplayQuestionStats(columnIdentifiers);
                    }
                };
                EventHandler<? super MouseEvent> handlerEnter = event -> {
                    columnIdentifiers = (String) ((Node) event.getTarget()).getProperties().get("index");
                };
                EventHandler<? super MouseEvent> handlerExit = event -> {
                    columnIdentifiers = "";
                };
                column.getGraphic().addEventFilter(MouseEvent.MOUSE_ENTERED, handlerEnter);
                column.getGraphic().addEventFilter(MouseEvent.MOUSE_CLICKED, handlerClick);
                column.getGraphic().addEventFilter(MouseEvent.MOUSE_EXITED, handlerExit);


                column.setSortable(false);
                tableViewArrayList.get(groupFinal).getColumns().add(column);

                //add the evaluations for the table
                for (int i = 0; i < tableViewArrayList.get(groupFinal).getItems().size(); i++) {
                    tableViewArrayList.get(groupFinal).getItems().get(i).addAnswer();
                    if (Koeko.studentGroupsAndClass.get(groupFinal).getActiveEvaluations() != null
                            && Koeko.studentGroupsAndClass.get(groupFinal).getActiveEvaluations().size() > i) {
                        Koeko.studentGroupsAndClass.get(groupFinal).getActiveEvaluations().get(i).add(-1.0);
                    }
                }
                Koeko.studentGroupsAndClass.get(groupFinal).getAverageEvaluations().add(0.0);


                final int questionIndex = Koeko.studentGroupsAndClass.get(groupFinal).getActiveQuestions().indexOf(question);
                column.setCellValueFactory((Callback<TableColumn.CellDataFeatures<SingleStudentAnswersLine, String>,
                        ObservableValue<String>>) p -> p.getValue().getAnswers().get(questionIndex));
                column.setCellFactory(new Callback<TableColumn<SingleStudentAnswersLine, String>, TableCell<SingleStudentAnswersLine, String>>() {
                    @Override
                    public TableCell<SingleStudentAnswersLine, String> call(TableColumn<SingleStudentAnswersLine, String> param) {
                        return new TableCell<SingleStudentAnswersLine, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (!isEmpty()) {
                                    this.setStyle("-fx-text-fill: red;");
                                    // Get fancy and change color based on data
                                    if (item.contains("#/#")) {
                                        this.setStyle("-fx-text-fill: green;");
                                    }
                                    setText(item.replace("#/#", ""));
                                }
                            }

                        };
                    }
                });
            });
        }
    }

    private void diplayQuestionStats(String groupColumnIndex) {
        Integer groupIndex = Integer.valueOf(groupColumnIndex.split("/")[0]);
        Integer columnIndex = Integer.valueOf(groupColumnIndex.split("/")[1]);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/MCQStats.fxml"));
        Parent root1 = ControllerUtils.openFXMLResource(fxmlLoader);
        mcqStatsController = fxmlLoader.getController();
        mcqStatsController.initParameters(tableViewArrayList, groupIndex, columnIndex, groupColumnIndex.split("/")[3]);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(groupColumnIndex.split("/")[2]);
        stage.setScene(new Scene(root1));
        stage.show();
    }

    public void removeQuestion(int index) {
        removeQuestion(index, 0);
    }
    public void removeQuestion(int index, Integer group) {
        //in case there are some tests in the ready question list, the index is not right, so we need to fix it one
        int indexCorrected = 0;
        if (index >= 0) {
            for (int i = 0; i < index && i < Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().size(); i++) {
                if (!Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().get(i).getGlobalID().contains("-")) {
                    indexCorrected++;
                }
            }
        }

        if (tableViewArrayList.get(group).getItems().size() > 0 && tableViewArrayList.get(group).getItems().get(0).getAnswers().size() > indexCorrected) {
            for (int i = 0; i < tableViewArrayList.get(group).getItems().size(); i++) {
                tableViewArrayList.get(group).getItems().get(i).getAnswers().remove(indexCorrected);
            }
            tableViewArrayList.get(group).getColumns().remove(indexCorrected + 3);
            //questions.remove(indexCorrected);
            Koeko.studentGroupsAndClass.get(group).getActiveQuestions().remove(indexCorrected);
            //questionsIDs.remove(indexCorrected);

            for (int i = 3 + indexCorrected; i < tableViewArrayList.get(group).getColumns().size(); i++) {
                TableColumn column = tableViewArrayList.get(group).getColumns().get(i);
                final int questionIndex = i - 3;
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SingleStudentAnswersLine, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<SingleStudentAnswersLine, String> p) {
                        // p.getValue() returns the Person instance for a particular TableView row
                        return p.getValue().getAnswers().get(questionIndex);
                    }
                });
                column.setCellFactory(new Callback<TableColumn<SingleStudentAnswersLine, String>, TableCell<SingleStudentAnswersLine, String>>() {
                    @Override
                    public TableCell<SingleStudentAnswersLine, String> call(TableColumn<SingleStudentAnswersLine, String> param) {
                        return new TableCell<SingleStudentAnswersLine, String>() {

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (!isEmpty()) {
                                    this.setStyle("-fx-text-fill: red;");
                                    // Get fancy and change color based on data
                                    if(item.contains("#/#")) {
                                        this.setStyle("-fx-text-fill: green;");
                                    }
                                    setText(item.replace("#/#",""));
                                }
                            }

                        };
                    }
                });
            }
        }

        //remove corresponding evaluations
        if (Koeko.studentGroupsAndClass.get(group).getAverageEvaluations().size() > indexCorrected) {
            Koeko.studentGroupsAndClass.get(group).getAverageEvaluations().remove(indexCorrected);
            Integer nbSutdents = Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().size();
            for (int i = 0; i < nbSutdents && Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().size() > indexCorrected; i++) {
                Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().get(i).remove(indexCorrected);
            }
        }
    }

    public void addUser(Student UserStudent, Boolean connection) {
        addUser(UserStudent,connection,0);
        if (connection) {
            Koeko.questionSendingControllerSingleton.addSudentToContextMenu(UserStudent);
        }
    }
    public void addUser(Student UserStudent, Boolean connection, Integer group) {
        //add user to the studentToReceptionStatus map
        studentIdToStatusReceptionMap.put(UserStudent.getUniqueDeviceID(), 0);

        System.out.println("adding user with connection:" + connection + "; ip: " + UserStudent.getInetAddress().toString());
        if (connection) {
            System.out.println("Student connection in addUser");
        } else {
            System.out.println("Student without connection in addUser");
        }

        //fill an array containing the names already present in the table
        ArrayList<String> studentNames = new ArrayList<>();
        for (SingleStudentAnswersLine singleStudentAnswersLine: tableViewArrayList.get(group).getItems()) studentNames.add(singleStudentAnswersLine.getStudentName());
        if (!studentNames.contains(UserStudent.getName())) {
            Koeko.studentGroupsAndClass.get(group).addStudentIfNotInClass(UserStudent);
            //for (int k = 0; k < 10; k++) {
            SingleStudentAnswersLine singleStudentAnswersLine;
            if (connection) {
                singleStudentAnswersLine = new SingleStudentAnswersLine(UserStudent, "connected / Sync : No", "0");
            } else {
                singleStudentAnswersLine = new SingleStudentAnswersLine(UserStudent, "disconnected / Sync : No", "0");
            }
            for (int i = 0; i < tableViewArrayList.get(group).getItems().get(0).getAnswers().size(); i++) {
                singleStudentAnswersLine.addAnswer();
            }
            if (tableViewArrayList.get(group).getItems().size() == 0) {
                tableViewArrayList.get(group).getItems().add(singleStudentAnswersLine);
            } else {
                tableViewArrayList.get(group).getItems().add(tableViewArrayList.get(group).getItems().size() - 1,singleStudentAnswersLine);
            }

            //add evaluation line
            if (Koeko.studentGroupsAndClass.get(group).getActiveEvaluations() != null) {
                Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().add(new ArrayList<>());
                for (int i = 0; i < Koeko.studentGroupsAndClass.get(group).getActiveQuestions().size(); i++) {
                    Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().get(Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().size() - 1).add(-1.0);
                }
            }

            //adapt table height
            tableViewArrayList.get(group).setPrefHeight(tableViewArrayList.get(group).getPrefHeight() + cellHeight * 1.1);

            //pass information to mcqstats if open
            if (mcqStatsController != null) {
                mcqStatsController.addUser();
            }
        } else {
            //BEGIN change connection status to connected
            if (connection) {
                int indexStudent = -1;
                for (int i = 0; i < tableViewArrayList.get(group).getItems().size() - 1; i++) {
                    if (tableViewArrayList.get(group).getItems().get(i).getStudentName().contentEquals(UserStudent.getName())) {
                        indexStudent = i;
                    }
                }
                if (indexStudent >= 0) {
                    SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(group).getItems().get(indexStudent);
                    if (singleStudentAnswersLine.getStatus().contains("Disconnected")) {
                        singleStudentAnswersLine.setStatus("connected / Sync : No&red");
                    } else {
                        singleStudentAnswersLine.setStatus("connected / Sync : No");
                    }
                    tableViewArrayList.get(group).getItems().set(indexStudent, singleStudentAnswersLine);
                }
            }
            //END change connection status to connected

            //we merge students if we have same ip address and one is not initialized (name="no name") and if we have 2 same names
            Koeko.studentGroupsAndClass.get(group).mergeStudentsOnNameOrIP(UserStudent);
        }
    }

    public void addAnswerForUser(String student, String answer, String question, double evaluation, String questionId, Integer group) {
        try {
            //set answer
            answer = answer.replace("|||", ";");
            if (answer.contentEquals("") || answer.contentEquals(" ")) {
                answer = "no answer";
            }
            if (evaluation == 100) {
                answer += "#/#";
            }
            Integer indexColumn = Koeko.studentGroupsAndClass.get(group).getActiveQuestionIDs().indexOf(questionId);

            Integer indexRow = -1;
            for (int i = 0; i < tableViewArrayList.get(group).getItems().size(); i++) {
                if (tableViewArrayList.get(group).getItems().get(i).getStudentName().contentEquals(student)) {
                    indexRow = i;
                }
            }

            //if the answer doesn't correspond to an active question, active it first if possible
            if (indexRow >= 0 && indexColumn == -1) {
                int nbColumnsBefore = tableViewArrayList.get(group).getColumns().size();
                //the question is not activated (for example when reading from QR code), so we activate it and insert the answer
                Koeko.studentGroupsAndClass.get(group).getActiveIDs().add(questionId);
                QuestionGeneric questionGeneric;
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionId);
                if (questionMultipleChoice.getQUESTION().length() > 0) {
                    NetworkCommunication.networkCommunicationSingleton.getClassroom().addQuestMultChoice(questionMultipleChoice);
                    addQuestion(questionMultipleChoice.getQUESTION(), questionMultipleChoice.getID(), group);
                    questionGeneric = QuestionGeneric.mcqToQuestionGeneric(questionMultipleChoice);
                } else {
                    QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionId);
                    NetworkCommunication.networkCommunicationSingleton.getClassroom().addQuestShortAnswer(questionShortAnswer);
                    addQuestion(questionShortAnswer.getQUESTION(), questionShortAnswer.getID(), group);
                    questionGeneric = QuestionGeneric.shrtaqToQuestionGeneric(questionShortAnswer);
                }

                Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().add(questionGeneric);
                indexColumn = Koeko.studentGroupsAndClass.get(group).getActiveQuestionIDs().indexOf(questionId);

                int nbColumnsAfter = nbColumnsBefore;
                for (int i = 0; i < 5 && nbColumnsAfter <= nbColumnsBefore; i++) {
                    nbColumnsAfter = tableViewArrayList.get(group).getColumns().size();
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!Koeko.studentGroupsAndClass.get(group).getActiveQuestions().contains(question)) {
                Platform.runLater(() -> popUpIfQuestionNotCorresponding());
            }

            if (indexColumn >= 0 && indexRow >= 0) {
                SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(group).getItems().get(indexRow);
                singleStudentAnswersLine.setAnswer(answer, indexColumn);

                //update evaluation
                int numberAnswers = 0;
                for (int i = 0; i < singleStudentAnswersLine.getAnswers().size(); i++) {
                    String answerInCell = singleStudentAnswersLine.getAnswers().get(i).getValue();
                    if (answerInCell.length() > 0) numberAnswers++;
                }
                Double meanEvaluation = Double.parseDouble(singleStudentAnswersLine.getEvaluation());
                meanEvaluation = ((meanEvaluation * (numberAnswers - 1)) + evaluation) / numberAnswers;
                if (Koeko.studentGroupsAndClass.get(group).getAverageEvaluations().size() > indexColumn &&
                        Koeko.studentGroupsAndClass.get(group).getAverageEvaluations().get(indexColumn) != null) {
                    Koeko.studentGroupsAndClass.get(group).getAverageEvaluations().set(indexColumn, meanEvaluation);
                } else {
                    System.out.println("Problem setting the average evaluation for column: " + indexColumn);
                }
                DecimalFormat df = new DecimalFormat("#.#");
                singleStudentAnswersLine.setEvaluation(String.valueOf(df.format(meanEvaluation)));

                //update mean values
                Double questionAverage = Koeko.studentGroupsAndClass.get(group).updateAverageEvaluationForQuestion(indexColumn, indexRow, evaluation);
                Double classAverage = Koeko.studentGroupsAndClass.get(group).updateAverageEvaluationForClass();
                SingleStudentAnswersLine averageEvaluationsLine = tableViewArrayList.get(group).getItems().get(tableViewArrayList.get(group).getItems().size() - 1);
                averageEvaluationsLine.setEvaluation(String.valueOf(df.format(classAverage)));
                if (questionAverage > 60.0) {
                    averageEvaluationsLine.setAnswer(String.valueOf(df.format(questionAverage)) + "#/#", indexColumn);
                } else {
                    averageEvaluationsLine.setAnswer(String.valueOf(df.format(questionAverage)), indexColumn);
                }

                //update MCQStats
                if (mcqStatsController != null) {
                    mcqStatsController.updateChart(answer, indexRow);
                }
            }
        } catch (NumberFormatException e) {
            if (!functionalTesting.testMode) {
                e.printStackTrace();
            }
        }
    }

    public void userDisconnected(Student student) {
        userDisconnected(student,0);
    }
    public void userDisconnected(Student student, Integer group) {
        int indexStudent = -1;
        for (int i = 0; i < tableViewArrayList.get(group).getItems().size() - 1; i++) {
            if (tableViewArrayList.get(group).getItems().get(i).getStudentName().contentEquals(student.getName())) indexStudent = i;
        }
        System.out.println("user disconnected: " + student.getName() + "; index in table: " + indexStudent);
        if (indexStudent >= 0) {
            SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(group).getItems().get(indexStudent);

            if (!singleStudentAnswersLine.getStatus().contains("Disconnected")) {
                //&red is used to color the cell in red
                singleStudentAnswersLine.setStatus("Disconnected / Sync : No&red");
                tableViewArrayList.get(group).getItems().set(indexStudent, singleStudentAnswersLine);

                //play sound
                String musicFile = "sounds/bell.mp3";

                Media sound = new Media(new File(musicFile).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.play();
            }
        }
    }

    private void popUpIfQuestionNotCorresponding() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Question stored in the database doesn't correspond to the question answered by a student"));
        Scene dialogScene = new Scene(dialogVbox, 600, 40);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    //BUTTONS


//    public void addNewStudentToClass() {
//        if (chooseClassComboBox.getSelectionModel().getSelectedIndex() >= 0) {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateStudent.fxml"));
//            Parent root1 = null;
//            try {
//                root1 = fxmlLoader.load();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            CreateStudentController controller = fxmlLoader.<CreateStudentController>getController();
//            controller.initClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
//            Stage stage = new Stage();
//            stage.initModality(Modality.WINDOW_MODAL);
//            stage.initStyle(StageStyle.DECORATED);
//            stage.setTitle("Edit Evaluation");
//            stage.setScene(new Scene(root1));
//            stage.show();
//        } else {
//            Koeko.leftBarController.promptGenericPopUp("No class is currently selected", "No Class");
//        }
//    }
//
//    public void createClass() {
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CreateClass.fxml"));
//        Parent root1 = null;
//        try {
//            root1 = fxmlLoader.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        CreateClassController controller = fxmlLoader.<CreateClassController>getController();
//        controller.initializeParameters(chooseClassComboBox);
//        Stage stage = new Stage();
//        stage.initModality(Modality.WINDOW_MODAL);
//        stage.initStyle(StageStyle.DECORATED);
//        stage.setTitle("Create a New Class");
//        stage.setScene(new Scene(root1));
//        stage.show();
//    }
//
//    public void saveStudentsToClass() {
//        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
//            for (int i = 0; i < tableViewArrayList.get(0).getItems().size() - 1; i++) {
//                String studentName = tableViewArrayList.get(0).getItems().get(i).getStudentName();
//                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
//                DbTableRelationClassStudent.addClassStudentRelation(className, studentName);
//            }
//        }
//    }
//
//    public void removeStudentFromClass() {
//        removeStudentFromClass(0);
//    }
//    public void removeStudentFromClass(Integer group) {
//        if (!tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudentName().contentEquals("CLASS")) {
//            //adapt table height
//            tableViewArrayList.get(group).setPrefHeight(tableViewArrayList.get(group).getPrefHeight() - cellHeight * 1.1);
//
//            String studentName = tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudentName();
//            if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
//                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
//                try {
//                    DbTableRelationClassStudent.removeStudentFromClass(studentName, className);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            int studentIndex = tableViewArrayList.get(0).getSelectionModel().getSelectedIndex();
//            tableViewArrayList.get(0).getItems().remove(tableViewArrayList.get(0).getSelectionModel().getSelectedItem());
//            Koeko.studentGroupsAndClass.get(group).getActiveEvaluations().remove(studentIndex);
//            if (Koeko.studentGroupsAndClass.get(group).getStudents().size() > studentIndex &&
//                    studentName.contentEquals(Koeko.studentGroupsAndClass.get(group).getStudents().get(studentIndex).getName())) {
//                Koeko.studentGroupsAndClass.get(group).getStudents().remove(studentIndex);
//            }
//        }
//    }
//
//    public void loadGroups() {
//        ArrayList<String> groups = DbTableClasses.getGroupsFromClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
//        groups.add(0,chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
//        Integer nbGroups = Koeko.studentGroupsAndClass.size();
//        //Integer nbGroupsToAdd = groups.size() - groupsAlreadyAdded;
//
//        int groupsLoaded = 0;
//        if (tableVBox.getChildren().size() > 1) {
//            int vboxSize = tableVBox.getChildren().size();
//            tableVBox.getChildren().remove(1, vboxSize);
//            groupsLoaded = 1;
//        }
//
//        for (int i = 0; i < nbGroups; i++) {
//            if (i > 0) {
//                addGroup(groups.get(i), i);
//            }
//
//            //add studentGroupsAndClass for group
//            ArrayList<Student> students = DbTableClasses.getStudentsInClass(groups.get(i));
//            for (Student student : students) {
//                addUser(student, false, i);
//            }
//        }
//    }
//
//    public void loadClass() {
//        String activeClass = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
//        Koeko.questionSendingControllerSingleton.activeClassChanged(activeClass);
//
//        chooseTestCombo.getItems().clear();
//        ArrayList<String> tests = DbTableRelationClassTest.getTestsForClass(activeClass);
//        chooseTestCombo.getItems().add("No test");
//        for (String test : tests) {
//            chooseTestCombo.getItems().add(test);
//        }
//
//        loadGroups();
//    }
//
    public void addGroup(String group, Integer groupIndex) {
        TableView<SingleStudentAnswersLine> studentsQuestionsTable = new TableView<>();
        //TableColumn groupName = new TableColumn<SingleStudentAnswersLine,String>(group);
        //studentsQuestionsTable.getColumns().add(groupName);
        TableColumn columnStudent = new TableColumn<SingleStudentAnswersLine,String>(bundle.getString("string.student"));
        columnStudent.setPrefWidth(180);
        columnStudent.setCellValueFactory(new PropertyValueFactory<>(bundle.getString("string.student")));
        studentsQuestionsTable.getColumns().add(columnStudent);

        TableColumn columnStatus = new TableColumn<SingleStudentAnswersLine,String>(bundle.getString("string.status"));
        columnStatus.setPrefWidth(100);
        columnStatus.setCellValueFactory(new PropertyValueFactory<>(bundle.getString("string.status")));
        studentsQuestionsTable.getColumns().add(columnStatus);

        TableColumn columnEvaluation = new TableColumn<SingleStudentAnswersLine,String>(bundle.getString("string.evaluation"));
        columnEvaluation.setPrefWidth(100);
        columnEvaluation.setCellValueFactory(new PropertyValueFactory<>(bundle.getString("string.evaluation")));
        studentsQuestionsTable.getColumns().add(columnEvaluation);

        //add summary linestudentsQuestionsTable.setFixedCellSize(cellHeight);
        Student classStudent = new Student();
        classStudent.setName("string.class");
        SingleStudentAnswersLine singleStudentAnswersLine = new SingleStudentAnswersLine(classStudent, "0", "0.0");
        studentsQuestionsTable.setPrefHeight(cellHeight * 4);
        studentsQuestionsTable.getItems().add(singleStudentAnswersLine);

        Label groupNameLabel = new Label(bundle.getString("string.group") + group);
        groupNameLabel.setStyle("-fx-padding: 20 0 0 0;");
        tableVBox.getChildren().add(groupNameLabel);
        tableVBox.getChildren().add(studentsQuestionsTable);
        tableViewArrayList.add(studentsQuestionsTable);

        Classroom newGroup = new Classroom();
        newGroup.setTableIndex(Koeko.studentGroupsAndClass.size() - 1);
        //Koeko.studentGroupsAndClass.add(newGroup);


        //add questions
        ArrayList<String> questionIDs = DbTableRelationClassQuestion.getQuestionsIDsForClass(group);
        for (String id : questionIDs) {
            Koeko.studentGroupsAndClass.get(groupIndex).getActiveIDs().add(id);
            if (!Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().contains(id)) {
                Koeko.studentGroupsAndClass.get(0).getIDsToStoreOnDevices().add(id);
            }
            Integer questionType = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(id));
            String question = "";
            if (questionType == 0) {
                try {
                    question = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(id).getQUESTION();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (questionType == 1) {
                question = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id).getQUESTION();
            }
            addQuestion(question, id, groupIndex);
        }
    }

    public void removeGroup(int groupIndex) {
        tableViewArrayList.remove(groupIndex);
        tableVBox.getChildren().remove(1 + (groupIndex) * 2);
        tableVBox.getChildren().remove(1 + (groupIndex) * 2);
    }
//
//    public void launchChooseTest() {
//        if (chooseClassComboBox.getSelectionModel().getSelectedItem() != null) {
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/ChooseTest.fxml"));
//            Parent root1 = null;
//            try {
//                root1 = fxmlLoader.load();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            ChooseTestController controller = fxmlLoader.<ChooseTestController>getController();
//            controller.initializeParameters(chooseTestCombo, chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
//            Stage stage = new Stage();
//            stage.initModality(Modality.WINDOW_MODAL);
//            stage.initStyle(StageStyle.DECORATED);
//            stage.setTitle("Assign a Certificative Test to the Class");
//            stage.setScene(new Scene(root1));
//            stage.show();
//        }
//    }
//
//    public void sendObjectiveEvaluationToStudents() {
//        String test = chooseTestCombo.getSelectionModel().getSelectedItem().toString();
//        if (test != null && !test.contentEquals("No test")) {
//            for (int i = 0; i < tableViewArrayList.get(0).getItems().size() - 1; i++) {
//                String studentName = tableViewArrayList.get(0).getItems().get(i).getStudentName();
//                for (int j = 3; j < tableViewArrayList.get(0).getColumns().size(); j++) {
//                    String objective = tableViewArrayList.get(0).getColumns().get(j).getText();
//                    String evaluation = tableViewArrayList.get(0).getItems().get(i).getAnswers().get(j - 3).getValue();
//                    NetworkCommunication.networkCommunicationSingleton.sendTestEvaluation(studentName, test, objective, evaluation);
//                }
//            }
//        }
//    }
//
//    public void certificativeTestSelected() {
//
//        //remove questions or objectives columns if some are present
//        String firstQuestion = "";
//        if (Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().size() > 0 &&
//                Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().get(0) != null) {
//            firstQuestion = Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().get(0).getQuestion();
//
//            //remove questions if some are present in the ready list
//            Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems().clear();
//        }
//        if (tableViewArrayList.get(0).getColumns().size() > 3 && !tableViewArrayList.get(0).getColumns().get(3).getText().contentEquals(firstQuestion)) {
//            tableViewArrayList.get(0).getColumns().remove(3, tableViewArrayList.get(0).getColumns().size());
//        } else {
//            while (tableViewArrayList.get(0).getColumns().size() > 3) {
//                removeQuestion(0);
//            }
//        }
//
//        //load questions if the selected certificative test is "No test"
//        if (chooseTestCombo.getSelectionModel().getSelectedItem().toString().contentEquals("No test")) {
//            for (QuestionGeneric questionGeneric : Koeko.questionSendingControllerSingleton.readyQuestionsList.getItems()) {
//                addQuestion(questionGeneric.getQuestion(), questionGeneric.getGlobalID(), 0);
//            }
//        }
//
//        //BEGIN display objectives as questions in table
//        ArrayList<String> objectives = DbTableRelationObjectiveTest.getObjectivesFromTestName(chooseTestCombo.getSelectionModel().getSelectedItem().toString());
//        for (int k = 0; k < objectives.size(); k++) {
//            TableColumn column = new TableColumn(objectives.get(k));
//            column.setPrefWidth(180);
//            column.setEditable(true);
//            column.setCellFactory(TextFieldTableCell.forTableColumn());
//            column.setOnEditCommit(
//                    new EventHandler<TableColumn.CellEditEvent<SingleStudentAnswersLine, String>>() {
//                        @Override
//                        public void handle(TableColumn.CellEditEvent<SingleStudentAnswersLine, String> t) {
//                            System.out.println(t.getRowValue().getStudentName());
//                            System.out.println(t.getTableColumn().getText());
//                            String idObjective = DbTableLearningObjectives.getObjectiveIdFromName(t.getTableColumn().getText());
//                            DbTableIndividualQuestionForStudentResult.addIndividualObjectiveForStudentResult(idObjective,
//                                    t.getRowValue().getStudentName(),t.getNewValue(),"CERTIFICATIVE", chooseTestCombo.getSelectionModel().getSelectedItem().toString());
//                        }
//                    }
//            );
//            tableViewArrayList.get(0).setEditable(true);
//            tableViewArrayList.get(0).getColumns().add(column);
//            for (int i = 0; i < tableViewArrayList.get(0).getItems().size(); i++) {
//                tableViewArrayList.get(0).getItems().get(i).addAnswer();
//            }
//
//            final int objectiveIndex = k;
//            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SingleStudentAnswersLine, String>, ObservableValue<String>>() {
//                public ObservableValue<String> call(TableColumn.CellDataFeatures<SingleStudentAnswersLine, String> p) {
//                    // p.getValue() returns the instance for a particular TableView row
//                    return p.getValue().getAnswers().get(objectiveIndex);
//                }
//            });
//        }
//        //END display objectives as questions in table
//
//        //BEGIN fill table with results for objectives
//        if (!chooseTestCombo.getSelectionModel().getSelectedItem().toString().contentEquals("No test")) {
//            ArrayList<Integer> objectivesIDs = DbTableRelationObjectiveTest.getObjectivesIDsFromTestName(chooseTestCombo.getSelectionModel().getSelectedItem().toString());
//
//            for (int j = 0; j < tableViewArrayList.get(0).getItems().size(); j++) {
//                SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(0).getItems().get(j);
//                for (int i = 0; i < objectivesIDs.size(); i++) {
//                    String eval = DbTableIndividualQuestionForStudentResult.getResultForStudentForObjectiveInTest(singleStudentAnswersLine.getStudentName(),
//                            String.valueOf(objectivesIDs.get(i)), chooseTestCombo.getSelectionModel().getSelectedItem().toString());
//                    singleStudentAnswersLine.setAnswer(eval, i);
//                }
//            }
//        } else {
//            for (SingleStudentAnswersLine singleStudentAnswersLine : tableViewArrayList.get(0).getItems()) {
//                for (int i = 0; i < singleStudentAnswersLine.getAnswers().size(); i++) {
//                    singleStudentAnswersLine.setAnswer("", i);
//                }
//            }
//        }
//        //END fill table with results for objectives
//    }
//
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Koeko.studentsVsQuestionsTableControllerSingleton = this;
        bundle = resources;
        //initialize table
        tableViewArrayList = new ArrayList<>();
        studentIdToStatusReceptionMap = Collections.synchronizedMap(new LinkedHashMap<>());
        TableView<SingleStudentAnswersLine> studentsQuestionsTable = new TableView<>();
        studentsQuestionsTable.sortPolicyProperty().set(t -> {
            Comparator<SingleStudentAnswersLine> comparator = (r1, r2)
                    -> r1.getStudentObject().getStudentID() == "-1" ? 1 //CLASS at the bottom
                    : r2.getStudentObject().getStudentID() == "-1" ? -1 //CLASS at the bottom
                    : t.getComparator() == null ? 0 //no column sorted: don't change order
                    : t.getComparator().compare(r1, r2); //columns are sorted: sort accordingly
            FXCollections.sort(studentsQuestionsTable.getItems(), comparator);
            return true;
        });
        TableColumn<SingleStudentAnswersLine, Student> columnStudent = new TableColumn<>(bundle.getString("string.student"));
        columnStudent.setPrefWidth(180);
        columnStudent.setCellValueFactory( p -> new SimpleObjectProperty(p.getValue().getStudentObject()));
        columnStudent.setCellFactory(param -> {
            StudentTableCell studentTableCell = new StudentTableCell();
            return studentTableCell;
        } );
        studentsQuestionsTable.getColumns().add(columnStudent);
        TableColumn columnStatus = new TableColumn<SingleStudentAnswersLine,String>(bundle.getString("string.status"));
        columnStatus.setPrefWidth(180);
        columnStatus.setCellValueFactory(new PropertyValueFactory<>(bundle.getString("string.status")));
        studentsQuestionsTable.getColumns().add(columnStatus);

        //set red color if student disconnected
        columnStatus.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<SingleStudentAnswersLine, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            if(item.contains("&red")) {
                                this.setStyle("-fx-text-fill: red;");
                                item = item.replace("&red", "");
                            } else {
                                this.setStyle("-fx-text-fill: white;");
                            }
                            setText(item);
                        }
                    }
                };
            }
        });

        TableColumn columnEvaluation = new TableColumn<SingleStudentAnswersLine,String>(bundle.getString("string.evaluation"));
        columnEvaluation.setPrefWidth(100);
        columnEvaluation.setCellValueFactory(new PropertyValueFactory<>(bundle.getString("string.evaluation")));
        studentsQuestionsTable.getColumns().add(columnEvaluation);
        studentsQuestionsTable.setFixedCellSize(cellHeight);
        studentsQuestionsTable.setPrefHeight(cellHeight * 3.5);
        tableVBox.getChildren().add(studentsQuestionsTable);
        tableViewArrayList.add(studentsQuestionsTable);

        //add summary line
        Student classStudent = new Student();
        classStudent.setName(bundle.getString("string.class"));
        SingleStudentAnswersLine singleStudentAnswersLine = new SingleStudentAnswersLine(classStudent, "0", "0.0");
        tableViewArrayList.get(0).getItems().add(singleStudentAnswersLine);

        //initialize other members
        //questions = new ArrayList<>();
        //questionsIDs = new ArrayList<>();
        Koeko.studentGroupsAndClass = new ArrayList<>();
        Classroom mainClassroom = new Classroom();
        Koeko.studentGroupsAndClass.add(mainClassroom);
        List<String> classes = DbTableClasses.getAllClasses();
        ObservableList<String> observableList = FXCollections.observableList(classes);
        //chooseClassComboBox.setItems(observableList);

        //setup contextmenu for right click on row
        MenuItem menuItem = new MenuItem("Mark as Connected");
        contextMenu.getItems().add(menuItem);
        menuItem.setOnAction(event -> {
            int indexStudent = tableViewArrayList.get(0).getSelectionModel().getSelectedIndex();
            SingleStudentAnswersLine singleStudentAnswersLine2 = tableViewArrayList.get(0).getItems().get(indexStudent);
            if (singleStudentAnswersLine2.getStatus().split("/").length > 1) {
                singleStudentAnswersLine2.setStatus("connected /" + singleStudentAnswersLine2.getStatus().split("/")[1].replace("&red", ""));
            }
            tableViewArrayList.get(0).getItems().set(indexStudent, singleStudentAnswersLine2);
        });

        tableViewArrayList.get(0).addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if(tableViewArrayList.get(0).getSelectionModel().getSelectedIndex() != tableViewArrayList.get(0).getItems().size() - 1
                        && t.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(tableViewArrayList.get(0), t.getScreenX(), t.getScreenY());
                }
            }
        });
    }

    private Integer indexOfStudent(Vector<Student> studentsVector, Student singleStudent) {
        for (int i = 0; i < studentsVector.size(); i++) {
            if (singleStudent.getName().contentEquals(studentsVector.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    public void setStatusQuestionsReceived(Student student, Integer statusQuestionsReceived) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                Integer status = studentIdToStatusReceptionMap.get(student.getUniqueDeviceID());
                if (status == 0 && statusQuestionsReceived == 1) {
                    int indexStudent = -1;
                    for (int i = 0; i < tableViewArrayList.get(0).getItems().size() - 1; i++) {
                        if (tableViewArrayList.get(0).getItems().get(i).getStudentName().contentEquals(student.getName())) {
                            indexStudent = i;
                        }
                    }
                    if ( indexStudent != -1) {
                        SingleStudentAnswersLine singleStudentAnswersLine2 = tableViewArrayList.get(0).getItems().get(indexStudent);
                        singleStudentAnswersLine2.setStatus(singleStudentAnswersLine2.getStatus().split("/")[0] + "/ Sync : OK");
                        tableViewArrayList.get(0).getItems().set(indexStudent, singleStudentAnswersLine2);
                    }
                } else if (status == 1 && statusQuestionsReceived == 0) {
                    int indexStudent = -1;
                    for (int i = 0; i < tableViewArrayList.get(0).getItems().size() - 1; i++) {
                        if (tableViewArrayList.get(0).getItems().get(i).getStudentName().contentEquals(student.getName())) {
                            indexStudent = i;
                        }
                    }
                    if ( indexStudent != -1) {
                        SingleStudentAnswersLine singleStudentAnswersLine2 = tableViewArrayList.get(0).getItems().get(indexStudent);
                        singleStudentAnswersLine2.setStatus(singleStudentAnswersLine2.getStatus().split("/")[0] + "/ Sync : No");
                        tableViewArrayList.get(0).getItems().set(indexStudent, singleStudentAnswersLine2);
                    }
                }
                studentIdToStatusReceptionMap.put(student.getUniqueDeviceID(), statusQuestionsReceived);
            }
        });
    }
}
