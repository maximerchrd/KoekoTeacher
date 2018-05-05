package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionQuestion {
    static public void createTableRelationQuestionQuestion(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_question_relation " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL_1      TEXT     NOT NULL, " +
                    " ID_GLOBAL_2      TEXT     NOT NULL, " +
                    " TEST      TEXT     NOT NULL, " +
                    " CONDITION      TEXT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (ID_GLOBAL_1, ID_GLOBAL_2, TEST)) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addRelationQuestionQuestion(String idGlobal1, String idGlobal2, String test, String condition) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO question_question_relation (ID_GLOBAL_1, ID_GLOBAL_2, TEST, CONDITION) " +
                    "VALUES ('" + idGlobal1 + "','" + idGlobal2 + "','" + test + "','" + condition + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void removeRelationsWithQuestion(String questionID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_question_relation WHERE ID_GLOBAL1='" + questionID + "';";
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
