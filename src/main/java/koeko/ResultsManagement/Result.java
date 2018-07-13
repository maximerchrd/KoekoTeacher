package koeko.ResultsManagement;

import java.util.Vector;

public class Result {
    Vector<String> XaxisValues = new Vector<>();
    Vector<Vector<String>> results = new Vector<>();
    Vector<Vector<String>> dates = new Vector<>();

    public Vector<String> getXaxisValues() {
        return XaxisValues;
    }

    public void setXaxisValues(Vector<String> xaxisValues) {
        this.XaxisValues = xaxisValues;
    }

    public Vector<Vector<String>> getResults() {
        return results;
    }

    public void setResults(Vector<Vector<String>> results) {
        this.results = results;
    }

    public Vector<Vector<String>> getDates() {
        return dates;
    }

    public void setDates(Vector<Vector<String>> dates) {
        this.dates = dates;
    }
}
