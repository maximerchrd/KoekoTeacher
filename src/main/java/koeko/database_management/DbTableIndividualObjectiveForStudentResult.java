package koeko.database_management;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableIndividualObjectiveForStudentResult {
    static public void createTableDirectEvaluationOfObjective(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS individual_objective_for_student_result " +
                    "(ID_INDIV_OBJ_STUDENT_RES        INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_OBJECTIVE             INT    NOT NULL, " +
                    " ID_STUDENT_GLOBAL     INT    NOT NULL, " +
                    " QUANTITATIVE_EVAL     TEXT    NOT NULL, " +
                    " QUALTATIVE_EVAL       TEXT    NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
