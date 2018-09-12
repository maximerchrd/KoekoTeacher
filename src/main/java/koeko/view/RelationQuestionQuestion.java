package koeko.view;

import java.io.Serializable;

public class RelationQuestionQuestion implements Serializable {
    private String idGlobal1;
    private String idGlobal2;
    private String testId;
    private String condition;

    public String getIdGlobal1() {
        return idGlobal1;
    }

    public void setIdGlobal1(String idGlobal1) {
        this.idGlobal1 = idGlobal1;
    }

    public String getIdGlobal2() {
        return idGlobal2;
    }

    public void setIdGlobal2(String idGlobal2) {
        this.idGlobal2 = idGlobal2;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
