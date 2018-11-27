package koeko.database_management;

import koeko.view.Professor;
import koeko.view.Utilities;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by alainrichard on 25.04.18
 */
public class DbTableProfessor {
    static public void createTableProfessor(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS professor " +
                    "(ID_PROF    INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " PROF_MUID  TEXT, " +
                    " FIRSTNAME  TEXT, " +
                    " LASTNAME   TEXT    NOT NULL, " +
                    " ALIAS      TEXT    NOT NULL, " +
                    " SYNC_KEY  TEXT, " +
                    " LANGUAGE TEXT, " +
                    " MODIF_DATE TEXT, " +
                    " UNIQUE (ALIAS) ) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void addProfessor(String firstname, String lastname, String alias) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO professor (FIRSTNAME,LASTNAME, ALIAS) " +
                    "VALUES ('" + firstname + "','" + lastname + "','" + alias + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        setModifDate(alias);
    }
	
	
    static public void setProfessorMUID(String idProf, String muid) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"UPDATE professor SET PROF_MUID='" + muid +
                    "' WHERE ID_PROF=" + idProf + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        setModifDateWIthID(idProf);
    }

    static public void setProfessorAlias(String idProf, String alias) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"UPDATE professor SET ALIAS='" + alias +
                    "' WHERE ID_PROF=" + idProf + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        setModifDate(alias);
    }

    static public void setProfessorLanguage(String alias, String language) {
        String sql = 	"UPDATE professor SET LANGUAGE=? WHERE ALIAS=?;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            language = Utilities.languageToCodeMap.get(language);
            pstmt.setString(1, language);
            pstmt.setString(2, alias);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        setModifDate(alias);
    }

    static public void setProfessorSyncKey(String alias, String synchronizationKey) {
        String sql = 	"UPDATE professor SET SYNC_KEY=? WHERE ALIAS=?;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, synchronizationKey);
            pstmt.setString(2, alias);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        setModifDate(alias);
    }

    static public Professor getProfessor() {
        Professor professor = null;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT * FROM professor;";
            ResultSet rs = stmt.executeQuery(query);

            String alias = "";
            if (rs.next()) {
                String id = rs.getString("ID_PROF");
                alias = rs.getString("ALIAS");
                String muid = rs.getString("PROF_MUID");
                String language = rs.getString("LANGUAGE");
                String sync_key = rs.getString("SYNC_KEY");
                String modifDateString = rs.getString("MODIF_DATE");
                Timestamp modifTimestamp = Timestamp.valueOf(modifDateString);

                professor = Professor.createProfessor(id, alias, muid);
                professor.set_language(language);
                professor.set_synchronizationKey(sync_key);
                professor.set_timestamp(modifTimestamp);
            }
            stmt.close();
            c.commit();
            c.close();
            setProfessorSyncKey(alias, "");
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return professor;
    }

    static private void setModifDate(String alias) {
        String sql = 	"UPDATE professor SET MODIF_DATE=? WHERE ALIAS=?;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, Utilities.TimestampForNowAsString());
            pstmt.setString(2, alias);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static private void setModifDateWIthID(String idProf) {
        String sql = 	"UPDATE professor SET MODIF_DATE=? WHERE ID_PROF=?;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, Utilities.TimestampForNowAsString());
            pstmt.setString(2, idProf);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
