package koeko.controllers;


import koeko.ResultsManagement.Result;
import koeko.database_management.DbTableStudents;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;


import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by maximerichard on 21.12.17.
 */
public class DisplayStatsController implements Initializable {
    private Vector<String> studentsVector;

    @FXML private ComboBox chart_type;
    @FXML private ComboBox time_step;
    @FXML private TreeView students_tree;
    @FXML private BarChart <String, Number> bar_chart;
    @FXML private NumberAxis numberYAxis;
    @FXML private CategoryAxis categoryXAxis;
    @FXML private javafx.scene.control.ScrollPane chartScrollPane;
    @FXML private AnchorPane anchorPane;

    DisplayStatsController displayStats_singleton;


    public void initialize(URL location, ResourceBundle resources) {
        //combobox with types of values to consider for the chart
        chart_type.getItems().addAll("Evaluation vs objective", "Evaluation vs subject");
        chart_type.getSelectionModel().select("Evaluation vs objective");

        //tree with studentGroupsAndClass
        studentsVector = DbTableStudents.getStudentNames();
        TreeItem<String> rootItem = new TreeItem<String> ("Inbox");
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
        int timeStep = time_step.getSelectionModel().getSelectedIndex();
        drawChart(chart_type.getSelectionModel().getSelectedItem().toString(), selectedItem.getValue().toString(),
                timeStep);
    }

    public void eraseChartButtonClicked() {
        bar_chart.getData().remove(0, bar_chart.getData().size());
    }

    private void drawChart(String valuesType, String student, int timeStep) {

        XYChart.Series series1 = new XYChart.Series();
        if (valuesType.contentEquals("Evaluation vs subject")) {
            categoryXAxis.setLabel("Subjects");
            numberYAxis.setLabel("Evaluation [%]");
            Result studentResultsPerSubject = DbTableStudents.getStudentResultsPerSubjectPerTimeStep(student.toString(), timeStep);
            Vector<String> subjects = studentResultsPerSubject.getXaxisValues();
            putResultsIntoSeries(student, timeStep, series1, studentResultsPerSubject, subjects);
        } else if (valuesType.contentEquals("Evaluation vs objective")) {
            categoryXAxis.setLabel("Learning objectives");
            numberYAxis.setLabel("Evaluation [%]");
            Result studentResultsPerObjective = DbTableStudents.getStudentResultsPerObjectivePerTimeStep(student.toString(), timeStep);
            Vector<String> objectives = studentResultsPerObjective.getXaxisValues();
            putResultsIntoSeries(student, timeStep, series1, studentResultsPerObjective, objectives);
        }


        bar_chart.setPrefWidth(chartScrollPane.getWidth());
        bar_chart.setPrefHeight(chartScrollPane.getHeight());
        bar_chart.setAnimated(false);
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
