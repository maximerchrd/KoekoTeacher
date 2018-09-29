package koeko.controllers;


import koeko.ResultsManagement.Result;
import koeko.database_management.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.view.Subject;


import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by maximerichard on 21.12.17.
 */
public class DisplayStatsController implements Initializable {
    private Vector<String> studentsVector;
    private ArrayList<QuestionMultipleChoice> questionMultipleChoices;
    private String chartType1 = "Evaluation vs subject";
    private String chartType2 = "Evaluation vs objective";
    private String chartType3 = "Histogram for Questions";
    private TreeItem<String> rootItem;

    @FXML private ComboBox chart_type;
    @FXML private ComboBox time_step;
    @FXML private TreeView students_tree;
    @FXML private BarChart <String, Number> bar_chart;
    @FXML private NumberAxis numberYAxis;
    @FXML private CategoryAxis categoryXAxis;
    @FXML private javafx.scene.control.ScrollPane chartScrollPane;
    @FXML private AnchorPane anchorPane;
    @FXML private ComboBox subject_filtering;

    DisplayStatsController displayStats_singleton;


    public void initialize(URL location, ResourceBundle resources) {
        //combobox with types of values to consider for the chart
        chart_type.getItems().addAll(chartType1, chartType2, chartType3);
        chart_type.getSelectionModel().select(chartType2);

        //setup and collapse subject filtering combobox
        ArrayList<String> subjects = new ArrayList<>();
        for (Subject subject : DbTableSubject.getSubjects()) {
            subjects.add(subject.get_subjectName());
        }
        subject_filtering.getItems().addAll(subjects);
        subject_filtering.managedProperty().bind(subject_filtering.visibleProperty());
        subject_filtering.setVisible(false);

        //get all the multiple choice questions for the nb of hits for answers histogram
        Vector<String> allIds = DbTableQuestionGeneric.getAllGenericQuestionsIds();
        questionMultipleChoices = new ArrayList<>();
        for (int j = 0; j < allIds.size(); j++) {
            QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(allIds.get(j));
            if( questionMultipleChoice.getQUESTION().length() > 0) {
                questionMultipleChoices.add(questionMultipleChoice);
            }
        }

        //tree with studentGroupsAndClass
        studentsVector = DbTableStudents.getStudentNames();
        rootItem = new TreeItem<String> ("Root");
        rootItem.setExpanded(true);
        for (int i = 0; i < studentsVector.size(); i++) {
            TreeItem<String> item = new TreeItem<String> (studentsVector.get(i));
            rootItem.getChildren().add(item);
        }
        students_tree.setRoot(rootItem);
        students_tree.setShowRoot(false);

        //combobox with time span
        time_step.getItems().addAll("All", "Week", "Month");
        time_step.getSelectionModel().select("All");

        int screenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
        int screenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        //chartScrollPane.setPrefWidth(screenWidth - time_step.getWidth() - 30);
        //chartScrollPane.setPrefHeight(screenHeight);
        anchorPane.setPrefWidth(screenWidth - time_step.getWidth() - 30);
        bar_chart.setPrefWidth(screenWidth - time_step.getWidth() - 30);
    }

    public void displayChartButtonClicked() {
        TreeItem selectedItem = (TreeItem)students_tree.getSelectionModel().getSelectedItem();
        drawChart(chart_type.getSelectionModel().getSelectedItem().toString(), selectedItem.getValue().toString());
    }

    public void eraseChartButtonClicked() {
        bar_chart.getData().remove(0, bar_chart.getData().size());
    }

    public void chartTypeChanged() {
        if (chart_type.getSelectionModel().getSelectedItem().toString().contentEquals(chartType3)) {
            rootItem.getChildren().removeAll(rootItem.getChildren());
            for (int i = 0; i < questionMultipleChoices.size(); i++) {
                TreeItem<String> item = new TreeItem<String>(questionMultipleChoices.get(i).getQUESTION());
                rootItem.getChildren().add(item);
            }

            //combobox with classes
            time_step.getItems().removeAll(time_step.getItems());
            ArrayList<String> classes = (ArrayList<String>) DbTableClasses.getAllClasses();
            classes.add(0, "All classes");
            time_step.getItems().addAll(classes);
            time_step.getSelectionModel().select(0);

            //setup subject filtering
            subject_filtering.setVisible(true);
        } else {
            rootItem.getChildren().removeAll(rootItem.getChildren());
            for (int i = 0; i < studentsVector.size(); i++) {
                TreeItem<String> item = new TreeItem<String>(studentsVector.get(i));
                rootItem.getChildren().add(item);
            }

            //combobox with time span
            time_step.getItems().removeAll(time_step.getItems());
            time_step.getItems().addAll("All", "Week", "Month");
            time_step.getSelectionModel().select("All");

            //toggle subject filtering combobox visibility
            subject_filtering.setVisible(false);
        }
    }

