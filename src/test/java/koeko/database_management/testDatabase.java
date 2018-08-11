package koeko.database_management;

import koeko.questions_management.QuestionMultipleChoice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;


public class testDatabase {

    @Test
    public void runDbTableIndividualQuestionForStudentResultTests() {
        DbTableIndividualQuestionForStudentResultTest dbTableIndividualQuestionForStudentResultTest = new DbTableIndividualQuestionForStudentResultTest();
        dbTableIndividualQuestionForStudentResultTest.insertIntoDBForTest();
        dbTableIndividualQuestionForStudentResultTest.testgetAnswersHistogramForQuestion();
    }

    @Test
    public void runDbTableRelationQuestionQuestionTests() {
        DbTableRelationQuestionQuestionTest dbTableRelationQuestionQuestionTest = new DbTableRelationQuestionQuestionTest();
        dbTableRelationQuestionQuestionTest.insertIntoDBForTest();
        dbTableRelationQuestionQuestionTest.testBeginnerQuestionQuestionRelation();
        dbTableRelationQuestionQuestionTest.testEasyQuestionQuestionRelation();
        dbTableRelationQuestionQuestionTest.testOneQuestionQuestionRelation();
        dbTableRelationQuestionQuestionTest.testTwoQuestionQuestionRelation();
        dbTableRelationQuestionQuestionTest.removeTestFromDB();
    }
}