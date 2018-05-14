package koeko.view;

public class RelationQuestionSubject {
    private int _questionId;
    private String _subjectMUID;
    private int _level;

    public int get_questionId() {
        return _questionId;
    }

    public void set_questionId(int _questionId) {
        this._questionId = _questionId;
    }

    public String get_subjectMUID() {
        return _subjectMUID;
    }

    public void set_subjectId(String _subjectMUID) {
        this._subjectMUID = _subjectMUID;
    }

    public int get_level() {
        return _level;
    }

    public void set_level(int _level) {
        this._level = _level;
    }

    public RelationQuestionSubject(int _questionId, String _subjectMUID, int _level) {
        this._questionId = _questionId;
        this._subjectMUID = _subjectMUID;
        this._level = _level;
    }
}
