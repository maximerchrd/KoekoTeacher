package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;
import com.wideworld.learningtrackerteacher.questions_management.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableSettings {
    static public void createTableSettings(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS settings " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " NEARBY_MODE      INT    NOT NULL, " +
                    " TEACHER_NAME      TEXT     NOT NULL) ";
            statement.executeUpdate(sql);
            sql = "INSERT INTO settings (NEARBY_MODE, TEACHER_NAME)" +
                    "VALUES ('" +
                    0 + "','" +
                    "No Name" + "');";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public Integer getNearbyMode() {
        Integer nearbyMode = -1;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT NEARBY_MODE FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                nearbyMode = Integer.parseInt(rs.getString("NEARBY_MODE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return nearbyMode;
    }

    static public String getTeacherName() {
        String teacherName = "";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT TEACHER_NAME FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                teacherName = rs.getString("TEACHER_NAME");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return teacherName;
    }

    static public void insertNearbyMode(Integer nearbyMode) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET NEARBY_MODE = " + nearbyMode +" WHERE ID = 1";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void insertTeacherName(String name) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET TEACHER_NAME = " + name +" WHERE ID = 1";
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
