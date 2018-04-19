package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.Test;
import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableTests {
    static public void createTableTest(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS tests " +
                    "(ID_TEST       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_TEST_GLOBAL      INT     NOT NULL, " +
                    " NAME      TEXT     NOT NULL, " +
                    " QUANTITATIVE_EVAL           TEXT    NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public ArrayList<Test> getAllTests() {
        ArrayList<Test> tests = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_TEST_GLOBAL,NAME FROM tests;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Test newTest = new Test();
                newTest.setTestName(rs.getString("NAME"));
                newTest.setIdTest(Integer.parseInt(rs.getString("ID_TEST_GLOBAL")));
                ArrayList<Integer> newQuestionIDsList = DbTableRelationQuestionTest.getQuestionIdsFromTestName(String.valueOf(newTest.getIdTest()));
                ArrayList<QuestionGeneric> questionGenericArrayList = new ArrayList<>();
                for (int i = 0; i < newQuestionIDsList.size(); i++) {
                    QuestionGeneric newQuestionGeneric = new QuestionGeneric();
                    newQuestionGeneric.setGlobalID(newQuestionIDsList.get(i));
                    newQuestionGeneric.setIntTypeOfQuestion(DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(newQuestionIDsList.get(i))));
                    questionGenericArrayList.add(newQuestionGeneric);
                }
                newTest.setIdsQuestions(newQuestionIDsList);
                newTest.setGenericQuestions(questionGenericArrayList);
                tests.add(newTest);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return tests;
    }
    static public Test getLastTests() {
        Test newTest = null;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_TEST_GLOBAL,NAME FROM tests WHERE ID_TEST = (SELECT MAX(ID_TEST) FROM tests);";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                newTest = new Test();
                newTest.setTestName(rs.getString("NAME"));
                newTest.setIdTest(Integer.parseInt(rs.getString("ID_TEST_GLOBAL")));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return newTest;
    }
    static public Test getTestWithID(Integer testID) {
        Test newTest = new Test();
        if (testID < 0) {
            testID = -testID;
        }
        newTest.setIdTest(testID);
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT NAME FROM tests WHERE ID_TEST_GLOBAL = '" + testID + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                newTest = new Test();
                newTest.setTestName(rs.getString("NAME"));
                newTest.setIdTest(testID);
            }
            stmt.close();
            c.commit();
            c.close();

            //get the objectives and insert them inside the test object
            newTest.setObjectives(DbTableRelationObjectiveTest.getObjectivesFromTestName(newTest.getTestName()));
            newTest.setObjectivesIDs(DbTableRelationObjectiveTest.getObjectivesIDsFromTestName(newTest.getTestName()));
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return newTest;
    }
    static public Integer addTest(String name) {
        Integer testID = 0;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO tests (ID_TEST_GLOBAL,NAME,QUANTITATIVE_EVAL)" +
                    "VALUES ('" +
                    2000000 + "','" +
                    name + "','-1');";
            stmt.executeUpdate(sql);
            sql = "UPDATE tests SET ID_TEST_GLOBAL = 2000000 + ID_TEST WHERE ID_TEST = (SELECT MAX(ID_TEST) FROM tests)";
            stmt.executeUpdate(sql);
            sql = "SELECT ID_TEST_GLOBAL FROM tests WHERE ID_TEST = (SELECT MAX(ID_TEST) FROM tests);";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                testID = rs.getInt("ID_TEST_GLOBAL");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return testID;
    }
    static public void removeTestWithID(String ID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM tests WHERE ID_TEST_GLOBAL = '" + ID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void removeTestWithName(String testName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM tests WHERE NAME = '" + testName + "';";
            stmt.executeUpdate(sql);
            sql = "DELETE FROM question_test_relation WHERE TEST_NAME='" + testName + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void renameTest(int global_id, String newName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE tests SET NAME = '" + newName + "' " +
                    "WHERE ID_TEST_GLOBAL = '" + global_id + "';";
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
