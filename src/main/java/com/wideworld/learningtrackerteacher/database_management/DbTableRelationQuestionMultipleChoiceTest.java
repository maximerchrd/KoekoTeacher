package com.wideworld.learningtrackerteacher.database_management;

import com.wideworld.learningtrackerteacher.questions_management.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionMultipleChoiceTest {
    static public void createTableRelationQuestionTest(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_test_relation " +
                    "(ID_GLOBAL_TEST       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_TEST      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addRelationQuestionTest(String id_global, String id_test) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO question_test_relation (ID_GLOBAL,ID_TEST)" +
                    "VALUES ('" +
                    id_global + "','" +
                    id_test + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public ArrayList<Integer> getQuestionIdsFromTestId(String testId) {
        ArrayList<Integer> questionIds = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_GLOBAL FROM question_test_relation WHERE ID_TEST='" + testId + "';";
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
}
