package koeko.database_management;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationClassObjective {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS class_objective_relation " +
                    "(ID_ST_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_CLASS      INT     NOT NULL, " +
                    " ID_OBJECTIVE      INT     NOT NULL, " +
                    " QUANTITATIVE_EVAL      TEXT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
