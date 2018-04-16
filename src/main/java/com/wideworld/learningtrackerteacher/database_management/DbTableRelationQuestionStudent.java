package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionStudent {
    static public void createTableStudent(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_student_relation " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_STUDENT_GLOBAL      INT     NOT NULL, " +
                    " INSERT_DATE      TEXT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (ID_GLOBAL, ID_STUDENT_GLOBAL)) ";
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
     * @param student
     * @throws Exception
     */
    static public void addRelationQuestionStudent(Integer idGlobal, String student) throws Exception {
        //first get the list of all subjects linked to the question

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT OR IGNORE INTO question_student_relation (ID_GLOBAL, ID_STUDENT_GLOBAL, INSERT_DATE) VALUES ('" + idGlobal +
                    "', (SELECT ID_STUDENT_GLOBAL FROM students WHERE students.FIRST_NAME = '" + student + "'), DATETIME('now'));";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public Vector<String> getQuestionsIdsForStudent(String student) {
        Vector<String> questionIDs = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_GLOBAL, INSERT_DATE FROM question_student_relation " +
                    "WHERE ID_STUDENT_GLOBAL = (SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = '" + student + "');";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionIDs.add(rs.getString("ID_GLOBAL") + "|" + rs.getString("INSERT_DATE"));
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

    static public void removeRelationsQuestionStudent(Integer questionID, String student) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_student_relation WHERE ID_GLOBAL='" + questionID + "' AND ID_STUDENT_GLOBAL = (SELECT ID_STUDENT_GLOBAL FROM students WHERE FIRST_NAME = '" + student + "');";
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
