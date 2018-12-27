package koeko.database_management;

import koeko.view.Utilities;

import java.sql.*;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableAnswerOptions {
    static public void createTableAnswerOptions(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS answer_options " +
                    "(ID_ANSWEROPTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_ANSWEROPTION_GLOBAL      INT     NOT NULL, " +
                    " OPTION           TEXT    NOT NULL, " +
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15)," +
                    " UNIQUE ( OPTION ));";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void addAnswerOption(String questionID, String option) {
        String sql = 	"INSERT OR IGNORE INTO answer_options (ID_ANSWEROPTION_GLOBAL,OPTION) " +
                "VALUES (?,?);";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, Utilities.TimestampForNowAsString());
            pstmt.setString(2, option);
            // update
            pstmt.executeUpdate();

            DbTableRelationQuestionAnserOption.addRelationQuestionAnserOption(questionID, option);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void removeOptionsRelationsQuestion(String questionID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"DELETE FROM question_answeroption_relation WHERE ID_GLOBAL='" + questionID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
