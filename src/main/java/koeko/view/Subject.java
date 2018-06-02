package koeko.view;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Subject implements Serializable {

    public Subject() {
        this._subjectName = "";
        this._subjectId = 0;
        this._subjectMUID = "";
        this._sbjUpdDts = Timestamp.valueOf(LocalDateTime.now());
    }

    public Subject(String _subjectName, int _subjectId, String _subjectMUID, Timestamp _sbjUpdDts) {
        this._subjectName = _subjectName;
        this._subjectId = _subjectId;
        this._subjectMUID = _subjectMUID;
        this._sbjUpdDts = _sbjUpdDts;
    }

    private String _subjectName;
    private int _subjectId;
    private String _subjectMUID;
    private Timestamp _sbjUpdDts;

    public Timestamp get_sbjUpdDts() {
        return _sbjUpdDts;
    }

    public void set_sbjUpdDts(Timestamp _sbjUpdDts) {
        this._sbjUpdDts = _sbjUpdDts;
    }

    public String get_subjectName() {
        return _subjectName;
    }

    public void set_subjectName(String _subjectName) {
        this._subjectName = _subjectName;
    }

    public int get_subjectId() {
        return _subjectId;
    }

    public void set_subjectId(int _subjectId) {
        this._subjectId = _subjectId;
    }

    public String get_subjectMUID() {
        return _subjectMUID;
    }

    public void set_subjectMUID(String _subjectMUID) {
        this._subjectMUID = _subjectMUID;
    }
}
