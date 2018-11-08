package koeko.questions_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maximerichard on 22.11.17.
 */
public class QuestionGeneric {
    private String typeOfQuestion;
    private String question;
    private String imagePath;
    private int intTypeOfQuestion;
    private int indexInList;
    private String globalID;
    private Boolean activated;
    public QuestionGeneric () {
        typeOfQuestion = "undefined";
        indexInList = -1;
        globalID = "-1";
        intTypeOfQuestion = -1;
        question = "question not initialized";
        imagePath = "";
        activated = false;
    }
    public QuestionGeneric(String typeoflist, int indexinlist) {
        typeOfQuestion = typeoflist;
        indexInList = indexinlist;
        activated = false;
    }
    public QuestionGeneric(int typeofQuest, String GlobalID) {
        intTypeOfQuestion = typeofQuest;
        globalID = GlobalID;
        activated = false;
    }
    public String getGlobalID() {
        globalID = globalID.replace("--", "");
        return globalID;
    }

    public String getTypeOfQuestion() {
        return typeOfQuestion;
    }
    public int getIndexInList() {
        return indexInList;
    }
    public int getIntTypeOfQuestion() {
        return intTypeOfQuestion;
    }
    public String getQuestion() {
        return question;
    }
    public String getImagePath() {
        return imagePath;
    }
    public Boolean getActivated() {
        return activated;
    }

    public void setGlobalID(String globalID) {
        this.globalID = globalID;
    }
    public void setTypeOfQuestion(String typeofquestion) {
        typeOfQuestion = typeofquestion;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public void setIndexInList(int indexinlist) {
        indexInList = indexinlist;
    }
    public void setIntTypeOfQuestion(int intTypeOfQuestion) {
        this.intTypeOfQuestion = intTypeOfQuestion;
    }
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    //other methods
    static public QuestionGeneric mcqToQuestionGeneric (QuestionMultipleChoice questionMultipleChoice) {
        QuestionGeneric questionGeneric = new QuestionGeneric();
        questionGeneric.setGlobalID(questionMultipleChoice.getID());
        questionGeneric.setQuestion(questionMultipleChoice.getQUESTION());
        questionGeneric.setImagePath(questionMultipleChoice.getIMAGE());
        questionGeneric.setIntTypeOfQuestion(0);

        return questionGeneric;
    }

    static public QuestionGeneric shrtaqToQuestionGeneric (QuestionShortAnswer questionShortAnswer) {
        QuestionGeneric questionGeneric = new QuestionGeneric();
        questionGeneric.setGlobalID(questionShortAnswer.getID());
        questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
        questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
        questionGeneric.setIntTypeOfQuestion(1);

        return questionGeneric;
    }

    static public QuestionGeneric searchForQuestionWithID (List<QuestionGeneric> questionList, String id) {
        for (QuestionGeneric questionGeneric : questionList) {
            if (id.contentEquals(String.valueOf(questionGeneric.getGlobalID()))) {
                return questionGeneric;
            }
        }
        return null;
    }

    @Override
    public String toString()  {
        return this.question;
    }

    static public String changeIdSign(String id) {
        if (id.contains("--")){
            id = id.replace("--","-");
        } else if (id.contains("-")) {
            id = id.replace("-", "");
        } else {
            id = "-" + id;
        }
        return id;
    }
}
