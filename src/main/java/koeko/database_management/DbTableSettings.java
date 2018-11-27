package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableSettings {
    static public void createTableSettings(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS settings " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " NEARBY_MODE      INT    NOT NULL, " +
                    " CORRECTION_MODE      INT    NOT NULL, " +
                    " FORCE_SYNC      INT    NOT NULL, " +
                    " UI_MODE      INT NOT NULL," +
                    " TEACHER_NAME      TEXT     NOT NULL) ";
            statement.executeUpdate(sql);
            sql = "INSERT INTO settings (NEARBY_MODE, CORRECTION_MODE, FORCE_SYNC, UI_MODE, TEACHER_NAME)" +
                    "VALUES ('" +
                    0 + "','" +
                    0 + "','" +
                    0 + "','" +
                    0 + "','" +
                    "No Name" + "');";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public Integer getNearbyMode() {
        Integer nearbyMode = -1;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT NEARBY_MODE FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                nearbyMode = Integer.parseInt(rs.getString("NEARBY_MODE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return nearbyMode;
    }

    static public Integer getCorrectionMode() {
        Integer correctionMode = -1;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT CORRECTION_MODE FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                correctionMode = Integer.parseInt(rs.getString("CORRECTION_MODE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return correctionMode;
    }

    static public Integer getForceSync() {
        Integer forceSync = -1;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT FORCE_SYNC FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                forceSync = Integer.parseInt(rs.getString("FORCE_SYNC"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return forceSync;
    }

    static public Integer getUIMode() {
        Integer UiMode = -1;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT UI_MODE FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                UiMode = Integer.parseInt(rs.getString("UI_MODE"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return UiMode;
    }

    static public String getTeacherName() {
        String teacherName = "";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT TEACHER_NAME FROM settings WHERE ID = 1;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                teacherName = rs.getString("TEACHER_NAME");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return teacherName;
    }

    static public void insertNearbyMode(Integer nearbyMode) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET NEARBY_MODE = " + nearbyMode +" WHERE ID = 1";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void insertUIMode(Integer UIMode) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET UI_MODE = " + UIMode +" WHERE ID = 1";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void insertCorrectionMode(Integer correctionMode) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET CORRECTION_MODE = " + correctionMode +" WHERE ID = 1";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void insertForceSync(Integer forceSync) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET FORCE_SYNC = " + forceSync +" WHERE ID = 1";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void insertTeacherName(String name) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE settings SET TEACHER_NAME = " + name +" WHERE ID = 1";
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
