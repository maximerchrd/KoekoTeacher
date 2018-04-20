package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionShortAnswer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableQuestionShortAnswer {
    static public void createTableQuestionShortAnswer(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS short_answer_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " LEVEL      INT     NOT NULL, " +
                    " QUESTION           TEXT    NOT NULL, " +
                    " AUTOMATIC_CORRECTION      INT     NOT NULL, " +
                    " IMAGE_PATH           TEXT    NOT NULL, " +
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15))";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public String addShortAnswerQuestion(QuestionShortAnswer quest) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String idGlobal = "-1";
        try {
            idGlobal = DbTableQuestionGeneric.addGenericQuestion(1);
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT INTO short_answer_questions (ID_GLOBAL,LEVEL," +
                    "QUESTION,AUTOMATIC_CORRECTION,IMAGE_PATH,MODIF_DATE) " +
                    "VALUES ('" +
                    idGlobal + "','" +
                    quest.getLEVEL() + "','" +
                    quest.getQUESTION() + "','" +
                    0 + "','" +
                    quest.getIMAGE() + "','" +
                    ZonedDateTime.now() +"');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();

            for (int i = 0; i < quest.getANSWER().size(); i++) {
                DbTableAnswerOptions.addAnswerOption(idGlobal,quest.getANSWER().get(i));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return idGlobal;
    }

    static public void updateShortAnswerQuestion(QuestionShortAnswer quest) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"UPDATE short_answer_questions " +
                    "SET QUESTION='" + quest.getQUESTION() + "', " +
                    "IMAGE_PATH='" + quest.getIMAGE() + "' " +
                    "MODIF_DATE='" + ZonedDateTime.now() + "' " +
                    "WHERE ID_GLOBAL='" + quest.getID() + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
            DbTableAnswerOptions.removeOptionsRelationsQuestion(String.valueOf(quest.getID()));
            for (int i = 0; i < quest.getANSWER().size(); i++) {
                DbTableAnswerOptions.addAnswerOption(String.valueOf(quest.getID()),quest.getANSWER().get(i));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public List<QuestionShortAnswer> getAllShortAnswersQuestions() throws Exception{
        List<QuestionShortAnswer> questionShortAnswerArrayList = new ArrayList<QuestionShortAnswer>();
        // Select All Query
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM short_answer_questions;" );
            while ( rs.next() ) {
                QuestionShortAnswer quest = new QuestionShortAnswer();
                //quest.setSUBJECT(rs.getString(2));
                quest.setID(rs.getInt(2));
                quest.setLEVEL(rs.getString(3));
                quest.setQUESTION(rs.getString(4));
                quest.setIMAGE(rs.getString(5));
                ArrayList<String> answers = new ArrayList<>();
                Statement stmt2 = c.createStatement();
                ResultSet rs2 = stmt2.executeQuery( "SELECT OPTION FROM answer_options " +
                        "INNER JOIN question_answeroption_relation ON answer_options.ID_ANSWEROPTION_GLOBAL = question_answeroption_relation.ID_ANSWEROPTION_GLOBAL " +
                        "INNER JOIN short_answer_questions ON question_answeroption_relation.ID_GLOBAL = short_answer_questions.ID_GLOBAL " +
                        "WHERE short_answer_questions.ID_GLOBAL = '" + quest.getID() +"';" );
                while ( rs2.next() ) {
                    answers.add(rs2.getString(1));
                }
                quest.setANSWER(answers);
                questionShortAnswerArrayList.add(quest);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return questionShortAnswerArrayList;
    }

    static public QuestionShortAnswer getShortAnswerQuestionWithId (int questionId) {
        QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
        questionShortAnswer.setID(questionId);
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = 	"SELECT LEVEL,QUESTION,IMAGE_PATH FROM short_answer_questions WHERE ID_GLOBAL='" + questionId + "';";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionShortAnswer.setLEVEL(rs.getString("LEVEL"));
                questionShortAnswer.setQUESTION(rs.getString("QUESTION"));
                questionShortAnswer.setIMAGE(rs.getString("IMAGE_PATH"));
            }
            ArrayList<String> answers = new ArrayList<>();
            rs = stmt.executeQuery( "SELECT OPTION FROM answer_options " +
                    "INNER JOIN question_answeroption_relation ON answer_options.ID_ANSWEROPTION_GLOBAL = question_answeroption_relation.ID_ANSWEROPTION_GLOBAL " +
                    "INNER JOIN short_answer_questions ON question_answeroption_relation.ID_GLOBAL = short_answer_questions.ID_GLOBAL " +
                    "WHERE short_answer_questions.ID_GLOBAL = '" + questionShortAnswer.getID() +"';" );
            while ( rs.next() ) {
                answers.add(rs.getString(1));
            }
            questionShortAnswer.setANSWER(answers);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        questionShortAnswer.setObjectives(DbTableLearningObjectives.getObjectiveForQuestionID(questionId));
        questionShortAnswer.setSubjects(DbTableSubject.getSubjectsForQuestionID(questionId));
        return questionShortAnswer;
    }
}