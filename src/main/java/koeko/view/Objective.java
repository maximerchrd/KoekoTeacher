package koeko.view;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Objective implements Serializable {

    public Objective() {
        this._objectiveName = "";
        this._objectiveId = 0;
        this._objectiveMUID = "";
        this._objectiveLanguage = "";
        this._objectiveLevel = -1;
        this._sbjUpdDts = Timestamp.valueOf(LocalDateTime.now());
    }

    public Objective(String _objectiveName, int _objectiveId, String _objectiveMUID, Timestamp _sbjUpdDts) {
        this._objectiveName = _objectiveName;
        this._objectiveId = _objectiveId;
        this._objectiveMUID = _objectiveMUID;
        this._objectiveLanguage = "";
        this._sbjUpdDts = _sbjUpdDts;
        this._objectiveLevel = -1;
    }

    public Objective(String _objectiveName, int _objectiveId, String _objectiveMUID, String _objectiveLanguage, Timestamp _sbjUpdDts) {
        this._objectiveName = _objectiveName;
        this._objectiveId = _objectiveId;
        this._objectiveMUID = _objectiveMUID;
        this._objectiveLanguage = _objectiveLanguage;
        this._sbjUpdDts = _sbjUpdDts;
        this._objectiveLevel = -1;
    }

    private String _objectiveName;
    private int _objectiveId;
    private String _objectiveMUID;
    private String _objectiveLanguage;
    private Integer _objectiveLevel;
    private Timestamp _sbjUpdDts;

    public Timestamp get_sbjUpdDts() {
        return _sbjUpdDts;
    }

    public void set_sbjUpdDts(Timestamp _sbjUpdDts) {
        this._sbjUpdDts = _sbjUpdDts;
    }

    public String get_objectiveName() {
        return _objectiveName;
    }

    public void set_objectiveName(String _objectiveName) {
        this._objectiveName = _objectiveName;
    }

    public int get_objectiveId() {
        return _objectiveId;
    }

    public void set_objectiveId(int _objectiveId) {
        this._objectiveId = _objectiveId;
    }

    public String get_objectiveLanguage() {
        return _objectiveLanguage;
    }

    public void set_objectiveLanguage(String _objectiveLanguage) {
        this._objectiveLanguage = _objectiveLanguage;
    }

    public Integer get_objectiveLevel() {
        return _objectiveLevel;
    }

    public void set_objectiveLevel(Integer _objectiveLevel) {
        this._objectiveLevel = _objectiveLevel;
    }

    public String get_objectiveMUID() {
        return _objectiveMUID;
    }

    public void set_objectiveMUID(String _objectiveMUID) {
        this._objectiveMUID = _objectiveMUID;
    }
}
