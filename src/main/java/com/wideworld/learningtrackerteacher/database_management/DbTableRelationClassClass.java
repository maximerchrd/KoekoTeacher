package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTableRelationClassClass {
    static public void createTableRelationClassClass(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS class_class_relation " +
                    "(ID_CL_CL_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " CLASS_NAME1      TEXT     NOT NULL, " +
                    " CLASS_NAME2      TEXT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (CLASS_NAME1, CLASS_NAME2))  ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addClassStudentRelation(String className, String groupName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO class_class_relation (CLASS_NAME1, CLASS_NAME2) " +
                    "VALUES ('" +
                    className + "','" +
                    groupName +  "');";
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
