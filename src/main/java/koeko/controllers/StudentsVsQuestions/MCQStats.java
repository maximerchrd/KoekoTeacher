package koeko.controllers.StudentsVsQuestions;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.stage.Window;
import koeko.controllers.controllers_tools.SingleStudentAnswersLine;
import koeko.database_management.DbTableIndividualQuestionForStudentResult;
import koeko.database_management.DbTableQuestionMultipleChoice;
import koeko.questions_management.QuestionMultipleChoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class MCQStats  extends Window {

    @FXML private BarChart<String, Number> bar_chart;
    @FXML private NumberAxis numberYAxis;
    @FXML private CategoryAxis categoryXAxis;

    private ArrayList<String> labels;
    private ArrayList<Integer> nbAnswers;
    private CopyOnWriteArrayList<String[]> answerLines;

    public void initParameters(ArrayList<TableView<SingleStudentAnswersLine>> tableViewArrayList, Integer groupIndex,
                               Integer columnIndex, String questionID) {
        labels = new ArrayList<>();
        nbAnswers = new ArrayList<>();
        answerLines = new CopyOnWriteArrayList<>();

        QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionID);

        if (questionMultipleChoice.getQUESTION().length() > 0) {
            categoryXAxis.setLabel("Answers");
            numberYAxis.setLabel("# of time the answer was chosen");

            for (String answer : questionMultipleChoice.getAnswers()) {
                labels.add(answer);
                nbAnswers.add(0);
            }

            for (SingleStudentAnswersLine line : tableViewArrayList.get(groupIndex).getItems()) {
                String[] answers = line.getAnswers().get(columnIndex - 3).getValue().split(";");
                answerLines.add(answers);
            }

            drawChart();
        }
    }

    public void addUser() {
        answerLines.add(new String[0]);
    }

    public void updateChart(String answers, int lineNumber) {
        for (int i = 0; i < nbAnswers.size(); i++) {
            nbAnswers.set(i, 0);
        }
        try {
            answerLines.set(lineNumber, answers.split(";"));
            Platform.runLater(() -> drawChart());
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void drawChart() {
        for (String[] answers : answerLines) {
            for (int i = 0; i < answers.length; i++) {
                int answerIndex = labels.indexOf(answers[i]);
                if (answerIndex >= 0) {
                    nbAnswers.set(answerIndex, nbAnswers.get(answerIndex) + 1);
                }
            }
        }

        ArrayList<String> answers = labels;

        XYChart.Series series1 = new XYChart.Series();
        for (int i = 0; i < answers.size(); i++) {
            series1.getData().add(new XYChart.Data(answers.get(i), nbAnswers.get(i)));
        }
        bar_chart.getData().remove(0, bar_chart.getData().size());
        bar_chart.getData().addAll(series1);
    }
}