    public void subjectChanged() {
        ArrayList<String> questionIds = new ArrayList<>(DbTableRelationQuestionSubject
                .getQuestionsIdsForSubject(subject_filtering.getSelectionModel().getSelectedItem().toString()));
        rootItem.getChildren().removeAll(rootItem.getChildren());
        questionMultipleChoices.removeAll(questionMultipleChoices);
        for (String questionId : questionIds) {
            Integer questionType = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(questionId);
            if (questionType == 0) {
                questionMultipleChoices.add(DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionId));
                rootItem.getChildren().add(new TreeItem<>(questionMultipleChoices
                        .get(questionMultipleChoices.size() - 1).getQUESTION()));
            }
        }
    }

    private void drawChart(String valuesType, String student) {
        int timeStep = time_step.getSelectionModel().getSelectedIndex();
        XYChart.Series series1 = new XYChart.Series();
        if (valuesType.contentEquals(chartType1)) {
            categoryXAxis.setLabel("Subjects");
            numberYAxis.setLabel("Evaluation [%]");
            Result studentResultsPerSubject = DbTableStudents.getStudentResultsPerSubjectPerTimeStep(student.toString(), timeStep);
            Vector<String> subjects = studentResultsPerSubject.getXaxisValues();
            putResultsIntoSeries(student, timeStep, series1, studentResultsPerSubject, subjects);
        } else if (valuesType.contentEquals(chartType2)) {
            categoryXAxis.setLabel("Learning objectives");
            numberYAxis.setLabel("Evaluation [%]");
            Result studentResultsPerObjective = DbTableStudents.getStudentResultsPerObjectivePerTimeStep(student.toString(), timeStep);
            Vector<String> objectives = studentResultsPerObjective.getXaxisValues();
            putResultsIntoSeries(student, timeStep, series1, studentResultsPerObjective, objectives);
        } else if (valuesType.contentEquals(chartType3)) {
            categoryXAxis.setLabel("Answers");
            numberYAxis.setLabel("# of time the answer was chosen");
            int treeindex = students_tree.getSelectionModel().getSelectedIndex();
            ArrayList<ArrayList> answersAndHistogram = DbTableIndividualQuestionForStudentResult.getAnswersHistogramForQuestion(
                    questionMultipleChoices.get(students_tree.getSelectionModel().getSelectedIndex()).getID(),
                    time_step.getSelectionModel().getSelectedItem().toString());
            ArrayList<String> answers = answersAndHistogram.get(0);
            ArrayList<Integer> histogram = answersAndHistogram.get(1);
            setupQuestionHistogram(answers, histogram, series1,
                    questionMultipleChoices.get(students_tree.getSelectionModel().getSelectedIndex()).getQUESTION());
        }


        bar_chart.setPrefWidth(chartScrollPane.getWidth());
        bar_chart.setPrefHeight(chartScrollPane.getHeight());
        bar_chart.setAnimated(false);
    }

    private void setupQuestionHistogram(ArrayList<String> answers, ArrayList<Integer> nbAnswers, XYChart.Series series1, String question) {
        series1.setName(question);
        for (int i = 0; i < answers.size(); i++) {
            series1.getData().add(new XYChart.Data(answers.get(i), nbAnswers.get(i)));
        }
        bar_chart.getData().addAll(series1);
    }

    private void putResultsIntoSeries(String student, int timeStep, XYChart.Series series1, Result studentResultsPerXValue, Vector<String> xValue) {
        if (timeStep == 0) {
            series1.setName(student.toString());
            for (int i = 0; i < xValue.size(); i++) {
                series1.getData().add(new XYChart.Data(xValue.get(i), Double.parseDouble(studentResultsPerXValue.getResults().get(i).get(0))));
            }
            bar_chart.getData().addAll(series1);
        } else {
            Vector<XYChart.Series> series = new Vector<>();
            for (int i = 0; i < xValue.size(); i++) {
                for (int j = 0; j < studentResultsPerXValue.getResults().get(i).size(); j++) {
                    int k = 0;
                    for (; k < series.size(); k++) {
                        if (studentResultsPerXValue.getDates().get(i).get(j).contentEquals(series.get(k).getName())) {
                            break;
                        }
                    }
                    if (k == series.size()) {
                        XYChart.Series serie = new XYChart.Series();
                        serie.setName(studentResultsPerXValue.getDates().get(i).get(j));
                        if (studentResultsPerXValue.getResults().get(i).get(j).contentEquals("NaN")) {
                            studentResultsPerXValue.getResults().get(i).set(j, "0.0");
                        }
                        serie.getData().add(new XYChart.Data(xValue.get(i), Double.parseDouble(studentResultsPerXValue.getResults().get(i).get(j))));
                        series.add(serie);
                    } else {
                        if (studentResultsPerXValue.getResults().get(i).get(j).contentEquals("NaN")) {
                            studentResultsPerXValue.getResults().get(i).set(j, "0.0");
                        }
                        series.get(k).getData().add(new XYChart.Data(xValue.get(i), Double.parseDouble(studentResultsPerXValue.getResults().get(i).get(j))));
                    }
                }
            }
            for (int k = 0; k < series.size(); k++) {
                bar_chart.getData().addAll(series.get(k));
            }
        }
    }
}
