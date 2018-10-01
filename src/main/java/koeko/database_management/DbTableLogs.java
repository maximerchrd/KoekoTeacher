package koeko.database_management;

import koeko.Koeko;
import koeko.students_management.Student;
import koeko.view.Log;
import koeko.view.Utilities;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableLogs {
    static private String sessionId = "";
    static private String uid = "";
    static public void createTableClasses(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS logs " +
                    "(ID_LOG       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " UID TEXT, " +
                    " SESSION_ID TEXT, " +
                    " INSERT_TIME TEXT, " +
                    " SYNCED INT, " +
                    " LOG_TAG TEXT, " +
                    " LOG_MESSAGE      TEXT) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        if (Koeko.recordLogs) {
            ArrayList<Log> log = getLogs(1);
            if (log.size() > 0) {
                uid = log.get(0).getUid();
            } else {
                uid = Utilities.localUniqueID();
                insertLog("SPECS", System.getProperty("os.name") + "/" + System.getProperty("os.version"));
            }
        }
    }

    static public void insertLog(String tag, String message) {
        if (sessionId.contentEquals("")) {
            sessionId = Utilities.localUniqueID();
        }

        String query = "INSERT INTO logs (UID, SESSION_ID, LOG_TAG, LOG_MESSAGE, SYNCED, INSERT_TIME) VALUES (?,?,?,?,0,?)";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, uid);
            stmt.setString(2, sessionId);
            stmt.setString(3, tag);
            stmt.setString(4, message);
            stmt.setString(5, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public ArrayList<Log> getLogs(Integer checkIfTableExists) {
        ArrayList<Log> logs = new ArrayList<>();
        String query = "";
        if (checkIfTableExists == 1) {
            query = "SELECT * FROM logs LIMIT 1";
        } else {
            query = "SELECT * FROM logs WHERE SYNCED=0";
        }

        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Log log = new Log();
                log.setId(rs.getString("ID_LOG"));
                log.setUid(rs.getString("UID"));
                log.setSessionId("SESSION_ID");
                log.setLogTag("LOG_TAG");
                log.setLogMessage("LOG_MESSAGE");
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }
}
