package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addAnswerOption(String questionID, String option) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO answer_options (ID_ANSWEROPTION_GLOBAL,OPTION) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    option +"');";
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected > 0) {
                sql = "UPDATE answer_options SET ID_ANSWEROPTION_GLOBAL = ID_ANSWEROPTION_GLOBAL + ID_ANSWEROPTION WHERE ID_ANSWEROPTION = (SELECT MAX(ID_ANSWEROPTION) FROM answer_options)";
                stmt.executeUpdate(sql);
                System.out.println(option + " added");
            } else {
                System.out.println(option + " not added");
            }
            stmt.close();
            c.commit();
            c.close();
            DbTableRelationQuestionAnserOption.addRelationQuestionAnserOption(questionID, option);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void removeOptionsRelationsQuestion(String questionID) throws Exception {
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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
