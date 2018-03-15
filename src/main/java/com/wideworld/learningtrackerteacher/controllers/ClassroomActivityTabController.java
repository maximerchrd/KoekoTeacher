package com.wideworld.learningtrackerteacher.controllers;

import com.wideworld.learningtrackerteacher.students_management.Student;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;
import javax.swing.text.TableView;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by maximerichard on 19.02.18.
 */
public class ClassroomActivityTabController implements Initializable {

    static int screenWidth = 0;
    static int screenHeight = 0;

    @FXML private AnchorPane studentsQuestionsTable;
    @FXML private StudentsVsQuestionsTableController studentsQuestionsTableController;
    @FXML private Tab classroom_activity_tab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }



    public void addQuestion(String question, Integer id) {
        studentsQuestionsTableController.addQuestion(question,id);
    }
    public void addUser(Student UserStudent, Boolean connection) {
        studentsQuestionsTableController.addUser(UserStudent,connection);
    }
    public void addAnswerForUser(Student student, String answer, String question, double evaluation, Integer questionId) {
        studentsQuestionsTableController.addAnswerForUser(student,answer,question,evaluation,questionId);
    }
    public void removeQuestion(int index) {
        studentsQuestionsTableController.removeQuestion(index);
    }
    public void userDisconnected(Student student) {
        studentsQuestionsTableController.userDisconnected(student);
    }
}
