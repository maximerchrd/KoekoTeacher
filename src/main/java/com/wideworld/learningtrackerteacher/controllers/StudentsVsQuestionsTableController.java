package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.LearningTracker;
import com.wideworld.learningtrackerteacher.controllers.controllers_tools.SingleStudentAnswersLine;
import com.wideworld.learningtrackerteacher.students_management.Classroom;
import com.wideworld.learningtrackerteacher.students_management.Student;
import com.wideworld.learningtrackerteacher.database_management.DbTableClasses;
import com.wideworld.learningtrackerteacher.database_management.DbTableRelationClassStudent;
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

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by maximerichard on 12.03.18.
 */
public class StudentsVsQuestionsTableController extends Window implements Initializable {
    private final Double cellHeight = 25.0;
    private ArrayList<String> questions;
    private ArrayList<Integer> questionsIDs;
    static public ArrayList<Classroom> studentsConnected;
    private ArrayList<TableView<SingleStudentAnswersLine>> tableViewArrayList;
    @FXML private ComboBox chooseClassComboBox;
    @FXML private VBox tableVBox;


    public void addQuestion(String question, Integer ID) {
        addQuestion(question,ID,0);
    }
    public void addQuestion(String question, Integer ID, Integer group) {
        // Add extra columns if necessary:
        System.out.println("adding column");
        TableColumn column = new TableColumn(question);
        column.setPrefWidth(180);
        tableViewArrayList.get(group).getColumns().add(column);
        questions.add(question);
        questionsIDs.add(ID);
        for (int i = 0; i < tableViewArrayList.get(group).getItems().size(); i++) {
            tableViewArrayList.get(group).getItems().get(i).addAnswer();
        }
        final int questionIndex = questions.size() - 1;
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
                            this.setTextFill(Color.RED);
                            // Get fancy and change color based on data
                            if(item.contains("#/#")) {
                                this.setTextFill(Color.GREEN);
                            }
                            setText(item.replace("#/#",""));

                        }
                    }

                };
            }
        });
    }

    public void removeQuestion(int index) {
        removeQuestion(index, 0);
    }
    public void removeQuestion(int index, Integer group) {
        for (int i = 0; i < tableViewArrayList.get(group).getItems().size(); i++) {
            tableViewArrayList.get(group).getItems().get(i).getAnswers().remove(index);
        }
        tableViewArrayList.get(group).getColumns().remove(index + 3);
        questions.remove(index);
        questionsIDs.remove(index);

        for (int i = 3 + index; i < tableViewArrayList.get(group).getColumns().size(); i++) {
            TableColumn column = tableViewArrayList.get(group).getColumns().get(i);
            final int questionIndex = i - 3;
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SingleStudentAnswersLine, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<SingleStudentAnswersLine, String> p) {
                    // p.getValue() returns the Person instance for a particular TableView row
                    return p.getValue().getAnswers().get(questionIndex);
                }
            });
        }
    }

    public void addUser(Student UserStudent, Boolean connection) {
        addUser(UserStudent,connection,0);
    }
    public void addUser(Student UserStudent, Boolean connection, Integer group) {
        //adapt table height
        tableViewArrayList.get(group).setPrefHeight(tableViewArrayList.get(group).getPrefHeight() + cellHeight);

        ArrayList<String> studentNames = new ArrayList<>();
        for (Student student: studentsConnected.get(group).getStudents_array()) studentNames.add(student.getName());
        if (!studentNames.contains(UserStudent.getName())) {
            //for (int k = 0; k < 10; k++) {
                SingleStudentAnswersLine singleStudentAnswersLine;
                if (connection) {
                    singleStudentAnswersLine = new SingleStudentAnswersLine(UserStudent.getName(), "connected", "0");
                } else {
                    singleStudentAnswersLine = new SingleStudentAnswersLine(UserStudent.getName(), "disconnected", "0");
                }
                for (int i = 0; i < questions.size(); i++) {
                    singleStudentAnswersLine.addAnswer();
                }
                tableViewArrayList.get(group).getItems().add(singleStudentAnswersLine);
                studentsConnected.get(group).addStudent(UserStudent);
            //}
        } else {
            int indexStudent = -1;
            for (int i = 0; i < tableViewArrayList.get(group).getItems().size(); i++) {
                if (tableViewArrayList.get(group).getItems().get(i).getStudent().contentEquals(UserStudent.getName())) indexStudent = i;
            }
            if (indexStudent >= 0) {
                SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(group).getItems().get(indexStudent);
                singleStudentAnswersLine.setStatus("connected");
                tableViewArrayList.get(group).getItems().set(indexStudent,singleStudentAnswersLine);
            }
        }
    }

    public void addAnswerForUser(Student student, String answer, String question, double evaluation, Integer questionId) {
        addAnswerForUser(student,answer,question,evaluation,questionId,0);
    }
    public void addAnswerForUser(Student student, String answer, String question, double evaluation, Integer questionId, Integer group) {
        if (!questions.contains(question)) {
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    popUpIfQuestionNotCorresponding();
                }
            });
        }

        //set answer
        answer = answer.replace("|||",";");
        if (answer.contentEquals("") || answer.contentEquals(" ")) {
            answer = "no answer";
        }
        if (evaluation == 100) {
            answer += "#/#";
        }
        Integer indexColumn = questionsIDs.indexOf(questionId);
        Integer indexRow = indexOfStudent(studentsConnected.get(group).getStudents_array(), student);
        SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(group).getItems().get(indexRow);
        if (indexColumn >= 0 && indexRow >= 0) {
            singleStudentAnswersLine.setAnswer(answer,indexColumn);
        }

        //update evaluation
        int numberAnswers = 0;
        for (int i = 0; i < singleStudentAnswersLine.getAnswers().size(); i++) {
            String answerInCell = singleStudentAnswersLine.getAnswers().get(i).getValue();
            if (answerInCell.length() > 0) numberAnswers++;
        }
        Double meanEvaluation = Double.parseDouble(singleStudentAnswersLine.getEvaluation());
        meanEvaluation = ((meanEvaluation * (numberAnswers -1)) + evaluation) / numberAnswers;
        DecimalFormat df = new DecimalFormat("#.#");
        singleStudentAnswersLine.setEvaluation(String.valueOf(df.format(meanEvaluation)));
        tableViewArrayList.get(group).getItems().set(indexRow,singleStudentAnswersLine);
    }

    public void userDisconnected(Student student) {
        userDisconnected(student,0);
    }
    public void userDisconnected(Student student, Integer group) {
        int indexStudent = -1;
        for (int i = 0; i < tableViewArrayList.get(group).getItems().size(); i++) {
            if (tableViewArrayList.get(group).getItems().get(i).getStudent().contentEquals(student.getName())) indexStudent = i;
        }
        System.out.println("user disconnected: " + student.getName() + "; index in table: " + indexStudent);
        if (indexStudent >= 0) {
            SingleStudentAnswersLine singleStudentAnswersLine = tableViewArrayList.get(group).getItems().get(indexStudent);
            singleStudentAnswersLine.setStatus("disconnected");
            tableViewArrayList.get(group).getItems().set(indexStudent,singleStudentAnswersLine);
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
    public void editEvaluation() {
        editEvaluation(0);
    }
    public void editEvaluation(Integer group) {
        TablePosition tablePosition = tableViewArrayList.get(0).getFocusModel().getFocusedCell();
        Integer globalID = questionsIDs.get(tablePosition.getColumn() - 3);
        Integer studentID = studentsConnected.get(group).getStudents_array().get(tablePosition.getRow()).getStudentID();
        if (globalID >= 0) {
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
            for (int i = 0; i < tableViewArrayList.get(0).getItems().size(); i++) {
                String studentName = tableViewArrayList.get(0).getItems().get(i).getStudent();
                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
                DbTableRelationClassStudent.addClassStudentRelation(className, studentName);
            }
        }
    }

    public void removeStudentFromClass() {
        removeStudentFromClass(0);
    }
    public void removeStudentFromClass(Integer group) {
        //adapt table height
        tableViewArrayList.get(group).setPrefHeight(tableViewArrayList.get(group).getPrefHeight() - cellHeight);

        String studentName = tableViewArrayList.get(0).getSelectionModel().getSelectedItem().getStudent();
        String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
        try {
            DbTableRelationClassStudent.removeStudentFromClass(studentName, className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tableViewArrayList.get(0).getItems().remove(tableViewArrayList.get(0).getSelectionModel().getSelectedItem());
    }

    public void loadGroups() {
        ArrayList<String> groups = DbTableClasses.getGroupsFromClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
        Integer groupsAlreadyAdded = studentsConnected.size() - 1;
        Integer nbGroupsToAdd = groups.size() - groupsAlreadyAdded;

        for (int i = 0; i < nbGroupsToAdd; i++) {
            addGroup(groups.get(i + groupsAlreadyAdded));

            //add studentsConnected for group
            Vector<Student> students = DbTableClasses.getStudentsInClass(groups.get(i + groupsAlreadyAdded));
            for (Student student : students) {
                addUser(student, false, i + 1 + groupsAlreadyAdded);
                studentsConnected.get(i + 1 + groupsAlreadyAdded).addStudent(student);
            }
        }
    }

    public void loadClass() {
        Vector<Student> students = DbTableClasses.getStudentsInClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
        for (int i = 0; i < students.size(); i++) {
            addUser(students.get(i),false);
        }
        String activeClass = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
        LearningTracker.questionSendingControllerSingleton.activeClassChanged(activeClass);
    }

    private void addGroup(String group) {
        TableView<SingleStudentAnswersLine> studentsQuestionsTable = new TableView<>();
        TableColumn groupName = new TableColumn<SingleStudentAnswersLine,String>(group);
        studentsQuestionsTable.getColumns().add(groupName);
        TableColumn columnStudent = new TableColumn<SingleStudentAnswersLine,String>("Student");
        columnStudent.setPrefWidth(180);
        columnStudent.setCellValueFactory(new PropertyValueFactory<>("Student"));
        groupName.getColumns().add(columnStudent);

        TableColumn columnStatus = new TableColumn<SingleStudentAnswersLine,String>("Status");
        columnStatus.setPrefWidth(180);
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
        groupName.getColumns().add(columnStatus);

        TableColumn columnEvaluation = new TableColumn<SingleStudentAnswersLine,String>("Evaluation");
        columnEvaluation.setPrefWidth(180);
        columnEvaluation.setCellValueFactory(new PropertyValueFactory<>("Evaluation"));
        groupName.getColumns().add(columnEvaluation);

        studentsQuestionsTable.setFixedCellSize(cellHeight);
        studentsQuestionsTable.setPrefHeight(cellHeight * 4);

        tableVBox.getChildren().add(studentsQuestionsTable);
        tableViewArrayList.add(studentsQuestionsTable);

        Classroom newGroup = new Classroom();
        studentsConnected.add(newGroup);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialize table
        tableViewArrayList = new ArrayList<>();
        TableView<SingleStudentAnswersLine> studentsQuestionsTable = new TableView<>();
        TableColumn columnStudent = new TableColumn<SingleStudentAnswersLine,String>("Student");
        columnStudent.setPrefWidth(180);
        columnStudent.setCellValueFactory(new PropertyValueFactory<>("Student"));
        studentsQuestionsTable.getColumns().add(columnStudent);
        TableColumn columnStatus = new TableColumn<SingleStudentAnswersLine,String>("Status");
        columnStatus.setPrefWidth(180);
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
        studentsQuestionsTable.getColumns().add(columnStatus);
        TableColumn columnEvaluation = new TableColumn<SingleStudentAnswersLine,String>("Evaluation");
        columnEvaluation.setPrefWidth(180);
        columnEvaluation.setCellValueFactory(new PropertyValueFactory<>("Evaluation"));
        studentsQuestionsTable.getColumns().add(columnEvaluation);
        studentsQuestionsTable.setFixedCellSize(cellHeight);
        studentsQuestionsTable.setPrefHeight(cellHeight * 2.5);
        tableVBox.getChildren().add(studentsQuestionsTable);
        tableViewArrayList.add(studentsQuestionsTable);

        //initialize other members
        questions = new ArrayList<>();
        questionsIDs = new ArrayList<>();
        studentsConnected = new ArrayList<>();
        Classroom mainClassroom = new Classroom();
        studentsConnected.add(mainClassroom);
        List<String> classes = DbTableClasses.getAllClasses();
        ObservableList<String> observableList = FXCollections.observableList(classes);
        chooseClassComboBox.setItems(observableList);
    }

    private Integer indexOfStudent(ArrayList<Student> studentsArray, Student singleStudent) {
        for (int i = 0; i < studentsArray.size(); i++) {
            if (singleStudent.getName().contentEquals(studentsArray.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
}
