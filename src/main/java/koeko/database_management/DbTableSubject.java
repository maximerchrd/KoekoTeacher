package koeko.database_management;
import koeko.questions_management.SubjectTransferable;
import koeko.view.Subject;
import koeko.view.Utilities;

import javax.rmi.CORBA.Util;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            e.printStackTrace();
            System.exit(0);
        }
    }
    static public void addSubject(String subject) {
        String sql = 	"INSERT OR IGNORE INTO subjects (ID_SUBJECT_GLOBAL,SUBJECT,MODIF_DATE) " +
                "VALUES (?,?,?);";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, Utilities.localUniqueID());
            stmt.setString(2, subject);
            stmt.setString(3, Utilities.TimestampForNowAsString());
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }



    /**
     * method for checking the existence of a subject inside a table
     * @param sbj
     * @throws Exception
     */
    static public boolean checkIfExists(Subject sbj) throws Exception {
        // Check if the question exists already
        boolean bExists = true;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT  COUNT(1) FROM subjects WHERE IDENTIFIER = '" + sbj.get_subjectMUID() + "';";
            ResultSet result_query = stmt.executeQuery(sql);
            bExists = (Integer.parseInt(result_query.getString(1)) > 0);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return bExists;
    }


    static public void addIfNeededSubject(Subject sbj) throws Exception {
        if (checkIfExists(sbj))
            return;

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO subjects (ID_SUBJECT_GLOBAL,SUBJECT,IDENTIFIER) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    sbj.get_subjectName() + "','" + sbj.get_subjectMUID() + "');";
            stmt.executeUpdate(sql);
            sql = "UPDATE subjects SET MODIF_DATE='" + Utilities.TimestampForNow() + "', ID_SUBJECT_GLOBAL = 2000000 + ID_SUBJECT WHERE ID_SUBJECT = (SELECT MAX(ID_SUBJECT) FROM subjects);";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void updateSubject(String oldSubject, String newSubject) {
        String sql = 	"UPDATE subjects SET SUBJECT = ?, MODIF_DATE = ?  WHERE SUBJECT = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, newSubject);
            pstmt.setString(2, Utilities.TimestampForNowAsString());
            pstmt.setString(3, oldSubject);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static public String getSujectIdentifier(String subjectName) {
        String sql = 	"SELECT ID_SUBJECT_GLOBAL,IDENTIFIER FROM subjects WHERE SUBJECT = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, subjectName);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String identifier = rs.getString("IDENTIFIER");
                String intId = rs.getString("ID_SUBJECT_GLOBAL");
                if ( identifier != null && identifier.length() > 0) {
                    return identifier;
                } else {
                    return intId;
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    static public Vector<String> getSubjectsForQuestionID(String questionID) {
        Vector<String> subjects = new Vector<>();
        String query = "SELECT SUBJECT FROM subjects " +
                "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                "INNER JOIN generic_questions ON generic_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                "WHERE generic_questions.ID_GLOBAL = ?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, questionID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return subjects;
    }

    static public ArrayList<SubjectTransferable> getSubjectsObjectsForQuestionID(String questionID) {
        ArrayList<SubjectTransferable> subjects = new ArrayList<>();
        String query = "SELECT SUBJECT FROM subjects " +
                "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                "INNER JOIN generic_questions ON generic_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                "WHERE generic_questions.ID_GLOBAL = ?";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, questionID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SubjectTransferable subject = new SubjectTransferable();
                subject.set_subjectName(rs.getString("SUBJECT"));
                subject.setQuestionId(questionID);
                subjects.add(subject);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return subjects;
    }

    static public Vector<String> getAllSubjectsAsStrings() {
        Vector<String> subjects = new Vector<>();
        String query = "SELECT SUBJECT FROM subjects";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subjects.add(rs.getString("SUBJECT"));
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        return subjects;
    }
    static public Vector<Subject> getAllSubjects() {
        Vector<Subject> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT SUBJECT,ID_SUBJECT_GLOBAL FROM subjects;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjects.add(new Subject());
                subjects.get(subjects.size() - 1).set_subjectName(rs.getString("SUBJECT"));
                subjects.get(subjects.size() - 1).set_subjectMUID(rs.getString("ID_SUBJECT_GLOBAL"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
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


    // Get all subjects that must be synchronized to global collect
    static public Vector<Subject> getSubjects() {
        Vector<Subject> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Timestamp lastSyncTime = DbTableSettings.getLastSyncAsTimestamp();

            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT * FROM subjects";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Timestamp sbjUPD_DTS = DbUtils.getModifDateAsTimestamp(rs);
                if (sbjUPD_DTS.after(lastSyncTime)) {
                    String sbjName = rs.getString("SUBJECT");
                    int sbjId = rs.getInt("ID_SUBJECT");
                    String sbjMUID = rs.getString("IDENTIFIER");
                    Subject sbj = new Subject(sbjName, sbjId, sbjMUID, sbjUPD_DTS);
                    subjects.add(sbj);
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
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
            String sql = 	"UPDATE subjects SET IDENTIFIER='" + muid +
                    "' WHERE ID_SUBJECT=" + idSbj + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void deleteSubject(String subject) {
        String sql = 	"DELETE FROM subjects WHERE SUBJECT = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, subject);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
