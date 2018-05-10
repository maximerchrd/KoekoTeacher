package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionObjective {
    static public void createTableRelationQuestionObjective(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_objective_relation " +
                    "(ID_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_OBJECTIVE_GLOBAL      INT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (ID_GLOBAL, ID_OBJECTIVE_GLOBAL)) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addRelationQuestionObjective(String objective) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO question_objective_relation (ID_GLOBAL, ID_OBJECTIVE_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_OBJECTIVE_GLOBAL FROM generic_questions t1, learning_objectives t2 " +
                    "WHERE t1.ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM generic_questions) " +
                    "AND t2.OBJECTIVE='" + objective + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addRelationQuestionObjective(Integer questionID, String objective) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO question_objective_relation (ID_GLOBAL, ID_OBJECTIVE_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_OBJECTIVE_GLOBAL FROM generic_questions t1, learning_objectives t2 " +
                    "WHERE t1.ID_GLOBAL = '" + questionID + "' " +
                    "AND t2.OBJECTIVE='" + objective + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void removeRelationsWithQuestion(Integer questionID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_objective_relation WHERE ID_GLOBAL='" + questionID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
}
