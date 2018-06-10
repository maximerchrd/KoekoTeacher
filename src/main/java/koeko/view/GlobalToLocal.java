package koeko.view;

import java.io.Serializable;

public class GlobalToLocal implements Serializable {
    private int _nbQuestionMultipleChoice;
    private int _nbSubject;
    private int _nbRelationQcmSbj;

    public GlobalToLocal() {}

    public int get_nbQuestionMultipleChoice() {
        return _nbQuestionMultipleChoice;
    }

    public void set_nbQuestionMultipleChoice(int _nbQuestionMultipleChoice) {
        this._nbQuestionMultipleChoice = _nbQuestionMultipleChoice;
    }

    public int get_nbSubject() {
        return _nbSubject;
    }

    public void set_nbSubject(int _nbSubject) {
        this._nbSubject = _nbSubject;
    }

    public int get_nbRelationQcmSbj() {
        return _nbRelationQcmSbj;
    }

    public void set_nbRelationQcmSbj(int _nbRelationQcmSbj) {
        this._nbRelationQcmSbj = _nbRelationQcmSbj;
    }
}
