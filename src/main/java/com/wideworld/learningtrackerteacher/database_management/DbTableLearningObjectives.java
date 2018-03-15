package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableLearningObjectives {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS learning_objectives " +
                    "(ID_OBJECTIVE       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_OBJECTIVE_GLOBAL      INT     NOT NULL, " +
                    " OBJECTIVE      TEXT     NOT NULL, " +
                    " LEVEL_COGNITIVE_ABILITY      INT     NOT NULL, " +
                    " UNIQUE (OBJECTIVE) ); ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addObjective(String objective, int level_cognitive_ability) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO learning_objectives (ID_OBJECTIVE_GLOBAL,OBJECTIVE,LEVEL_COGNITIVE_ABILITY) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    objective + "','" +
                    level_cognitive_ability +"');";
            stmt.executeUpdate(sql);
            sql = "UPDATE learning_objectives SET ID_OBJECTIVE_GLOBAL = 2000000 + ID_OBJECTIVE WHERE ID_OBJECTIVE = (SELECT MAX(ID_OBJECTIVE) FROM learning_objectives)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public Vector<String> getObjectiveForQuestionID(int questionID) {
        Vector<String> objectives = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT OBJECTIVE FROM learning_objectives " +
                    "INNER JOIN question_objective_relation ON learning_objectives.ID_OBJECTIVE_GLOBAL = question_objective_relation.ID_OBJECTIVE_GLOBAL " +
                    "INNER JOIN generic_questions ON generic_questions.ID_GLOBAL = question_objective_relation.ID_GLOBAL " +
                    "WHERE generic_questions.ID_GLOBAL = '" + questionID + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                objectives.add(rs.getString("OBJECTIVE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return objectives;
    }

    static public Vector<String> getAllObjectives() {
        Vector<String> objectives = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT OBJECTIVE FROM learning_objectives;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                objectives.add(rs.getString("OBJECTIVE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return objectives;
    }
}
