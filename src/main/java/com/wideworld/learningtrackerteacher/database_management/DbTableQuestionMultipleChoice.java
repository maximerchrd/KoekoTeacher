package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZonedDateTime;

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
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15))";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * method for inserting new question into table multiple_choice_question
     * @param quest
     * @throws Exception
     */
    static public void addMultipleChoiceQuestion(QuestionMultipleChoice quest) throws Exception {
        String globalID = DbTableQuestionGeneric.addGenericQuestion(0);
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT INTO multiple_choice_questions (LEVEL,QUESTION,OPTION0," +
                    "OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9," +
                    "NB_CORRECT_ANS,IMAGE_PATH,ID_GLOBAL,MODIF_DATE) " +
                    "VALUES ('" +
                    quest.getLEVEL() + "','" +
                    quest.getQUESTION() + "','" +
                    quest.getOPT0() + "','" +
                    quest.getOPT1() + "','" +
                    quest.getOPT2() + "','" +
                    quest.getOPT3() + "','" +
                    quest.getOPT4() + "','" +
                    quest.getOPT5() + "','" +
                    quest.getOPT6() + "','" +
                    quest.getOPT7() + "','" +
                    quest.getOPT8() + "','" +
                    quest.getOPT9() + "','" +
                    quest.getNB_CORRECT_ANS() + "','" +
                    quest.getIMAGE() + "','" +
                    globalID +"','" +
                    ZonedDateTime.now() +"');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * method for inserting new question into table multiple_choice_question
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
                    "MODIF_DATE='" + ZonedDateTime.now() + "' " +
                    "WHERE ID_GLOBAL='" + ZonedDateTime.now() + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    /**
     * method for inserting new question into table multiple_choice_question
     * @param questionID
     * @throws Exception
     */
    static public QuestionMultipleChoice getMultipleChoiceQuestionWithID(int questionID) {
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
            String query = 	"SELECT LEVEL,QUESTION,OPTION0," +
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
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        questionMultipleChoice.setObjectives(DbTableLearningObjectives.getObjectiveForQuestionID(questionID));
        questionMultipleChoice.setSubjects(DbTableSubject.getSubjectsForQuestionID(questionID));
        return questionMultipleChoice;
    }
    static public int getLastIDGlobal() throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        int last_id_global = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"SELECT  ID_GLOBAL FROM multiple_choice_questions WHERE ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM multiple_choice_questions);";
            ResultSet result_query = stmt.executeQuery(sql);
            last_id_global = Integer.parseInt(result_query.getString(1));
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return last_id_global;
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
            String sql = 	"DELETE FROM multiple_choice_questions WHERE ID_GLOBAL = '" + ID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}