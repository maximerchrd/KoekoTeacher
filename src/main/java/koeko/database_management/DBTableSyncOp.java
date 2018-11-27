package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBTableSyncOp {

    static public void createTableSyncOp(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS syncop " +
                    "(LAST_TS  TEXT) ";
            statement.executeUpdate(sql);

            Statement insertStmt = connection.createStatement();
            sql = "INSERT INTO syncop VALUES ('2018-01-01 01:01:01.000000000') ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public String GetLastSyncOp() {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String lastTS = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT * FROM syncop;";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                lastTS = rs.getString("LAST_TS");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return lastTS;
    }


    static public void SetLastSyncOp(String timestamp) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "UPDATE syncop SET LAST_TS='" + timestamp + "';";
            stmt.executeUpdate(query);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
