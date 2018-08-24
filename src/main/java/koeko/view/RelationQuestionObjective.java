package koeko.view;

import java.io.Serializable;

public class RelationQuestionObjective implements Serializable {
    private String _questionId;
    private String _objectiveMUID;
    private String _questionMUID;
    private int _level;

    public String get_questionId() {
        return _questionId;
    }

    public void set_questionId(String _questionId) {
        this._questionId = _questionId;
    }

    public String get_objectiveMUID() {
        return _objectiveMUID;
    }

    public void set_objectiveId(String _objectiveMUID) {
        this._objectiveMUID = _objectiveMUID;
    }

    public int get_level() {
        return _level;
    }

    public void set_level(int _level) {
        this._level = _level;
    }

    public String get_questionMUID() {
        return _questionMUID;
    }

    public void set_questionMUID(String _questionMUID) {
        this._questionMUID = _questionMUID;
    }

    public RelationQuestionObjective() {

    }

    public RelationQuestionObjective(String _questionId, String _objectiveMUID, int _level) {
        this._questionId = _questionId;
        this._objectiveMUID = _objectiveMUID;
        this._level = _level;
    }
}
