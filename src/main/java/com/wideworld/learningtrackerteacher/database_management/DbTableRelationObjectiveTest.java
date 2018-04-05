package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationObjectiveTest {
    static public void createTableRelationObjectiveTest(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS objective_test_relation " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL_OBJECTIVE      INT     NOT NULL, " +
                    " ID_GLOBAL_TEST      INT     NOT NULL," +
                    "CONSTRAINT unq UNIQUE (ID_GLOBAL_OBJECTIVE, ID_GLOBAL_TEST)) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addRelationObjectiveTest(String objectiveName, String testName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String objectiveID = "";
            String testID = "";
            String query = "SELECT ID_OBJECTIVE_GLOBAL FROM learning_objectives WHERE OBJECTIVE = '" + objectiveName + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                objectiveID = rs.getString("ID_OBJECTIVE_GLOBAL");
            }
            query = "SELECT ID_TEST_GLOBAL FROM tests WHERE NAME = '" + testName + "';";
            ResultSet rs2 = stmt.executeQuery(query);
            while (rs2.next()) {
                testID = rs2.getString("ID_TEST_GLOBAL");
            }
            String sql = "INSERT INTO objective_test_relation (ID_GLOBAL_OBJECTIVE,ID_GLOBAL_TEST)" +
                    "VALUES ('" +
                    objectiveID + "','" +
                    testID + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public ArrayList<Integer> getQuestionIdsFromTestName(String testName) {
        ArrayList<Integer> questionIds = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_GLOBAL FROM question_test_relation WHERE TEST_NAME='" + testName + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionIds.add(Integer.parseInt(rs.getString("ID_GLOBAL")));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return questionIds;
    }

    static public void removeQuestionFromTest(String testName, Integer idGlobal) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_test_relation WHERE TEST_NAME='" + testName + "' AND ID_GLOBAL='" + idGlobal + "';";
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
