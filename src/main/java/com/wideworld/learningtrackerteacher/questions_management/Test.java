package com.wideworld.learningtrackerteacher.questions_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;

import java.util.ArrayList;

/**
 * Created by maximerichard on 15.01.18.
 */
public class Test {
    private String testName;
    private int idTest;
    private ArrayList<Integer> idsQuestions;
    private ArrayList<QuestionGeneric> genericQuestions;
    private ArrayList<Double> questionsEvaluations;
    private ArrayList<String> objectives;
    private ArrayList<Integer> objectivesIDs;
    private Double testEvaluation;

    public Test() {
        this.testName = "";
        this.idTest = -1;
        this.idsQuestions = new ArrayList<Integer>();
        this.genericQuestions = new ArrayList<QuestionGeneric>();
        this.questionsEvaluations = new ArrayList<>();
    }

    public void addGenericQuestion(QuestionGeneric questionGeneric) {
        genericQuestions.add(questionGeneric);
        idsQuestions.add(questionGeneric.getGlobalID());
    }

    public String getTestName() {
        return testName;
    }

    public int getIdTest() {
        return idTest;
    }

    public ArrayList<Integer> getIdsQuestions() {
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

    public void setTestEvaluation(Double testEvaluation) {
        this.testEvaluation = testEvaluation;
    }

    public void setQuestionsEvaluations(ArrayList<Double> questionsEvaluations) {
        this.questionsEvaluations = questionsEvaluations;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setIdTest(int idTest) {
        this.idTest = idTest;
    }

    public void setIdsQuestions(ArrayList<Integer> idsQuestions) {
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
}
