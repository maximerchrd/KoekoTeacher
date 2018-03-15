package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.controllers.controllers_tools.SingleStudentAnswersLine;
import com.wideworld.learningtrackerteacher.students_management.Student;
import com.wideworld.learningtrackerteacher.database_management.DbTableClasses;
import com.wideworld.learningtrackerteacher.database_management.DbTableRelationClassStudent;
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
    private ArrayList<String> questions;
    private ArrayList<Integer> questionsIDs;
    private ArrayList<Student> students;
    @FXML private TableView<SingleStudentAnswersLine> studentsQuestionsTable;
    @FXML private TableColumn<SingleStudentAnswersLine, String> Student;
    @FXML private TableColumn<SingleStudentAnswersLine, String> Status;
    @FXML private TableColumn<SingleStudentAnswersLine, String> Evaluation;
    @FXML private ComboBox chooseClassComboBox;

    public void addQuestion(String question, Integer ID) {
        // Add extra columns if necessary:
        System.out.println("adding column");
        TableColumn column = new TableColumn(question);
        column.setPrefWidth(180);
        studentsQuestionsTable.getColumns().add(column);
        questions.add(question);
        questionsIDs.add(ID);
        for (int i = 0; i < studentsQuestionsTable.getItems().size(); i++) {
            studentsQuestionsTable.getItems().get(i).addAnswer();
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
        for (int i = 0; i < studentsQuestionsTable.getItems().size(); i++) {
            studentsQuestionsTable.getItems().get(i).getAnswers().remove(index);
        }
        studentsQuestionsTable.getColumns().remove(index + 3);
        questions.remove(index);
        questionsIDs.remove(index);

        for (int i = 3 + index; i < studentsQuestionsTable.getColumns().size(); i++) {
            TableColumn column = studentsQuestionsTable.getColumns().get(i);
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
        ArrayList<String> studentNames = new ArrayList<>();
        for (Student student: students) studentNames.add(student.getName());
        if (!studentNames.contains(UserStudent.getName())) {
            SingleStudentAnswersLine singleStudentAnswersLine;
            if (connection) {
                singleStudentAnswersLine = new SingleStudentAnswersLine(UserStudent.getName(), "connected", "0");
            } else {
                singleStudentAnswersLine = new SingleStudentAnswersLine(UserStudent.getName(), "disconnected", "0");
            }
            for (int i = 0; i < questions.size(); i++) {
                singleStudentAnswersLine.addAnswer();
            }
            studentsQuestionsTable.getItems().add(singleStudentAnswersLine);
            students.add(UserStudent);
        } else {
            int indexStudent = -1;
            for (int i = 0; i < studentsQuestionsTable.getItems().size(); i++) {
                if (studentsQuestionsTable.getItems().get(i).getStudent().contentEquals(UserStudent.getName())) indexStudent = i;
            }
            if (indexStudent >= 0) {
                SingleStudentAnswersLine singleStudentAnswersLine = studentsQuestionsTable.getItems().get(indexStudent);
                singleStudentAnswersLine.setStatus("connected");
                studentsQuestionsTable.getItems().set(indexStudent,singleStudentAnswersLine);
            }
        }
    }

    public void addAnswerForUser(Student student, String answer, String question, double evaluation, Integer questionId) {
        if (!questions.contains(question)) {
            popUpIfQuestionNotCorresponding();
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
        Integer indexRow = students.indexOf(student);
        SingleStudentAnswersLine singleStudentAnswersLine = studentsQuestionsTable.getItems().get(indexRow);
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
        studentsQuestionsTable.getItems().set(indexRow,singleStudentAnswersLine);
    }

    public void userDisconnected(Student student) {
        int indexStudent = -1;
        for (int i = 0; i < studentsQuestionsTable.getItems().size(); i++) {
            if (studentsQuestionsTable.getItems().get(i).getStudent().contentEquals(student.getName())) indexStudent = i;
        }
        System.out.println("user disconnected: " + student.getName() + "; index in table: " + indexStudent);
        if (indexStudent >= 0) {
            SingleStudentAnswersLine singleStudentAnswersLine = studentsQuestionsTable.getItems().get(indexStudent);
            singleStudentAnswersLine.setStatus("disconnected");
            studentsQuestionsTable.getItems().set(indexStudent,singleStudentAnswersLine);
        }
    }

    private void popUpIfQuestionNotCorresponding() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(this);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Question stored in the database doesn't correspond to the question answered by a student"));
        Scene dialogScene = new Scene(dialogVbox, 400, 40);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    //BUTTONS
    public void editEvaluation() {
        TablePosition tablePosition = studentsQuestionsTable.getFocusModel().getFocusedCell();
        Integer globalID = questionsIDs.get(tablePosition.getColumn() - 3);
        Integer studentID = students.get(tablePosition.getRow()).getStudentID();
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
            for (int i = 0; i < studentsQuestionsTable.getItems().size(); i++) {
                String studentName = studentsQuestionsTable.getItems().get(i).getStudent();
                String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
                DbTableRelationClassStudent.addClassStudentRelation(className, studentName);
            }
        }
    }

    public void removeStudentFromClass() {
        String studentName = studentsQuestionsTable.getSelectionModel().getSelectedItem().getStudent();
        String className = chooseClassComboBox.getSelectionModel().getSelectedItem().toString();
        try {
            DbTableRelationClassStudent.removeStudentFromClass(studentName, className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        studentsQuestionsTable.getItems().remove(studentsQuestionsTable.getSelectionModel().getSelectedItem());
    }

    public void loadClass() {
        Vector<Student> students = DbTableClasses.getStudentsInClass(chooseClassComboBox.getSelectionModel().getSelectedItem().toString());
        for (int i = 0; i < students.size(); i++) {
            addUser(students.get(i),false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Student.setCellValueFactory(new PropertyValueFactory<SingleStudentAnswersLine, String>("Student"));
        Status.setCellValueFactory(new PropertyValueFactory<SingleStudentAnswersLine, String>("Status"));
        Evaluation.setCellValueFactory(new PropertyValueFactory<SingleStudentAnswersLine, String>("Evaluation"));
        questions = new ArrayList<>();
        questionsIDs = new ArrayList<>();
        students = new ArrayList<>();
        List<String> classes = DbTableClasses.getAllClasses();
        ObservableList<String> observableList = FXCollections.observableList(classes);
        chooseClassComboBox.setItems(observableList);
    }
}
