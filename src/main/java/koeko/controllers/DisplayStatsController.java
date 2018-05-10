package koeko.controllers;


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

        //combobox with time span
        time_step.getItems().addAll("All");
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

    private void drawChart(String valuesType, String student) {

        XYChart.Series series1 = new XYChart.Series();
        if (valuesType.contentEquals("Evaluation vs subject")) {
            categoryXAxis.setLabel("Subjects");
            numberYAxis.setLabel("Evaluation [%]");
            Vector<Vector<String>> studentResultsPerSubject = DbTableStudents.getStudentResultsPerSubject(student.toString());
            Vector<String> subjects = studentResultsPerSubject.get(0);
            Vector<String> results = studentResultsPerSubject.get(1);
            series1.setName(student.toString());
            for (int i = 0; i < subjects.size(); i++) {
                series1.getData().add(new XYChart.Data(subjects.get(i), Double.parseDouble(results.get(i))));
            }
        } else if (valuesType.contentEquals("Evaluation vs objective")) {
            categoryXAxis.setLabel("Learning objectives");
            numberYAxis.setLabel("Evaluation [%]");
            Vector<Vector<String>> studentResultsPerObjective = DbTableStudents.getStudentResultsPerObjective(student.toString());
            Vector<String> objectives = studentResultsPerObjective.get(0);
            Vector<String> results = studentResultsPerObjective.get(1);
            series1.setName(student.toString());
            for (int i = 0; i < objectives.size(); i++) {
                if (objectives.get(i).length() > 16) {
                    objectives.set(i, objectives.get(i).substring(0,15) + "\n" + objectives.get(i).substring(15,objectives.get(i).length()));
                    if (objectives.get(i).length() > 31) {
                        objectives.set(i, objectives.get(i).substring(0, 30) + "\n" + objectives.get(i).substring(30, objectives.get(i).length()));
                        if (objectives.get(i).length() > 46) {
                            objectives.set(i, objectives.get(i).substring(0, 45) + "...");
                        }
                    }
                }
                series1.getData().add(new XYChart.Data(objectives.get(i), Double.parseDouble(results.get(i))));
            }
        }

        bar_chart.getData().addAll(series1);
        bar_chart.setPrefWidth(chartScrollPane.getWidth());
        bar_chart.setPrefHeight(chartScrollPane.getHeight());
        bar_chart.setAnimated(false);
    }
}
