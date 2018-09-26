package koeko.view;

import java.io.Serializable;

public class GlobalToLocal implements Serializable {
    private int _nbQuestionMultipleChoice;
    private int _nbSubject;
    private int _nbRelationQcmSbj;
    private int _nbObjectives;
    private int _nbRelationsQcmObj;
    private int _nbRelationsQtoQ;

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

    public int get_nbObjectives() {
        return _nbObjectives;
    }

    public void set_nbObjectives(int _nbObjectives) {
        this._nbObjectives = _nbObjectives;
    }

    public int get_nbRelationsQcmObj() {
        return _nbRelationsQcmObj;
    }

    public void set_nbRelationsQcmObj(int _nbRelationsQcmObj) {
        this._nbRelationsQcmObj = _nbRelationsQcmObj;
    }

    public int get_nbRelationsQtoQ() {
        return _nbRelationsQtoQ;
    }

    public void set_nbRelationsQtoQ(int _nbRelationsQtoQ) {
        this._nbRelationsQtoQ = _nbRelationsQtoQ;
    }

}
