package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.controllers.controllers_tools.SingleResultForTable;
import com.wideworld.learningtrackerteacher.database_management.DbTableIndividualQuestionForStudentResult;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 01.03.18.
 */
public class ResultsTableController implements Initializable {

    @FXML private TableView<SingleResultForTable> resultsTable;
    @FXML private TableColumn<SingleResultForTable, String> Name;
    @FXML private TableColumn<SingleResultForTable, String> Date;
    @FXML private TableColumn<SingleResultForTable, String> Question;
    @FXML private TableColumn<SingleResultForTable, String> Evaluation;
    @FXML private TableColumn<SingleResultForTable, String> StudentsAnswer;
    @FXML private TableColumn<SingleResultForTable, String> CorrectAnswer;
    @FXML private TableColumn<SingleResultForTable, String> IncorrectAnswer;
    @FXML private TableColumn<SingleResultForTable, String> Subject;
    @FXML private TableColumn<SingleResultForTable, String> Objectives;


    public void initialize(URL location, ResourceBundle resources) {
        Name.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("name"));
        Date.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("date"));
        Question.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("question"));
        Evaluation.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("evaluation"));
        StudentsAnswer.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("studentsAnswer"));
        CorrectAnswer.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("correctAnswer"));
        IncorrectAnswer.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("incorrectAnswer"));
        Subject.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("subjects"));
        Objectives.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("objectives"));

        addResults();
    }

    private void addResults() {
        ObservableList<SingleResultForTable> data = resultsTable.getItems();
        ArrayList<SingleResultForTable> resultsList = DbTableIndividualQuestionForStudentResult.getAllSingleResults();
        for (int i = 0; i < resultsList.size(); i++) {
            data.add(resultsList.get(i));
        }
    }

    public void exportResults() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("results.csv", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.println("Name;Date;Question;Evaluation;Student's answer;Correct answer;Incorrect answer;Subjects;Objectives");
        ArrayList<SingleResultForTable> resultsList = DbTableIndividualQuestionForStudentResult.getAllSingleResults();
        for (int i = 0; i < resultsList.size(); i++) {
            writer.print(resultsList.get(i).getName() + ";");
            writer.print(resultsList.get(i).getDate() + ";");
            writer.print(resultsList.get(i).getQuestion().replace(";",",") + ";");
            writer.print(resultsList.get(i).getEvaluation() + ";");
            writer.print(resultsList.get(i).getStudentsAnswer().replace(";",",") + ";");
            writer.print(resultsList.get(i).getCorrectAnswer().replace(";",",") + ";");
            writer.print(resultsList.get(i).getIncorrectAnswer().replace(";",",") + ";");
            writer.print(resultsList.get(i).getSubjects().replace(";",",") + ";");
            writer.print(resultsList.get(i).getObjectives().replace(";",",") + "\n");
        }
        writer.close();
    }
}