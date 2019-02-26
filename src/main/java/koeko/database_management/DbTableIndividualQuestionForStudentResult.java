package koeko.database_management;

import koeko.controllers.ResultsTable.SingleResultForTable;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;
import koeko.students_management.Student;
import koeko.view.Result;
import koeko.view.Utilities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableIndividualQuestionForStudentResult {
    static public final int type2ClassroomActivity = 0;
    static public final int type2Homework = 2;
    static public final int type2FreePractice = 3;
    static private final String TABLE_NAME = "individual_question_for_student_result";
    static private final String KEY_QUESTION_TYPE = "QUESTION_TYPE";
    static private final String KEY_QUESTION_TYPE2 = "QUESTION_TYPE2";
    static private final String KEY_ID_GLOBAL = "ID_GLOBAL";
    static private final String KEY_ID_STUDENT_GLOBAL = "ID_STUDENT_GLOBAL";
    static private final String KEY_DATE = "DATE";
    static private final String KEY_ANSWERS = "ANSWERS";
    static private final String KEY_TIME_FOR_SOLVING = "TIME_FOR_SOLVING";
    static private final String KEY_QUESTION_WEIGHT = "QUESTION_WEIGHT";
    static private final String KEY_EVAL_TYPE = "EVAL_TYPE";
    static private final String KEY_QUANTITATIVE_EVAL = "QUANTITATIVE_EVAL";
    static private final String KEY_QUALITATIVE_EVAL = "QUALITATIVE_EVAL";
    static private final String KEY_TEST_BELONGING = "TEST_BELONGING";
    static private final String KEY_WEIGHTS_OF_ANSWERS = "WEIGHTS_OF_ANSWERS";

    static public void createTableDirectEvaluationOfObjective(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS individual_question_for_student_result " +
                    "(ID_DIRECT_EVAL        INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " QUESTION_TYPE             INT, " +        //0: Question Multiple Choice; 1: Question Short Answer; 2: Objective
                    " QUESTION_TYPE2             INT, " +
                    " ID_GLOBAL             INT    NOT NULL, " +
                    " ID_STUDENT_GLOBAL     INT    NOT NULL, " +
                    " DATE                  TEXT    NOT NULL, " +
                    " ANSWERS               TEXT    NOT NULL, " +
                    " TIME_FOR_SOLVING      INT    NOT NULL, " +
                    " QUESTION_WEIGHT       REAL    NOT NULL, " +
                    " EVAL_TYPE             TEXT    NOT NULL, " +
                    " QUANTITATIVE_EVAL     TEXT    NOT NULL, " +
                    " QUALITATIVE_EVAL       TEXT    NOT NULL, " +
                    " TEST_BELONGING        TEXT    NOT NULL, " +
                    " WEIGHTS_OF_ANSWERS    TEXT    NOT NULL) ";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public double addIndividualQuestionForStudentResult(String id_global, String studentId, String answers, String answerType) {
        double quantitative_evaluation = -1;
        String sql = "INSERT INTO individual_question_for_student_result (ID_GLOBAL,ID_STUDENT_GLOBAL,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                "VALUES (?,?,date('now'),?,'none','none','none','none','none','none','none');";
        DbUtils.updateWithThreeParam(sql, id_global, studentId, answers);

        if (answerType.contains("ANSW0")) {
            // correcting the answers and evaluate the multiple choice question in %
            String[] student_answers_array = answers.split("\\|\\|\\|");
            ArrayList<Integer> codedStudentAnswers = new ArrayList<>();
            int number_answers = 0;
            ArrayList<String> allOptionsVector = new ArrayList<>();
            ArrayList<String> rightAnswersArray = new ArrayList<>();

            String query = "SELECT OPTION0,OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9,NB_CORRECT_ANS " +
                    "FROM multiple_choice_questions WHERE ID_GLOBAL = ?";

            try (Connection conn = Utilities.getDbConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {
                preparedStatement.setString(1, id_global);
                ResultSet rs = preparedStatement.executeQuery();

                if (rs.next()) {
                    for (int i = 1; i < 11; i++) {
                        if (!rs.getString(i).equals(" ")) {
                            allOptionsVector.add(rs.getString(i));
                            number_answers++;
                        }
                    }
                    for (int i = 0; i < rs.getInt(11); i++) {
                        rightAnswersArray.add(rs.getString(i + 1));
                    }
                } else {
                    System.out.println("problem writing result: probably no corresponding question ID");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //code the student answers
            List<String> studentAnswers = Arrays.asList(student_answers_array);
            for (int i = 0; i < allOptionsVector.size(); i++) {
                if (studentAnswers.contains(allOptionsVector.get(i))) {
                    codedStudentAnswers.add(i + 1);
                }
            }

            //evaluate the answer according to the correction mode
            int number_rignt_checked_answers_from_student = 0;
            for (int i = 0; i < rightAnswersArray.size(); i++) {
                if (Arrays.asList(student_answers_array).contains(rightAnswersArray.get(i))) {
                    number_rignt_checked_answers_from_student++;
                }
            }
            int number_right_unchecked_answers_from_student = 0;
            for (int i = 0; i < number_answers; i++) {
                if (!Arrays.asList(rightAnswersArray).contains(allOptionsVector.get(i)) && !Arrays.asList(student_answers_array).contains(allOptionsVector.get(i))) {
                    number_right_unchecked_answers_from_student++;
                }
            }

            String correctionMode = DbTableQuestionMultipleChoice.getCorrectionMode(id_global);
            if (correctionMode.contentEquals("AllOrNothing") || correctionMode.contentEquals("")) {
                try {
                    quantitative_evaluation = 100 * (number_rignt_checked_answers_from_student + number_right_unchecked_answers_from_student) / number_answers;
                    if (quantitative_evaluation < 100) {
                        quantitative_evaluation = 0;
                    }
                } catch (ArithmeticException e) {
                    e.printStackTrace();
                }
            } else if (correctionMode.contentEquals("PercentCorrectDecisions")) {
                quantitative_evaluation = 100 * (number_rignt_checked_answers_from_student + number_right_unchecked_answers_from_student) / number_answers;
            } else if (correctionMode.contains("Custom")) {
                quantitative_evaluation = DbTableIndividualQuestionForStudentResult.getCustomEvaluationForQMC(correctionMode.replace("Custom#", ""), codedStudentAnswers);
            }
        } else if (answerType.contains("ANSW1")) {
            QuestionShortAnswer questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(id_global);
            String answer = answers.replace("|||", "");
            quantitative_evaluation = 0;
            if (questionShortAnswer.getANSWER().contains(answer)) {
                quantitative_evaluation = 100;
            }
        }

        System.out.println("student result: " + quantitative_evaluation);

        sql = "UPDATE individual_question_for_student_result SET QUANTITATIVE_EVAL = ? WHERE ID_DIRECT_EVAL = (SELECT MAX(ID_DIRECT_EVAL) FROM individual_question_for_student_result);";
        DbUtils.updateWithOneParam(sql, String.valueOf(quantitative_evaluation));

        return quantitative_evaluation;
    }

    static public void addIndividualObjectiveForStudentResult(String id_global, String student_name, String quantitativeEvaluation, String evalType, String testName) {

        String sql = "DELETE FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL = " +
                "(SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = ?) AND ID_GLOBAL = ? AND TEST_BELONGING = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, student_name);
            pstmt.setString(2, id_global);
            pstmt.setString(3, testName);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "INSERT INTO individual_question_for_student_result (QUESTION_TYPE,ID_GLOBAL,ID_STUDENT_GLOBAL,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                "VALUES ('2',?,(SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = ?),date('now'),'none','none','none',?,?,'none',?,'none');";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, String.valueOf(id_global));
            pstmt.setString(2, student_name);
            pstmt.setString(3, evalType);
            pstmt.setString(4, String.valueOf(quantitativeEvaluation));
            pstmt.setString(5, testName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static public double addIndividualTestEval(String id_global, String student_name, Double testEvaluation) {
        double quantitative_evaluation = -1;
        student_name = student_name;
        String sql = "INSERT INTO individual_question_for_student_result (ID_GLOBAL,ID_STUDENT_GLOBAL,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                "VALUES (?,'-1',date('now'),'none','none','none','none',?,'none','none','none');";
        DbUtils.updateWithTwoParam(sql, id_global, String.valueOf(testEvaluation));
        sql = "UPDATE individual_question_for_student_result SET ID_STUDENT_GLOBAL = (SELECT ID_STUDENT_GLOBAL FROM students " +
                "WHERE FIRST_NAME = ?) WHERE ID_DIRECT_EVAL = (SELECT MAX(ID_DIRECT_EVAL) FROM individual_question_for_student_result);";
        DbUtils.updateWithOneParam(sql, student_name);

        return quantitative_evaluation;
    }

    /**
     * Put the string "" as testName if you want the results independently from the test
     *
     * @param studentName
     * @param objectiveID
     * @param testName
     * @return
     */
    static public String getResultForStudentForObjectiveInTest(String studentName, String objectiveID, String testName) {
        ArrayList<String> results = new ArrayList<>();
        String result = "";
        String sql = "";
        if (testName.contentEquals("")) {
            sql = "SELECT QUANTITATIVE_EVAL FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL = " +
                    "(SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = ?) AND ID_GLOBAL = " +
                    "(SELECT ID_GLOBAL FROM question_objective_relation WHERE ID_OBJECTIVE_GLOBAL=?)";
        } else {
            sql = "SELECT QUANTITATIVE_EVAL FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL = " +
                    "(SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = ?) AND ID_GLOBAL = ? AND TEST_BELONGING = ?";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, studentName);
            pstmt.setString(2, objectiveID);
            if (!testName.contentEquals("")) {
                pstmt.setString(3, testName);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString("QUANTITATIVE_EVAL"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        Double resultDouble = 0.0;
        for (String res : results) {
            resultDouble += Double.valueOf(res);
        }
        result = String.valueOf(resultDouble / results.size());

        return result;
    }

    static public void deleteResultForStudentForObjectiveInTest(String studentName, String objectiveID, String testName) {
        String sql = "DELETE FROM individual_question_for_student_result WHERE ID_STUDENT_GLOBAL = " +
                "(SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = ?) AND ID_GLOBAL = ? AND TEST_BELONGING = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, studentName);
            pstmt.setString(2, objectiveID);
            pstmt.setString(3, testName);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static public String getEvalForQuestionAndStudentIDs(String globalID, String globalStudentID) {
        String evaluation = "";
        String identifier = "";
        String query = "SELECT ID_DIRECT_EVAL,QUANTITATIVE_EVAL FROM individual_question_for_student_result " +
                "WHERE (ID_STUDENT_GLOBAL=? AND ID_GLOBAL=?);";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, globalStudentID);
            stmt.setString(2, globalID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                evaluation = rs.getString("QUANTITATIVE_EVAL");
                identifier = rs.getString("ID_DIRECT_EVAL");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evaluation + "///" + identifier;
    }

    static public ArrayList<ArrayList<String>> getEvalAndDateForStudentID(String globalStudentID) {
        ArrayList<ArrayList<String>> evalAndDates = new ArrayList<>();
        ArrayList<String> evaluations = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        String query = "SELECT QUANTITATIVE_EVAL,DATE FROM individual_question_for_student_result " +
                "WHERE ID_STUDENT_GLOBAL=?;";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(query)) {
            pstmt.setString(1, globalStudentID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                evaluations.add(rs.getString("QUANTITATIVE_EVAL"));
                dates.add(rs.getString("DATE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        evalAndDates.add(evaluations);
        evalAndDates.add(dates);

        return evalAndDates;
    }

    static public ArrayList<SingleResultForTable> getAllSingleResults() {
        ArrayList<ArrayList<String>> namesAndIds = DbTableStudents.getStudentNamesAndIds();
        ArrayList<String> names = namesAndIds.get(0);
        ArrayList<String> ids = namesAndIds.get(1);
        ArrayList<String> evaluations = new ArrayList<>();
        ArrayList<String> idGlobals = new ArrayList<>();
        ArrayList<String> studentIds = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<SingleResultForTable> resultsArray = new ArrayList<>();
        String query = "SELECT ID_GLOBAL,ID_STUDENT_GLOBAL,DATE,ANSWERS,QUANTITATIVE_EVAL FROM individual_question_for_student_result " +
                "ORDER BY ID_DIRECT_EVAL DESC;";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            ResultSet rs0 = stmt.executeQuery();
            while (rs0.next()) {
                evaluations.add(rs0.getString("QUANTITATIVE_EVAL"));
                idGlobals.add(rs0.getString("ID_GLOBAL"));
                studentIds.add(rs0.getString("ID_STUDENT_GLOBAL"));
                answers.add(rs0.getString("ANSWERS"));
                dates.add(rs0.getString("DATE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int j = 0; j < evaluations.size(); j++) {
            QuestionMultipleChoice questionMultipleChoice;
            QuestionShortAnswer questionShortAnswer;
            int questionType = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(idGlobals.get(j));

            SingleResultForTable tempSingleResult = new SingleResultForTable();
            if (questionType == 0) {
                questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(idGlobals.get(j));
                questionMultipleChoice.setSubjects(DbTableSubject.getSubjectsForQuestionID(idGlobals.get(j)));
                questionMultipleChoice.setObjectives(DbTableLearningObjectives.getObjectiveForQuestionID(idGlobals.get(j)));
                int nameIndex = ids.indexOf(studentIds.get(j));
                if (nameIndex >= 0) {
                    tempSingleResult.setName(names.get(nameIndex));
                } else {
                    tempSingleResult.setName("No name");
                    System.err.println("Printing raw results: student id doesn't match any name");
                }
                tempSingleResult.setDate(dates.get(j));
                tempSingleResult.setEvaluation(evaluations.get(j));
                tempSingleResult.setQuestion(questionMultipleChoice.getQUESTION());
                tempSingleResult.setStudentsAnswer(answers.get(j));
                String correctAnswers = "";
                for (int i = 0; i < questionMultipleChoice.getCorrectAnswers().size(); i++) {
                    correctAnswers += questionMultipleChoice.getCorrectAnswers().get(i) + ";";
                }
                tempSingleResult.setCorrectAnswer(correctAnswers);
                String incorrectAnswers = "";
                for (int i = 0; i < questionMultipleChoice.getIncorrectAnswers().size(); i++) {
                    incorrectAnswers += questionMultipleChoice.getIncorrectAnswers().get(i) + ";";
                }
                tempSingleResult.setIncorrectAnswer(incorrectAnswers);
                String subjects = "";
                for (int i = 0; i < questionMultipleChoice.getSubjects().size(); i++) {
                    subjects += questionMultipleChoice.getSubjects().get(i) + ";";
                }
                tempSingleResult.setSubjects(subjects);
                String objectives = "";
                for (int i = 0; i < questionMultipleChoice.getObjectives().size(); i++) {
                    objectives += questionMultipleChoice.getObjectives().get(i) + ";";
                }
                tempSingleResult.setObjectives(objectives);
            } else {
                questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(idGlobals.get(j));
                questionShortAnswer.setSubjects(DbTableSubject.getSubjectsForQuestionID(idGlobals.get(j)));
                questionShortAnswer.setObjectives(DbTableLearningObjectives.getObjectiveForQuestionID(idGlobals.get(j)));
                int nameIndex = ids.indexOf(studentIds.get(j));
                if (nameIndex >= 0) {
                    tempSingleResult.setName(names.get(nameIndex));
                } else {
                    tempSingleResult.setName("No name");
                    System.err.println("Printing raw results: student id doesn't match any name");
                }
                tempSingleResult.setDate(dates.get(j));
                tempSingleResult.setEvaluation(evaluations.get(j));
                tempSingleResult.setQuestion(questionShortAnswer.getQUESTION());
                tempSingleResult.setStudentsAnswer(answers.get(j));
                String correctAnswers = "";
                for (int i = 0; i < questionShortAnswer.getANSWER().size(); i++) {
                    correctAnswers += questionShortAnswer.getANSWER().get(i) + ";";
                }
                tempSingleResult.setCorrectAnswer(correctAnswers);
                String subjects = "";
                for (int i = 0; i < questionShortAnswer.getSubjects().size(); i++) {
                    subjects += questionShortAnswer.getSubjects().get(i) + ";";
                }
                tempSingleResult.setSubjects(subjects);
                String objectives = "";
                for (int i = 0; i < questionShortAnswer.getObjectives().size(); i++) {
                    objectives += questionShortAnswer.getObjectives().get(i) + ";";
                }
                tempSingleResult.setObjectives(objectives);
            }
            resultsArray.add(tempSingleResult);
        }

        return resultsArray;
    }

    /**
     * Method that returns a histogram of the number of hits vs the answer options for a certain question
     *
     * @param questionID
     * @param className  (set to "" if you want the result for all classes)
     * @return an array of 2 arrays containing the answer options (one array) and the number of students who chose
     * this option (second array)
     * IF THE QUESTION ID CORRESPONDS TO A SHRTAQ, RETURNS NULL
     */
    static public ArrayList<ArrayList> getAnswersHistogramForQuestion(String questionID, String className, String allClassesString) {
        ArrayList<ArrayList> arraysToReturn = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        ArrayList<Integer> histogram = new ArrayList<>();

        ArrayList<String> studentAnswers = new ArrayList<>();
        ArrayList<String> studentsID = new ArrayList<>();

        //extract the students from the class if a class name is given
        ArrayList<Student> studentsFromClass;
        ArrayList<String> studentsFromClassID = new ArrayList<>();
        if (!className.contentEquals(allClassesString)) {
            studentsFromClass = DbTableClasses.getStudentsInClass(className);
            for (Student student : studentsFromClass) {
                studentsFromClassID.add(student.getStudentID());
            }
        }

        QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionID);
        answers.addAll(questionMultipleChoice.getAnswers());

        //initialize histogram to 0
        for (String answer : answers) {
            histogram.add(0);
        }

        if (questionMultipleChoice.getQUESTION().length() == 0) {
            arraysToReturn = null;
        } else {
            String sql = "SELECT ANSWERS,ID_STUDENT_GLOBAL FROM individual_question_for_student_result WHERE ID_GLOBAL = ?";
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // set the corresponding param
                pstmt.setString(1, questionID);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    studentAnswers.add(rs.getString("ANSWERS"));
                    studentsID.add(rs.getString("ID_STUDENT_GLOBAL"));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            //extract nb of hits from student answers
            for (int j = 0; j < studentAnswers.size(); j++) {
                if (className.contentEquals(allClassesString) || studentsFromClassID.contains(studentsID.get(j))) {
                    String[] singleAnswers = studentAnswers.get(j).split("\\|\\|\\|");
                    for (int i = 0; i < singleAnswers.length; i++) {
                        int index = answers.indexOf(singleAnswers[i]);
                        if (index >= 0) {
                            histogram.set(index, histogram.get(index) + 1);
                        } else {
                            System.out.println("Problem in getAnswersHistogramForQuestion: answer in indiv results doesn't " +
                                    "correspond to the one of QuestionMultipleChoice");
                        }
                    }
                }
            }

            arraysToReturn.add(answers);
            arraysToReturn.add(histogram);
        }
        return arraysToReturn;
    }

    static public void setEvalForQuestionAndStudentIDs(Double eval, String identifier) {
        String sql = "UPDATE individual_question_for_student_result SET QUANTITATIVE_EVAL = ? " +
                "WHERE ID_DIRECT_EVAL = ?";
        DbUtils.updateWithTwoParam(sql, String.valueOf(eval), identifier);
    }

    /**
     * studentAnswers: coded checked answers
     * <p>
     * negative:allow           (to allow negative evaluation)
     * <p>
     * 1,2,3,4,etc are the answer options in the order they are stored in the db
     * 1:5/-1   means that if the option 1 is checked, 5 "points" are added to the score for the question,
     * and if option 1 is not checked, -1 "point" is subtracted to the score.
     * In the end, the score is divided by the max possible score.
     * <p>
     * If no "/" is detected on the line, the instruction is assumed to make a combination of checked/unchecked options
     * to a certain evaluation:
     * 2,4,5:80     (means that if the options, 2,4 and 5 are checked and the other options are unchecked, the eval will be 80
     */
    static private Double getCustomEvaluationForQMC(String customEvaluationCode, ArrayList<Integer> studentAnswers) {
        String[] evalInstructions = customEvaluationCode.split("\n");
        List<String> evalInstructionsList = Arrays.asList(evalInstructions);

        //parse negative allowed
        Boolean negativeAllowed = false;
        if (evalInstructionsList.contains("negative:allowed")) {
            negativeAllowed = true;
            evalInstructionsList.remove("negative:allowed");
        }

        Integer maxScore = 0;
        Integer score = 0;


        for (String instruction : evalInstructionsList) {
            //parse instructions with "/"
            if (instruction.contains("/")) {
                Integer answer = Integer.valueOf(instruction.split(":")[0]);
                Integer scorechecked = Integer.valueOf(instruction.split(":")[1].split("/")[0]);
                Integer scoreunchecked = Integer.valueOf(instruction.split(":")[1].split("/")[1]);

                if (scorechecked > scoreunchecked) {
                    maxScore += scorechecked;
                } else {
                    maxScore += scoreunchecked;
                }

                if (studentAnswers.contains(answer)) {
                    score += scorechecked;
                } else {
                    score += scoreunchecked;
                }
            } else {
                String[] checkedQuestions = instruction.split(":")[0].split(",");
                ArrayList<Integer> answers = new ArrayList<>();

                for (int i = 0; i < checkedQuestions.length; i++) {
                    answers.add(Integer.valueOf(checkedQuestions[i]));
                }

                if (studentAnswers.containsAll(answers) && answers.containsAll(studentAnswers)) {
                    return Double.parseDouble(instruction.split(":")[1]);
                }
            }
        }

        Double finalScore = 100 * score.doubleValue() / maxScore.doubleValue();

        if (finalScore < 0 && negativeAllowed) {
            return finalScore;
        } else if (finalScore < 0 && !negativeAllowed) {
            return 0.0;
        } else {
            return finalScore;
        }
    }

    public static void addResult(Result result, String studentId) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" + KEY_ANSWERS + "," + KEY_DATE + "," + KEY_EVAL_TYPE + "," +
                KEY_ID_GLOBAL + "," + KEY_ID_STUDENT_GLOBAL + "," + KEY_QUALITATIVE_EVAL + "," + KEY_QUANTITATIVE_EVAL + "," + KEY_QUESTION_TYPE + "," +
                KEY_QUESTION_TYPE2 + "," + KEY_QUESTION_WEIGHT + "," + KEY_TEST_BELONGING + "," + KEY_TIME_FOR_SOLVING + "," +
                KEY_WEIGHTS_OF_ANSWERS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, result.getAnswers());
            stmt.setString(2, result.getDate());
            stmt.setString(3, result.getEvalType());
            stmt.setString(4, result.getResourceUid());
            stmt.setString(5, studentId);
            stmt.setString(6, result.getQualitativeEval());
            stmt.setString(7, result.getQuantitativeEval());
            stmt.setInt(8, result.getType1());
            stmt.setInt(9, result.getType2());
            stmt.setString(10, String.valueOf(result.getResourceWeight()));
            stmt.setString(11, result.getTestBelonging());
            stmt.setInt(12, result.getTimeForSolving());
            stmt.setString(13, result.getAnswersWeights());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
