package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationStudentObjective {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS student_objective_relation " +
                    "(ID_ST_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_STUDENT      INT     NOT NULL, " +
                    " ID_INDIV_OBJ_STUDENT_RES      INT     NOT NULL, " +
                    " QUANTITATIVE_EVAL      TEXT     NOT NULL, " +
                    " QUALITATIVE_EVAL      TEXT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
