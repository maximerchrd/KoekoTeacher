package koeko.database_management;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationClassTest {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS class_test_relation " +
                    "(ID_CLASS_TEST       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_CLASS      INT     NOT NULL, " +
                    " ID_TEST      INT     NOT NULL, " +
                    " QUANTITATIVE_EVAL      TEXT, " +
                    " CONSTRAINT unq UNIQUE (ID_CLASS, ID_TEST)) ";;
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void addClassTestRelation(String selectedClass, String test) {
        String sql = 	"INSERT OR REPLACE INTO class_test_relation (ID_CLASS, ID_TEST) VALUES( (SELECT ID_CLASS_GLOBAL FROM classes WHERE NAME = ?), " +
                "(SELECT ID_TEST_GLOBAL FROM test WHERE NAME = ?))";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, selectedClass);
            pstmt.setString(2, test);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static public ArrayList<String> getTestsForClass(String selectedClass) {
        ArrayList<String> tests = new ArrayList<>();
        String sql = 	"SELECT NAME FROM test " +
                " INNER JOIN class_test_relation ON test.ID_TEST_GLOBAL = class_test_relation.ID_TEST " +
                " WHERE class_test_relation.ID_CLASS = (SELECT ID_CLASS_GLOBAL FROM classes WHERE NAME=?)";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, selectedClass);

            // set the corresponding param
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tests.add(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return tests;
    }
}
