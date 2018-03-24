package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbTableRelationClassQuestion {
    static public void createTableRelationClassQuestion(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS class_question_relation " +
                    "(ID_CL_Q_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " CLASS_NAME      TEXT     NOT NULL, " +
                    " ID_GLOBAL      TEXT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (CLASS_NAME, ID_GLOBAL))  ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addClassQuestionRelation(String className, String questionID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO class_question_relation (CLASS_NAME, ID_GLOBAL) " +
                    "VALUES ('" +
                    className + "','" +
                    questionID +  "');";
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
