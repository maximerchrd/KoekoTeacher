package com.wideworld.learningtrackerteacher.database_management;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class DbTableRelationQuestionQuestionTest {

    @Before
    public void insertIntoDBForTest() {
        //test beginner
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","3","testForTest2*$@", "");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","2","testForTest2*$@", "EVALUATION<60");

        //test easy
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","2","testForTest3*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("2","3","testForTest3*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","4","testForTest3*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("2","4","testForTest3*$@", "");

        //test 1
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("5","6","testForTest*$@", "");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","2","testForTest*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","3","testForTest*$@", "");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("2","7","testForTest*$@", "");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("3","4","testForTest*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("4","5","testForTest*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("4","6","testForTest*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","7","testForTest*$@", "EVALUATION<60");

        //test 2
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","2","testForTestTwo*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("2","3","testForTestTwo*$@", "EVALUATION<60");
        DbTableRelationQuestionQuestion.addRelationQuestionQuestion("1","4","testForTestTwo*$@", "");
    }

    @Test
    public void testBeginnerQuestionQuestionRelation() {
        assertEquals("1;;;3:::;;;2:::EVALUATION<60|||2|||3|||",DbTableRelationQuestionQuestion.getFormattedQuestionsLinkedToTest("testForTest2*$@"));
    }

    @Test
    public void testEasyQuestionQuestionRelation() {
        assertEquals("1;;;2:::EVALUATION<60;;;4:::EVALUATION<60|||2;;;3:::EVALUATION<60;;;4:::|||3|||4|||",DbTableRelationQuestionQuestion.getFormattedQuestionsLinkedToTest("testForTest3*$@"));
    }

    @Test
    public void testOneQuestionQuestionRelation() {
        assertEquals("1;;;2:::EVALUATION<60;;;3:::;;;7:::EVALUATION<60|||2;;;7:::|||7|||3;;;4:::EVALUATION<60|||4;;;5:::EVALUATION<60;;;6:::EVALUATION<60|||5;;;6:::|||6|||",DbTableRelationQuestionQuestion.getFormattedQuestionsLinkedToTest("testForTest*$@"));
    }

    @Test
    public void testTwoQuestionQuestionRelation() {
        assertEquals("1;;;2:::EVALUATION<60;;;4:::|||2;;;3:::EVALUATION<60|||3|||4|||",DbTableRelationQuestionQuestion.getFormattedQuestionsLinkedToTest("testForTestTwo*$@"));
    }

    @After
    public void removeTestFromDB() {
        DbTableRelationQuestionQuestion.removeRelationsWithTest("testForTest2*$@");
        DbTableRelationQuestionQuestion.removeRelationsWithTest("testForTest3*$@");
        DbTableRelationQuestionQuestion.removeRelationsWithTest("testForTest*$@");
        DbTableRelationQuestionQuestion.removeRelationsWithTest("testForTestTwo*$@");
    }

}