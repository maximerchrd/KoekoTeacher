package koeko.database_management;

import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;
import koeko.questions_management.Test;
import koeko.view.QuestionMultipleChoiceView;
import koeko.view.Utilities;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableQuestionMultipleChoice {
    static public void createTableQuestionMultipleChoice(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS multiple_choice_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " LEVEL      INT     NOT NULL, " +
                    " QUESTION           TEXT    NOT NULL, " +
                    " OPTION0           TEXT    NOT NULL, " +
                    " OPTION1           TEXT    NOT NULL, " +
                    " OPTION2           TEXT    NOT NULL, " +
                    " OPTION3           TEXT    NOT NULL, " +
                    " OPTION4           TEXT    NOT NULL, " +
                    " OPTION5           TEXT    NOT NULL, " +
                    " OPTION6           TEXT    NOT NULL, " +
                    " OPTION7           TEXT    NOT NULL, " +
                    " OPTION8           TEXT    NOT NULL, " +
                    " OPTION9           TEXT    NOT NULL, " +
                    " NB_CORRECT_ANS   INT     NOT NULL, " +
                    " IMAGE_PATH       TEXT    NOT NULL, " +
                    " ID_GLOBAL        INT     NOT NULL, " +
                    " CORRECTION_MODE TEXT, " +
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15))";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * method for inserting new question into table multiple_choice_question
     *
     * @param quest
     * @throws Exception
     */
    static public String addMultipleChoiceQuestion(QuestionMultipleChoice quest) throws Exception {
        String globalID = DbTableQuestionGeneric.addGenericQuestion(0);
        Connection c = null;
        PreparedStatement preparedStatement = null;
        preparedStatement = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            String sql = "INSERT INTO multiple_choice_questions (LEVEL,QUESTION,OPTION0," +
                    "OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9," +
                    "NB_CORRECT_ANS,IMAGE_PATH,ID_GLOBAL,MODIF_DATE) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            preparedStatement = c.prepareStatement(sql);
            preparedStatement.setString(1, quest.getLEVEL());
            preparedStatement.setString(2, quest.getQUESTION());
            preparedStatement.setString(3, quest.getOPT0());
            preparedStatement.setString(4, quest.getOPT1());
            preparedStatement.setString(5, quest.getOPT2());
            preparedStatement.setString(6, quest.getOPT3());
            preparedStatement.setString(7, quest.getOPT4());
            preparedStatement.setString(8, quest.getOPT5());
            preparedStatement.setString(9, quest.getOPT6());
            preparedStatement.setString(10, quest.getOPT7());
            preparedStatement.setString(11, quest.getOPT8());
            preparedStatement.setString(12, quest.getOPT9());
            preparedStatement.setString(13, String.valueOf(quest.getNB_CORRECT_ANS()));
            preparedStatement.setString(14, quest.getIMAGE());
            preparedStatement.setString(15, globalID);
            preparedStatement.setString(16, Utilities.TimestampForNowAsString());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return globalID;
    }


    /**
     * method for inserting new question into table multiple_choice_question
     *
     * @param quest
     * @throws Exception
     */
    static public boolean checkIfExists(QuestionMultipleChoiceView quest) throws Exception {
        // Check if the question exists already
        boolean bExists = true;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "";
            if (quest.getTYPE() == 0) {
                sql = "SELECT  COUNT(1) FROM multiple_choice_questions WHERE IDENTIFIER = '" + quest.getQCM_MUID() + "';";
            } else {
                sql = "SELECT  COUNT(1) FROM short_answer_questions WHERE IDENTIFIER = '" + quest.getQCM_MUID() + "';";
            }
            ResultSet result_query = stmt.executeQuery(sql);
            bExists = (Integer.parseInt(result_query.getString(1)) > 0);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return bExists;
    }


    /**
     * method for inserting new question into table multiple_choice_question
     *
     * @param quest
     * @throws Exception
     */
    static public void addIfNeededMultipleChoiceQuestionFromView(QuestionMultipleChoiceView quest) throws Exception {
        // Check if the question exists already
        if (checkIfExists(quest))
            return;

        if (quest.getTYPE() == 0) {
            DbTableQuestionGeneric.addGenericQuestion(0, quest.getQCM_MUID());
            String sql = "INSERT INTO multiple_choice_questions (LEVEL,QUESTION,OPTION0," +
                    "OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9," +
                    "NB_CORRECT_ANS,IMAGE_PATH,ID_GLOBAL,IDENTIFIER,MODIF_DATE) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
                 PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, quest.getLEVEL());
                preparedStatement.setString(2, quest.getQUESTION());
                preparedStatement.setString(3, quest.getOPT0());
                preparedStatement.setString(4, quest.getOPT1());
                preparedStatement.setString(5, quest.getOPT2());
                preparedStatement.setString(6, quest.getOPT3());
                preparedStatement.setString(7, quest.getOPT4());
                preparedStatement.setString(8, quest.getOPT5());
                preparedStatement.setString(9, quest.getOPT6());
                preparedStatement.setString(10, quest.getOPT7());
                preparedStatement.setString(11, quest.getOPT8());
                preparedStatement.setString(12, quest.getOPT9());
                preparedStatement.setString(13, String.valueOf(quest.getNB_CORRECT_ANS()));
                preparedStatement.setString(14, quest.getIMAGE());
                preparedStatement.setString(15, quest.getQCM_MUID());
                preparedStatement.setString(16, quest.getQCM_MUID());
                preparedStatement.setString(17, quest.getQCM_UPD_TMS().toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else if (quest.getTYPE() == 1) {
            QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
            questionShortAnswer.setID(quest.getQCM_MUID());
            questionShortAnswer.setQUESTION(quest.getQUESTION());
            questionShortAnswer.setIMAGE(quest.getIMAGE());
            questionShortAnswer.getANSWER().add(quest.getOPT0());
            questionShortAnswer.getANSWER().add(quest.getOPT1());
            questionShortAnswer.getANSWER().add(quest.getOPT2());
            questionShortAnswer.getANSWER().add(quest.getOPT3());
            questionShortAnswer.getANSWER().add(quest.getOPT4());
            questionShortAnswer.getANSWER().add(quest.getOPT5());
            questionShortAnswer.getANSWER().add(quest.getOPT6());
            questionShortAnswer.getANSWER().add(quest.getOPT7());
            questionShortAnswer.getANSWER().add(quest.getOPT8());
            questionShortAnswer.getANSWER().addAll(new ArrayList<>(Arrays.asList(quest.getOPT9().split("///"))));
            questionShortAnswer.setUID(quest.getQCM_MUID());

            DbTableQuestionShortAnswer.addShortAnswerQuestion(questionShortAnswer);
        } else if (quest.getTYPE() == 2) {
            Test test = new Test();
            test.setIdTest(quest.getQCM_MUID());
            test.setTestName(quest.getQUESTION());

            DbTableTests.addTest(test);
        }
    }

    /**
     * method for inserting new question into table multiple_choice_question
     *
     * @param quest
     * @throws Exception
     */
    static public void updateMultipleChoiceQuestion(QuestionMultipleChoice quest) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE multiple_choice_questions " +
                    "SET QUESTION='" + quest.getQUESTION() + "', " +
                    "OPTION0='" + quest.getOPT0() + "', " +
                    "OPTION1='" + quest.getOPT1() + "', " +
                    "OPTION2='" + quest.getOPT2() + "', " +
                    "OPTION3='" + quest.getOPT3() + "', " +
                    "OPTION4='" + quest.getOPT4() + "', " +
                    "OPTION5='" + quest.getOPT5() + "', " +
                    "OPTION6='" + quest.getOPT6() + "', " +
                    "OPTION7='" + quest.getOPT7() + "', " +
                    "OPTION8='" + quest.getOPT8() + "', " +
                    "OPTION9='" + quest.getOPT9() + "', " +
                    "NB_CORRECT_ANS='" + quest.getNB_CORRECT_ANS() + "', " +
                    "IMAGE_PATH='" + quest.getIMAGE() + "', " +
                    "MODIF_DATE='" + Utilities.TimestampForNowAsString() + "' " +
                    "WHERE ID_GLOBAL='" + quest.getID() + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * method for getting question from the ID
     *
     * @param questionID
     * @throws Exception
     */
    static public QuestionMultipleChoice getMultipleChoiceQuestionWithID(String questionID) {
        QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
        questionMultipleChoice.setID(questionID);
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT LEVEL,QUESTION,OPTION0," +
                    "OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9," +
                    "NB_CORRECT_ANS,IMAGE_PATH FROM multiple_choice_questions WHERE ID_GLOBAL='" + questionID + "';";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionMultipleChoice.setLEVEL(rs.getString("LEVEL"));
                questionMultipleChoice.setQUESTION(rs.getString("QUESTION"));
                questionMultipleChoice.setOPT0(rs.getString("OPTION0"));
                questionMultipleChoice.setOPT1(rs.getString("OPTION1"));
                questionMultipleChoice.setOPT2(rs.getString("OPTION2"));
                questionMultipleChoice.setOPT3(rs.getString("OPTION3"));
                questionMultipleChoice.setOPT4(rs.getString("OPTION4"));
                questionMultipleChoice.setOPT5(rs.getString("OPTION5"));
                questionMultipleChoice.setOPT6(rs.getString("OPTION6"));
                questionMultipleChoice.setOPT7(rs.getString("OPTION7"));
                questionMultipleChoice.setOPT8(rs.getString("OPTION8"));
                questionMultipleChoice.setOPT9(rs.getString("OPTION9"));
                questionMultipleChoice.setNB_CORRECT_ANS(rs.getInt("NB_CORRECT_ANS"));
                questionMultipleChoice.setIMAGE(rs.getString("IMAGE_PATH"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        questionMultipleChoice.setObjectives(DbTableLearningObjectives.getObjectiveForQuestionID(questionID));
        questionMultipleChoice.setSubjects(DbTableSubject.getSubjectsForQuestionID(questionID));
        return questionMultipleChoice;
    }

    static public String getMultipleChoiceQuestionIDWithUID(String identifier) {
        String query = "SELECT ID_GLOBAL FROM multiple_choice_questions WHERE IDENTIFIER=?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            PreparedStatement pstmt = c.prepareStatement(query)){
            pstmt.setString(1, identifier);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                return rs.getString("ID_GLOBAL");
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return "";
    }

    static public String getLastIDGlobal() throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String last_id_global = "0";
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT  ID_GLOBAL FROM multiple_choice_questions WHERE ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM multiple_choice_questions);";
            ResultSet result_query = stmt.executeQuery(sql);
            last_id_global = result_query.getString(1);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return last_id_global;
    }

    public static void setCorrectionMode(String correctionMode, String globalId) {
        String sql = "UPDATE multiple_choice_questions SET CORRECTION_MODE = ? WHERE ID_GLOBAL = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, correctionMode);
            pstmt.setString(2, globalId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getCorrectionMode(String globalID) {
        String correctionMode = "";
        String sql = "SELECT CORRECTION_MODE FROM multiple_choice_questions WHERE ID_GLOBAL = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the corresponding param
            pstmt.setString(1, String.valueOf(globalID));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                correctionMode = rs.getString("CORRECTION_MODE");
                if (correctionMode == null) {
                    correctionMode = "";
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return correctionMode;
    }

    private static void QuestionMultipleChoiceViewFromRecord(QuestionMultipleChoiceView questionMultipleChoice, ResultSet rs) throws SQLException {
        questionMultipleChoice.setID(rs.getString("ID_GLOBAL"));
        questionMultipleChoice.setLEVEL(rs.getString("LEVEL"));
        questionMultipleChoice.setQUESTION(rs.getString("QUESTION"));
        questionMultipleChoice.setOPT0(rs.getString("OPTION0"));
        questionMultipleChoice.setOPT1(rs.getString("OPTION1"));
        questionMultipleChoice.setOPT2(rs.getString("OPTION2"));
        questionMultipleChoice.setOPT3(rs.getString("OPTION3"));
        questionMultipleChoice.setOPT4(rs.getString("OPTION4"));
        questionMultipleChoice.setOPT5(rs.getString("OPTION5"));
        questionMultipleChoice.setOPT6(rs.getString("OPTION6"));
        questionMultipleChoice.setOPT7(rs.getString("OPTION7"));
        questionMultipleChoice.setOPT8(rs.getString("OPTION8"));
        questionMultipleChoice.setOPT9(rs.getString("OPTION9"));
        questionMultipleChoice.setNB_CORRECT_ANS(rs.getInt("NB_CORRECT_ANS"));
        questionMultipleChoice.setQCM_MUID(rs.getString("IDENTIFIER"));
        questionMultipleChoice.setIMAGE(rs.getString("IMAGE_PATH"));
        questionMultipleChoice.setQCM_UPD_TMS(rs.getTimestamp("MODIF_DATE"));
        questionMultipleChoice.setTYPE(0);
    }

    static public Vector<QuestionMultipleChoiceView> getQuestionsMultipleChoiceView() {
        Vector<QuestionMultipleChoiceView> questions = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT * FROM multiple_choice_questions WHERE MODIF_DATE > (SELECT LAST_TS FROM syncop) OR LENGTH(TRIM(IDENTIFIER))<15;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                QuestionMultipleChoiceView qcm = new QuestionMultipleChoiceView();
                QuestionMultipleChoiceViewFromRecord(qcm, rs);
                questions.add(qcm);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return questions;
    }


    static public void setResourceMUID(String idQMC, String muid) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE generic_questions SET ID_GLOBAL='" + muid + 
                    "' WHERE ID_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE multiple_choice_questions SET ID_GLOBAL='" + muid + "',IDENTIFIER='" + muid +
                    "' WHERE ID_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE short_answer_questions SET ID_GLOBAL='" + muid + "',IDENTIFIER='" + muid +
                    "' WHERE ID_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE question_answeroption_relation SET ID_GLOBAL='" + muid +
                    "' WHERE ID_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE question_objective_relation SET ID_GLOBAL='" + muid +
                    "' WHERE ID_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE tests SET ID_TEST_GLOBAL='" + muid + "',IDENTIFIER='" + muid +
                    "' WHERE ID_TEST_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE question_question_relation SET ID_GLOBAL_1='" + muid +
                    "' WHERE ID_GLOBAL_1=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE question_question_relation SET ID_GLOBAL_2='" + muid +
                    "' WHERE ID_GLOBAL_2=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE question_question_relation SET TEST_ID='" + muid +
                    "' WHERE TEST_ID=" + idQMC + ";";
            stmt.executeUpdate(sql);
            sql = "UPDATE question_subject_relation SET ID_GLOBAL='" + muid +
                    "' WHERE ID_GLOBAL=" + idQMC + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public void removeMultipleChoiceQuestionWithID(String ID) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        int last_id_global = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM multiple_choice_questions WHERE ID_GLOBAL = '" + ID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
}