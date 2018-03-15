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

    public Test() {
        this.testName = "";
        this.idTest = -1;
        this.idsQuestions = new ArrayList<Integer>();
        this.genericQuestions = new ArrayList<QuestionGeneric>();
    }

    public void addGenericQuestion (QuestionGeneric questionGeneric) {
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
}
