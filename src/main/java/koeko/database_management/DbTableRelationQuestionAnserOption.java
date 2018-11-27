package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionAnserOption {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_answeroption_relation " +
                    "(ID_QUEST_ANSOPTION_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_ANSWEROPTION_GLOBAL      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and an answer option
     * @param questionID, option
     * @throws Exception
     */
    static public void addRelationQuestionAnserOption(String questionID, String option) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO question_answeroption_relation (ID_GLOBAL, ID_ANSWEROPTION_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_ANSWEROPTION_GLOBAL FROM short_answer_questions t1, answer_options t2 " +
                    "WHERE t1.ID_GLOBAL = '"+ questionID + "' " +
                    "AND t2.OPTION='" + option + "';";
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
