package koeko.controllers.ResultsTable;

import javafx.beans.property.SimpleStringProperty;

public class SingleResultsIndicatorsForTable {

    private final SimpleStringProperty name_indicators = new SimpleStringProperty("");
    private final SimpleStringProperty short_perf_indicators = new SimpleStringProperty("");
    private final SimpleStringProperty long_perf_indicators = new SimpleStringProperty("");
    private final SimpleStringProperty strong_obj_indicators = new SimpleStringProperty("");
    private final SimpleStringProperty weak_obj_indicators = new SimpleStringProperty("");

    public SingleResultsIndicatorsForTable() {
    }

    public String getName_indicators() {
        return name_indicators.get();
    }

    public SimpleStringProperty name_indicatorsProperty() {
        return name_indicators;
    }

    public void setName_indicators(String name_indicators) {
        this.name_indicators.set(name_indicators);
    }

    public String getShort_perf_indicators() {
        return short_perf_indicators.get();
    }

    public SimpleStringProperty short_perf_indicatorsProperty() {
        return short_perf_indicators;
    }

    public void setShort_perf_indicators(String short_perf_indicators) {
        this.short_perf_indicators.set(short_perf_indicators);
    }

    public String getLong_perf_indicators() {
        return long_perf_indicators.get();
    }

    public SimpleStringProperty long_perf_indicatorsProperty() {
        return long_perf_indicators;
    }

    public void setLong_perf_indicators(String long_perf_indicators) {
        this.long_perf_indicators.set(long_perf_indicators);
    }

    public String getStrong_obj_indicators() {
        return strong_obj_indicators.get();
    }

    public SimpleStringProperty strong_obj_indicatorsProperty() {
        return strong_obj_indicators;
    }

    public void setStrong_obj_indicators(String strong_obj_indicators) {
        this.strong_obj_indicators.set(strong_obj_indicators);
    }

    public String getWeak_obj_indicators() {
        return weak_obj_indicators.get();
    }

    public SimpleStringProperty weak_obj_indicatorsProperty() {
        return weak_obj_indicators;
    }

    public void setWeak_obj_indicators(String weak_obj_indicators) {
        this.weak_obj_indicators.set(weak_obj_indicators);
    }
}
