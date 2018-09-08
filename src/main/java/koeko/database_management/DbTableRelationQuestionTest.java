package koeko.database_management;

import koeko.view.RelationQuestionTest;
import koeko.view.TestView;
import koeko.view.Utilities;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by maximerichard on 24.11.17.
 *
 * TO BE REMOVED. REPLACED BY QUESTION - QUESTION RELATIONS
 */
public class DbTableRelationQuestionTest {
    static public void createTableRelationQuestionTest(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_test_relation " +
                    "(ID_GLOBAL_TEST       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_TEST      INT, " +
                    " TEST_NAME      TEXT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public ArrayList<RelationQuestionTest> getRelationQuestionTest(String testId, String testName) {
        ArrayList<RelationQuestionTest> relationQuestionTests = new ArrayList<>();

        String sql = 	"SELECT * FROM question_test_relation WHERE ID_GLOBAL_TEST=?";
        try (Connection conn = Utilities.getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, testId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RelationQuestionTest relationQuestionTest = new RelationQuestionTest();
                relationQuestionTest.set_questionMUID(rs.getString("ID_GLOBAL"));
                relationQuestionTest.set_testMUID(rs.getString("ID_TEST"));
                relationQuestionTests.add(relationQuestionTest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (relationQuestionTests.size() == 0) {
            sql = 	"SELECT * FROM question_test_relation WHERE TEST_NAME=?";
            try (Connection conn = Utilities.getDbConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, testName);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    RelationQuestionTest relationQuestionTest = new RelationQuestionTest();
                    relationQuestionTest.set_questionMUID(rs.getString("ID_GLOBAL"));
                    relationQuestionTest.set_testMUID(rs.getString("ID_TEST"));
                    relationQuestionTests.add(relationQuestionTest);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return relationQuestionTests;
    }

    static public void setIdentifier(String name, String identifier) {
        String sql = "UPDATE question_test_relation SET ID_TEST = ? WHERE TEST_NAME = ?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, identifier);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void addRelationQuestionTest(String id_global, String testName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO question_test_relation (ID_GLOBAL,TEST_NAME)" +
                    "VALUES ('" +
                    id_global + "','" +
                    testName + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public ArrayList<String> getQuestionIdsFromTestName(String testName) {
        ArrayList<String> questionIds = new ArrayList<>();
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
                questionIds.add(rs.getString("ID_GLOBAL"));
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

    static public void removeQuestionFromTest(String testName, String idGlobal) {
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
