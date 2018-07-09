package koeko.database_management;

import koeko.view.Professor;

import java.sql.*;

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
                    " LANGUAGE TEXT, " +
                    " UNIQUE (ALIAS) ) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void setProfessorLanguage(String alias, String language) {
        String sql = 	"UPDATE professor SET LANGUAGE=? WHERE ALIAS=?;";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, language);
            pstmt.setString(2, alias);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
            if (rs.next()) {
                String id = rs.getString("ID_PROF");
                String alias = rs.getString("ALIAS");
                String muid = rs.getString("PROF_MUID");
                String language = rs.getString("LANGUAGE");

                professor = Professor.createProfessor(id, alias, muid);
                professor.set_language(language);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return professor;
    }
}
