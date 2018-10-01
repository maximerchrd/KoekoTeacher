package koeko.controllers.ResultsTable;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import koeko.controllers.GenericPopUpController;
import koeko.database_management.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

/**
 * Created by maximerichard on 01.03.18.
 */
public class ResultsTableController implements Initializable {

    @FXML private ComboBox tableTypeCombobox;

    //raw results table
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

    //Indicators table
    @FXML private TableView<SingleResultsIndicatorsForTable> tableIndicators;
    @FXML private TableColumn<SingleResultsIndicatorsForTable, String> name_indicators;
    @FXML private TableColumn<SingleResultsIndicatorsForTable, String> short_perf_indicators;
    @FXML private TableColumn<SingleResultsIndicatorsForTable, String> long_perf_indicators;
    @FXML private TableColumn<SingleResultsIndicatorsForTable, String> strong_obj_indicators;
    @FXML private TableColumn<SingleResultsIndicatorsForTable, String> weak_obj_indicators;


    public void initialize(URL location, ResourceBundle resources) {
        tableTypeCombobox.getItems().addAll("Raw Results Table", "Indicators Table");
        tableTypeCombobox.getSelectionModel().select(0);

        resultsTable.managedProperty().bind(resultsTable.visibleProperty());
        Name.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("name"));
        Date.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("date"));
        Question.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("question"));
        Evaluation.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("evaluation"));
        StudentsAnswer.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("studentsAnswer"));
        CorrectAnswer.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("correctAnswer"));
        IncorrectAnswer.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("incorrectAnswer"));
        Subject.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("subjects"));
        Objectives.setCellValueFactory(new PropertyValueFactory<SingleResultForTable, String>("objectives"));

        name_indicators.setCellValueFactory(new PropertyValueFactory<SingleResultsIndicatorsForTable, String>("name_indicators"));
        short_perf_indicators.setCellValueFactory(new PropertyValueFactory<SingleResultsIndicatorsForTable, String>("short_perf_indicators"));
        long_perf_indicators.setCellValueFactory(new PropertyValueFactory<SingleResultsIndicatorsForTable, String>("long_perf_indicators"));
        strong_obj_indicators.setCellValueFactory(new PropertyValueFactory<SingleResultsIndicatorsForTable, String>("strong_obj_indicators"));
        weak_obj_indicators.setCellValueFactory(new PropertyValueFactory<SingleResultsIndicatorsForTable, String>("weak_obj_indicators"));

        tableIndicators.managedProperty().bind(tableIndicators.visibleProperty());
        tableIndicators.setVisible(false);
    }

    private void fillTableRawResults() {
        resultsTable.getItems().removeAll(resultsTable.getItems());
        ObservableList<SingleResultForTable> data = resultsTable.getItems();
        ArrayList<SingleResultForTable> resultsList = DbTableIndividualQuestionForStudentResult.getAllSingleResults();
        for (int i = 0; i < resultsList.size(); i++) {
            data.add(resultsList.get(i));
        }
    }

    private void fillTableIndicators(String groupName) {
        tableIndicators.getItems().removeAll(tableIndicators.getItems());
        ArrayList<ArrayList<String>> studentIds = new ArrayList<>();
        if (groupName.contentEquals("")) {
            studentIds = DbTableStudents.getStudentsIDsAndNames();
        }
        ArrayList<SingleResultsIndicatorsForTable> indicatorsForTables = new ArrayList<>();
        for (String student : studentIds.get(1)) {
            indicatorsForTables.add(new SingleResultsIndicatorsForTable());
            indicatorsForTables.get(indicatorsForTables.size() - 1).setName_indicators(student);
        }
        fillShortLongPerf(studentIds, indicatorsForTables);
        fillBestAndWorstObjectives(studentIds, indicatorsForTables);

        ObservableList<SingleResultsIndicatorsForTable> data = tableIndicators.getItems();
        for (int i = 0; i < indicatorsForTables.size(); i++) {
            data.add(indicatorsForTables.get(i));
        }
    }

    private void fillShortLongPerf(ArrayList<ArrayList<String>> studentIdsAndNames, ArrayList<SingleResultsIndicatorsForTable> tableRows) {
        for (int i = 0; i < studentIdsAndNames.get(0).size(); i++) {
            ArrayList<ArrayList<String>> datesNEvals = DbTableIndividualQuestionForStudentResult
                    .getEvalAndDateForStudentID(studentIdsAndNames.get(0).get(i));
            ArrayList<Double> shortPastAverageList = new ArrayList<>();
            ArrayList<Double> shortPresentAverageList = new ArrayList<>();
            ArrayList<Double> longPastAverageList = new ArrayList<>();
            ArrayList<Double> longPresentAverageList = new ArrayList<>();

            //set dates boundaries
            LocalDate shortTermDate = LocalDate.now().minusWeeks(1);
            LocalDate longTermDate = LocalDate.now().minusMonths(1);

            for (int j = 0; j < datesNEvals.get(0).size(); j++) {
                LocalDate dateOfEval = null;
                try {
                    dateOfEval = LocalDate.parse(datesNEvals.get(1).get(j));
                    if (Period.between(dateOfEval, shortTermDate).isNegative()) {
                        shortPresentAverageList.add(Double.parseDouble(datesNEvals.get(0).get(j)));
                    } else {
                        shortPastAverageList.add(Double.parseDouble(datesNEvals.get(0).get(j)));
                    }
                    if (Period.between(dateOfEval, longTermDate).isNegative()) {
                        longPresentAverageList.add(Double.parseDouble(datesNEvals.get(0).get(j)));
                    } else {
                        longPastAverageList.add(Double.parseDouble(datesNEvals.get(0).get(j)));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            if (shortPastAverageList.size() > 0 && shortPastAverageList.size() > 0) {
                Double shortPresentAverage = 0.0;
                for (Double eval : shortPresentAverageList) shortPresentAverage += eval;
                shortPresentAverage = shortPresentAverage / shortPresentAverageList.size();

                Double shortPastAverage = 0.0;
                for (Double eval : shortPastAverageList) shortPastAverage += eval;
                shortPastAverage = shortPastAverage / shortPastAverageList.size();

                tableRows.get(i).setShort_perf_indicators(String.valueOf(Math.round(shortPresentAverage) - Math.round(shortPastAverage)));
            } else {
                tableRows.get(i).setShort_perf_indicators("Not enough data to compute indicato");
            }

            if (longPresentAverageList.size() > 0 && longPastAverageList.size() > 0) {
                Double longPresentAverage = 0.0;
                for (Double eval : longPresentAverageList) longPresentAverage += eval;
                longPresentAverage = longPresentAverage / longPresentAverageList.size();

                Double longPastAverage = 0.0;
                for (Double eval : longPastAverageList) longPastAverage += eval;
                longPastAverage = longPastAverage / longPastAverageList.size();

                tableRows.get(i).setLong_perf_indicators(String.valueOf(Math.round(longPresentAverage) - Math.round(longPastAverage)));
            } else {
                tableRows.get(i).setLong_perf_indicators("Not enough data to compute indicato");
            }
        }
    }

    private void fillBestAndWorstObjectives(ArrayList<ArrayList<String>> studentIdsAndNames, ArrayList<SingleResultsIndicatorsForTable> tableRows) {
        ArrayList<String> objectives = new ArrayList<>(DbTableLearningObjectives.getAllObjectives());

        for (int k = 0; k < studentIdsAndNames.get(1).size(); k++) {
            Map<String,Double> resultsForObjectives = new LinkedHashMap<>();
            for (String objective : objectives) {
                String stringResult = DbTableIndividualQuestionForStudentResult.getResultForStudentForObjectiveInTest(studentIdsAndNames.get(1).get(k),
                        DbTableLearningObjectives.getObjectiveIdFromName(objective),"");
                Double result = -1.0;
                if (!stringResult.contentEquals("")) {
                    try {
                        result = Double.parseDouble(stringResult);
                    } catch (NumberFormatException e) {
                        result = -1.0;
                    }
                }
                resultsForObjectives.put(objective, result);
            }
            //sort the hashmap
            List<Map.Entry<String, Double>> entries =
                    new ArrayList<Map.Entry<String, Double>>(resultsForObjectives.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
                public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b){
                    return a.getValue().compareTo(b.getValue());
                }
            });
            Map<String, Double> sortedresultsForObjectives = new LinkedHashMap<String, Double>();
            for (Map.Entry<String, Double> entry : entries) {
                if (entry.getValue() != -1) {
                    sortedresultsForObjectives.put(entry.getKey(), entry.getValue());
                }
            }
            String strongObjectives = "";
            String weakObjectives = "";
            ArrayList<String> keys = new ArrayList<>(sortedresultsForObjectives.keySet());
            ArrayList<Double> values = new ArrayList<>(sortedresultsForObjectives.values());

            for (int i = 0; i < keys.size() && i < 3; i++) {
                if (values.get(i) <= 40) {
                    weakObjectives += keys.get(i) + "\n";
                }
            }
            for (int i = keys.size() - 1; i > 0 && i >= keys.size() -3; i--) {
                if (values.get(i) >= 80) {
                    strongObjectives += keys.get(i) + "\n";
                }
            }

            if (weakObjectives.contentEquals("")) {
                weakObjectives = "Not enough data to compute indicator";
            }
            if (strongObjectives.contentEquals("")) {
                strongObjectives = "Not enough data to compute indicator";
            }

            tableRows.get(k).setWeak_obj_indicators(weakObjectives);
            tableRows.get(k).setStrong_obj_indicators(strongObjectives);
        }
    }

    public void tableTypeChanged() {
        if (tableTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals("Raw Results Table")) {
            tableIndicators.setVisible(false);
            resultsTable.setVisible(true);
        } else {
            tableIndicators.setVisible(true);
            resultsTable.setVisible(false);
        }
    }

    public void getTable() {
        if (tableTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals("Raw Results Table")) {
            fillTableRawResults();
        } else if (tableTypeCombobox.getSelectionModel().getSelectedItem().toString().contentEquals("Indicators Table")) {
            fillTableIndicators("");
        }
    }

    public void exportResults() {
        String exportDoneMessage = "Export of Results Done!";

        PrintWriter writer = null;
        try {
            writer = new PrintWriter("results.csv", "UTF-8");
        } catch (FileNotFoundException e) {
            exportDoneMessage = "Sorry, we had a problem exporting results :-(";
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            exportDoneMessage = "Sorry, we had a problem exporting results :-(";
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

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/GenericPopUp.fxml"));
        Parent root1 = null;
        try {
            root1 = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenericPopUpController controller = fxmlLoader.getController();
        controller.initParameters(exportDoneMessage);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("Export");
        stage.setScene(new Scene(root1));
        stage.show();
    }
}