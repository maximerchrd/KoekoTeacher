package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionSubject {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_subject_relation " +
                    "(ID_SUBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_SUBJECT_GLOBAL      INT     NOT NULL, " +
                    " SUBJECT_LEVEL      INT NOT NULL) ";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and a subject
     * by linking the last added question with the subject given as parameter
     *
     * @param subject
     * @throws Exception
     */
    static public void addRelationQuestionSubject(String subject) throws Exception {
        //first get the list of all subjects linked to the question

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT t1.ID_GLOBAL,t2.ID_SUBJECT_GLOBAL," +
                    "'1' FROM generic_questions t1, subjects t2 WHERE t1.ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM generic_questions) " +
                    "AND t2.SUBJECT='" + subject + "';";
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
     * Method that adds a relation between a question and a subject
     * by linking the question with questionID to the subject given as parameter
     *
     * @param subject,questionID
     * @throws Exception
     */
    static public void addRelationQuestionSubject(Integer questionID, String subject) {
        //first get the list of all subjects linked to the question (also check parents subjects)
        Vector<String> subjectsVector = DbTableSubject.getSubjectsForQuestionID(questionID);
        Vector<String> allSubjectsVector = new Vector<>();
        if (!allSubjectsVector.contains(subject)) {
            allSubjectsVector.add(subject);
        }
        for (int i = 0; i < subjectsVector.size(); i++) {
            if (!allSubjectsVector.contains(subjectsVector.get(i))) {
                allSubjectsVector.add(subjectsVector.get(i));
            }
            Vector<String> parentsSubjectsVector = DbTableSubject.getAllParentsSubjects(subjectsVector.get(i));
            for (int j = 0; j < parentsSubjectsVector.size(); j++) {
                if (!allSubjectsVector.contains(parentsSubjectsVector.get(j))) {
                    allSubjectsVector.add(parentsSubjectsVector.get(j));
                }
            }
        }

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            for (int i = 0; i < allSubjectsVector.size(); i++) {
                String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                        "WHERE ID_GLOBAL='" + questionID + "' " +
                        "AND ID_SUBJECT_GLOBAL= (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = '" + allSubjectsVector.get(i) + "');";
                ResultSet rs = stmt.executeQuery(query);
                Vector<String> queries = new Vector<>();
                while (rs.next()) {
                    queries.add(rs.getString("ID_GLOBAL"));
                }
                if (queries.size() == 0) {
                    String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT t1.ID_GLOBAL,t2.ID_SUBJECT_GLOBAL," +
                            "'1' FROM generic_questions t1, subjects t2 WHERE t1.ID_GLOBAL = '" + questionID + "' " +
                            "AND t2.SUBJECT='" + allSubjectsVector.get(i) + "';";
                    stmt.executeUpdate(sql);
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public Vector<String> getQuestionsIdsForSubject(String subject) {
        Vector<String> questionIDs = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                    "WHERE ID_SUBJECT_GLOBAL = (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = '" + subject + "');";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionIDs.add(rs.getString("ID_GLOBAL"));
            }

            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return questionIDs;
    }

    static public void removeRelationsWithQuestion(Integer questionID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_subject_relation WHERE ID_GLOBAL='" + questionID + "';";
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
