package koeko.database_management;

import koeko.view.Professor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by alainrichard on 25.04.18
 */
public class DbTableProfessor {
    static public void createTableProfessor(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS professor " +
                    "(ID_PROF       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " PROF_MUID    TEXT, " +
                    " FIRSTNAME      TEXT     NOT NULL, " +
                    " LASTNAME      TEXT, " +
                    " UNIQUE (LASTNAME) ) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addProfessor(String firstname, String lastname) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO professor (FIRSTNAME,LASTNAME) " +
                    "VALUES ('" + firstname + "','" + lastname + "');";
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
        stmt = null;
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
                String fname = rs.getString("FIRSTNAME");
                String lname = rs.getString("LASTNAME");
                String muid = rs.getString("PROF_MUID");

                professor = Professor.createProfessor(id, fname, muid);
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
