package koeko.database_management;

import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;


public class DbTableIndividualQuestionForStudentResultTest {
    String questionID = "";
    @Before
    public void insertIntoDBForTest() {
        QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
        questionMultipleChoice.setQUESTION("Question multiple choice for testing the histogram display of results.");
        questionMultipleChoice.setOPT0("First Option (right)");
        questionMultipleChoice.setOPT1("Second Option (wrong)");
        questionMultipleChoice.setOPT2("Third Option (wrong)");
        questionMultipleChoice.setOPT3("Fourth Option (wrong)");
        questionMultipleChoice.setOPT4("Fifth Option (wrong)");
        questionMultipleChoice.setOPT5("Sixth Option (wrong)");
        questionMultipleChoice.setOPT6("Seventh Option (wrong)");
        try {
            questionID = DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(questionMultipleChoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String anatol = DbTableStudents.addStudent("noaddress","Anatol");
        String sophie = DbTableStudents.addStudent("noaddress","Sophie");
        String arthur = DbTableStudents.addStudent("noaddress","Arthur");
        String perceval = DbTableStudents.addStudent("noaddress","Perceval");
        String justin = DbTableStudents.addStudent("noaddress","Justin");
        String artemis = DbTableStudents.addStudent("noaddress","Artemis");

        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,anatol,
                "First Option (right)|||Second Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,anatol,
                "Third Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,sophie,
                "First Option (right)|||Second Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,arthur,
                "First Option (right)|||Third Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,perceval,
                "Fourth Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,justin,
                "Fifth Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,justin,
                "Seventh Option (wrong)","no type");
        DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(questionID,artemis,
                "Seventh Option (wrong)","no type");
        DbTableClasses.addClass("test indiv res table class", "", "");
        DbTableRelationClassStudent.addClassStudentRelation("test indiv res table class", "Anatol");
        DbTableRelationClassStudent.addClassStudentRelation("test indiv res table class", "Justin");
        DbTableRelationClassStudent.addClassStudentRelation("test indiv res table class", "Artemis");
    }

    @Test
    public void testgetAnswersHistogramForQuestion() {
        ArrayList<ArrayList> result = DbTableIndividualQuestionForStudentResult.getAnswersHistogramForQuestion(questionID, "All Classes", "All Classes");

        //test first array
        ArrayList<String> options = new ArrayList<>();
        options.add("First Option (right)");
        options.add("Second Option (wrong)");
        options.add("Third Option (wrong)");
        options.add("Fourth Option (wrong)");
        options.add("Fifth Option (wrong)");
        options.add("Sixth Option (wrong)");
        options.add("Seventh Option (wrong)");
        assertEquals(true, result.get(0).equals(options));

        //test second array
        ArrayList<Integer> values = new ArrayList<>();
        values.add(3);
        values.add(2);
        values.add(2);
        values.add(1);
        values.add(1);
        values.add(0);
        values.add(2);
        assertEquals(true, result.get(1).equals(values));

        //test with class
        ArrayList<ArrayList> result2 = DbTableIndividualQuestionForStudentResult.getAnswersHistogramForQuestion(questionID, "test indiv res table class", "All Classes");
        ArrayList<Integer> values2 = new ArrayList<>();
        values2.add(1);
        values2.add(1);
        values2.add(1);
        values2.add(0);
        values2.add(1);
        values2.add(0);
        values2.add(2);
        assertEquals(true, result2.get(0).equals(options));
        assertEquals(true, result2.get(1).equals(values2));
    }

    @After
    public void removeTestFromDB() {

    }

}