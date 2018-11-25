package koeko.questions_management;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maximerichard on 22.11.17.
 */
public class QuestionGeneric {
    static public int MCQ = 0;
    static public int SHRTAQ = 1;
    static public int FORMATIVE_TEST = 2;
    static public int CERTIFICATIVE_TEST = 3;
    static public int GAME = 4;
    static public int GAME_QUESTIONSET = 5;
    private String question;
    private String imagePath;
    private int intTypeOfQuestion; //0: MCQ; 1: SHRTAQ; 2: formative Test; 3: certificative Test; 4: game; 5: question set for game
    private int indexInList;
    private String globalID;
    private Boolean activated;
    public QuestionGeneric () {
        indexInList = -1;
        globalID = "-1";
        intTypeOfQuestion = -1;
        question = "question not initialized";
        imagePath = "";
        activated = false;
    }
    public QuestionGeneric(String globalId, int indexinlist) {
        globalID = globalId;
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
        questionGeneric.setIntTypeOfQuestion(QuestionGeneric.MCQ);

        return questionGeneric;
    }

    static public QuestionGeneric shrtaqToQuestionGeneric (QuestionShortAnswer questionShortAnswer) {
        QuestionGeneric questionGeneric = new QuestionGeneric();
        questionGeneric.setGlobalID(questionShortAnswer.getID());
        questionGeneric.setQuestion(questionShortAnswer.getQUESTION());
        questionGeneric.setImagePath(questionShortAnswer.getIMAGE());
        questionGeneric.setIntTypeOfQuestion(QuestionGeneric.SHRTAQ);

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
