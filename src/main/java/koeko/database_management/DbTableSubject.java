package koeko.database_management;

import koeko.view.Subject;

import java.sql.*;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableSubject {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS subjects " +
                    "(ID_SUBJECT       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_SUBJECT_GLOBAL      INT     NOT NULL, " +
                    " SUBJECT           TEXT    NOT NULL, " +
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15)," +
                    " UNIQUE (SUBJECT)); ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addSubject(String subject) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO subjects (ID_SUBJECT_GLOBAL,SUBJECT) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    subject +"');";
            stmt.executeUpdate(sql);
            sql = "UPDATE subjects SET ID_SUBJECT_GLOBAL = 2000000 + ID_SUBJECT WHERE ID_SUBJECT = (SELECT MAX(ID_SUBJECT) FROM subjects);";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public Vector<String> getSubjectsForQuestionID(int questionID) {
        Vector<String> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT SUBJECT FROM subjects " +
                    "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                    "INNER JOIN generic_questions ON generic_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                    "WHERE generic_questions.ID_GLOBAL = '" + questionID + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return subjects;
    }
    static public Vector<String> getAllSubjects() {
        Vector<String> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT SUBJECT FROM subjects;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return subjects;
    }
    static public Vector<String> getSubjectsWithParent(String parentSubject) {
        Vector<String> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String query = "";
        if (parentSubject.contentEquals("")) {
            query = "SELECT SUBJECT FROM subjects " +
                    "WHERE ID_SUBJECT_GLOBAL NOT IN (SELECT ID_SUBJECT_GLOBAL_CHILD FROM subject_subject_relation);";
        } else {
            query = "SELECT SUBJECT FROM subjects " +
                    "INNER JOIN subject_subject_relation ON subjects.ID_SUBJECT_GLOBAL = subject_subject_relation.ID_SUBJECT_GLOBAL_CHILD " +
                    "WHERE subject_subject_relation.ID_SUBJECT_GLOBAL_PARENT = (select ID_SUBJECT_GLOBAL from subjects where SUBJECT='" + parentSubject + "');";
        }
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return subjects;
    }

    static public Vector<String> getSubjectsWithChild(String childSubject) {
        Vector<String> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String query = "SELECT SUBJECT FROM subjects " +
                "INNER JOIN subject_subject_relation ON subjects.ID_SUBJECT_GLOBAL = subject_subject_relation.ID_SUBJECT_GLOBAL_PARENT " +
                "WHERE subject_subject_relation.ID_SUBJECT_GLOBAL_CHILD = (select ID_SUBJECT_GLOBAL from subjects where SUBJECT='" + childSubject + "');";

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return subjects;
    }

    static public Boolean isSubject(String subject) {
        Vector<String> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String query = "SELECT SUBJECT FROM subjects WHERE SUBJECT='" + subject + "';";
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        if (subjects.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    static public Vector<String> getAllParentsSubjects (String subject) {
        Vector<String> allParentsSubjects = new Vector<>();
        recursiveGetParentsSubjects(subject,allParentsSubjects);

        return allParentsSubjects;
    }
    static private Vector<String> recursiveGetParentsSubjects (String subject, Vector<String> allParentsSubjects) {
        Vector<String> parentSubjects = getSubjectsWithChild(subject);
        for (int i = 0; i < parentSubjects.size(); i++) {
            if (!allParentsSubjects.contains(parentSubjects.get(i))) {
                allParentsSubjects.add(parentSubjects.get(i));
            }
            recursiveGetParentsSubjects(parentSubjects.get(i), allParentsSubjects);
        }
        return parentSubjects;
    }



    static public Vector<Subject> getSubjects() {
        Vector<Subject> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT * FROM subjects;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String sbjName = rs.getString("SUBJECT");
                int sbjId = rs.getInt("ID_SUBJECT");
                String sbjMUID = rs.getString("SBJ_MUID");
                Timestamp sbjUPD_DTS = rs.getTimestamp("SBJ_UPD_DTS");
                Subject sbj = new Subject(sbjName, sbjId, sbjMUID, sbjUPD_DTS);
                subjects.add(sbj);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return subjects;
    }


    static public void setSubjectMUID(int idSbj, String muid) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"UPDATE subjects SET SBJ_MUID='" + muid +
                    "' WHERE ID_SUBJECT=" + idSbj + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

}
