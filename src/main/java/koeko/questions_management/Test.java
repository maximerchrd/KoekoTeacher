package koeko.questions_management;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Created by maximerichard on 15.01.18.
 */
public class Test {
    private String testName;
    private String idTest;
    private String mediaFileName;
    private Integer sendMediaFile;
    private ArrayList<String> idsQuestions;
    private ArrayList<QuestionGeneric> genericQuestions;
    private ArrayList<Double> questionsEvaluations;
    private ArrayList<String> objectives;
    private ArrayList<Integer> objectivesIDs;
    private Double testEvaluation;
    private Boolean isSynchroneousQuestionsTest;
    private Integer testMode;   //0: certificative test; 1: formative test
    private String medalsInstructions;
    private String updateTime;

    public Test() {
        this.testName = "";
        this.idTest = "-1";
        this.idsQuestions = new ArrayList<String>();
        this.genericQuestions = new ArrayList<QuestionGeneric>();
        this.questionsEvaluations = new ArrayList<>();
        this.isSynchroneousQuestionsTest = false;
        this.testMode = 1;
        this.medalsInstructions = "";
    }

    public void addGenericQuestion(QuestionGeneric questionGeneric) {
        genericQuestions.add(questionGeneric);
        idsQuestions.add(questionGeneric.getGlobalID());
    }

    //getters
    public String getTestName() {
        return testName;
    }

    public String getIdTest() {
        return idTest;
    }

    public ArrayList<String> getIdsQuestions() {
        return idsQuestions;
    }

    public ArrayList<QuestionGeneric> getGenericQuestions() {
        return genericQuestions;
    }

    public ArrayList<Double> getQuestionsEvaluations() {
        return questionsEvaluations;
    }

    public Double getTestEvaluation() {
        return testEvaluation;
    }

    public ArrayList<String> getObjectives() {
        return objectives;
    }

    public ArrayList<Integer> getObjectivesIDs() {
        return objectivesIDs;
    }

    public Boolean getSynchroneousQuestionsTest() {
        return isSynchroneousQuestionsTest;
    }

    public Integer getTestMode() {
        return testMode;
    }

    public String getMedalsInstructions() {
        return medalsInstructions;
    }

    public String getMediaFileName() {
        return mediaFileName;
    }

    public Integer getSendMediaFile() {
        return sendMediaFile;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    //setter
    public void setTestEvaluation(Double testEvaluation) {
        this.testEvaluation = testEvaluation;
    }

    public void setQuestionsEvaluations(ArrayList<Double> questionsEvaluations) {
        this.questionsEvaluations = questionsEvaluations;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setIdTest(String idTest) {
        this.idTest = idTest;
    }

    public void setIdsQuestions(ArrayList<String> idsQuestions) {
        this.idsQuestions = idsQuestions;
    }

    public void setGenericQuestions(ArrayList<QuestionGeneric> genericQuestions) {
        this.genericQuestions = genericQuestions;
    }

    public void setObjectives(ArrayList<String> objectives) {
        this.objectives = objectives;
    }

    public void setObjectivesIDs(ArrayList<Integer> objectivesIDs) {
        this.objectivesIDs = objectivesIDs;
    }

    public void setSynchroneousQuestionsTest(Boolean synchroneousQuestionsTest) {
        isSynchroneousQuestionsTest = synchroneousQuestionsTest;
    }

    public void setTestMode(Integer testMode) {
        this.testMode = testMode;
    }

    public void setMedalsInstructions(String medalsInstructions) {
        this.medalsInstructions = medalsInstructions;
    }

    public void setMediaFileName(String mediaFileName) {
        this.mediaFileName = mediaFileName;
    }

    public void setSendMediaFile(Integer sendMediaFile) {
        this.sendMediaFile = sendMediaFile;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
