package com.wideworld.learningtrackerteacher.database_management;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableQuestionIntermediateAnswers {
    static public void createTableQuestionIntermediateAnswers(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS intermediate_answers_question " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " QUESTION           TEXT    NOT NULL, " +
                    " OPTION0           TEXT    NOT NULL, " +
                    " OPTION1           TEXT    NOT NULL, " +
                    " OPTION2           TEXT    NOT NULL, " +
                    " OPTION3           TEXT    NOT NULL, " +
                    " OPTION4           TEXT    NOT NULL, " +
                    " OPTION5           TEXT    NOT NULL, " +
                    " OPTION6           TEXT    NOT NULL, " +
                    " OPTION7           TEXT    NOT NULL, " +
                    " OPTION8           TEXT    NOT NULL, " +
                    " OPTION9           TEXT    NOT NULL, " +
                    " INTERMEDIATE_ANSWERS           TEXT    NOT NULL, " +
                    " IMAGE_PATH                     TEXT    NOT NULL, " +
                    " LEVEL_COGNITIVE_ABILITY        INT    NOT NULL, " +
                    " ID_GLOBAL                      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
