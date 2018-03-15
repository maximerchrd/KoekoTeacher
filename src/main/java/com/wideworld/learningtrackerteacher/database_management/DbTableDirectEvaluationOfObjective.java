package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableDirectEvaluationOfObjective {
    static public void createTableDirectEvaluationOfObjective(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS direct_evaluation_of_objective " +
                    "(ID_DIRECT_EVAL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL           INT    NOT NULL, " +
                    " EVALUATION           TEXT    NOT NULL, " +
                    " LEVEL_COGNITIVE_ABILITY                      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
