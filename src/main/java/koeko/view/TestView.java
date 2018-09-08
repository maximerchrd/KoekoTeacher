package koeko.view;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by maximerichard on 07.09.18.
 */
public class TestView implements Serializable {
    private String testName;
    private String idTest;
    private Timestamp QCM_UPD_TMS;
    private String language;

    public TestView() {
        this.testName = "";
        this.idTest = "-1";
    }

    //getters
    public String getTestName() {
        return testName;
    }

    public String getIdTest() {
        return idTest;
    }

    public Timestamp getQCM_UPD_TMS() {
        return QCM_UPD_TMS;
    }

    public String getLanguage() {
        return language;
    }

    //setter
    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setIdTest(String idTest) {
        this.idTest = idTest;
    }

    public void setQCM_UPD_TMS(Timestamp QCM_UPD_TMS) {
        this.QCM_UPD_TMS = QCM_UPD_TMS;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
