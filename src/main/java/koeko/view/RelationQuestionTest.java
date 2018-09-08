package koeko.view;

import java.io.Serializable;

public class RelationQuestionTest implements Serializable {
    private String _testMUID;
    private String _questionMUID;

    public String get_testMUID() {
        return _testMUID;
    }

    public void set_testMUID(String _testMUID) {
        this._testMUID = _testMUID;
    }

    public String get_questionMUID() {
        return _questionMUID;
    }

    public void set_questionMUID(String _questionMUID) {
        this._questionMUID = _questionMUID;
    }
}
