package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;
import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;
import com.wideworld.learningtrackerteacher.questions_management.QuestionShortAnswer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maximerichard on 24.11.17.
 * Question Type: 0 = question multiple choice; 1 = question short answer
 */
public class DbTableQuestionGeneric {
    static public void createTableQuestionGeneric(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS generic_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL           INT    NOT NULL, " +
                    " QUESTION_TYPE      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public String addGenericQuestion(int questionType) throws Exception {
        String questID = "-1";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT INTO generic_questions (ID_GLOBAL,QUESTION_TYPE) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    questionType +"');";
            stmt.executeUpdate(sql);
            sql = "UPDATE generic_questions SET ID_GLOBAL = ID_GLOBAL + ID_QUESTION WHERE ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM generic_questions)";
            stmt.executeUpdate(sql);

            sql = "SELECT ID_GLOBAL FROM generic_questions WHERE ID_GLOBAL = (SELECT MAX(ID_GLOBAL) FROM generic_questions)";
            ResultSet result_query = stmt.executeQuery(sql);
            questID = result_query.getString(1);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return questID;
    }

    static public ArrayList<QuestionGeneric> getAllGenericQuestions() throws Exception{
        ArrayList<QuestionGeneric> questionGenericArrayList = new ArrayList<QuestionGeneric>();
        // Select All Query
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM generic_questions;" );
            while ( rs.next() ) {
                QuestionGeneric quest = new QuestionGeneric();
                //quest.setSUBJECT(rs.getString(2));
                quest.setGlobalID(rs.getInt("ID_GLOBAL"));
                quest.setIntTypeOfQuestion(rs.getInt("QUESTION_TYPE"));
                questionGenericArrayList.add(quest);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Read " + questionGenericArrayList.size() + " generic questions.");
        return questionGenericArrayList;
    }

    static public Integer getQuestionTypeFromIDGlobal (String idGlobal) {
        Integer questionType = -1;
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM generic_questions WHERE ID_GLOBAL = '" + idGlobal + "';" );
            if ( rs.next()) {
               questionType = rs.getInt("QUESTION_TYPE");
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return questionType;
    }
}
