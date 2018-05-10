package koeko.database_management;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationClassTest {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS class_test_relation " +
                    "(ID_CLASS_TEST       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_CLASS      INT     NOT NULL, " +
                    " ID_TEST      INT     NOT NULL, " +
                    " QUANTITATIVE_EVAL      TEXT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
