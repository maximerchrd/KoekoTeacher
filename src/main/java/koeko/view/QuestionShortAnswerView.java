package koeko.view;

import java.io.Serializable;
import java.sql.Timestamp;

public class QuestionShortAnswerView implements Serializable {
    private int ID;
    private String SUBJECT;
    private String LEVEL;
    private String QUESTION;
    private String IMAGE;
    private String SHRTAQ_MUID;
    private Timestamp SHRTAQ_UPD_TMS;


    public QuestionShortAnswerView()	{
        ID=0;
        SUBJECT="";
        LEVEL="";
        QUESTION="";
        IMAGE="none";
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSUBJECT() {
        return SUBJECT;
    }

    public void setSUBJECT(String SUBJECT) {
        this.SUBJECT = SUBJECT;
    }

    public String getLEVEL() {
        return LEVEL;
    }

    public void setLEVEL(String LEVEL) {
        this.LEVEL = LEVEL;
    }

    public String getQUESTION() {
        return QUESTION;
    }

    public void setQUESTION(String QUESTION) {
        this.QUESTION = QUESTION;
    }

    public String getIMAGE() {
        return IMAGE;
    }

    public void setIMAGE(String IMAGE) {
        this.IMAGE = IMAGE;
    }

    public String getSHRTAQ_MUID() {
        return SHRTAQ_MUID;
    }

    public void setSHRTAQ_MUID(String SHRTAQ_MUID) {
        this.SHRTAQ_MUID = SHRTAQ_MUID;
    }

    public Timestamp getSHRTAQ_UPD_TMS() {
        return SHRTAQ_UPD_TMS;
    }

    public void setSHRTAQ_UPD_TMS(Timestamp SHRTAQ_UPD_TMS) {
        this.SHRTAQ_UPD_TMS = SHRTAQ_UPD_TMS;
    }
}