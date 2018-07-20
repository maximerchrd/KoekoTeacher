package koeko.database_management;

import koeko.view.Subject;

import java.sql.*;
import java.util.Vector;

import static koeko.database_management.DbTableRelationQuestionSubject.getQuestionsIdsForSubject;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationSubjectSubject {
    static public void createTableRelationSubjectSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS subject_subject_relation " +
                    "(ID_SUBJ_SUBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_SUBJECT_GLOBAL_PARENT      INT     NOT NULL, " +
                    " ID_SUBJECT_GLOBAL_CHILD      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and a subject
     * by linking the last added question with the subject given as parameter
     * It also deletes the former relation, the former question-subject relations and
     * adds the new question-subject relations
     * @param subjectParent,subjectChild
     */
    static public void addRelationSubjectSubject(String subjectParent, String subjectChild, String oldSubjectParent) {
        //BEGIN remove the old question-subject relations
        Vector<String> childQuestions = new Vector<>();
        Vector<String> parentQuestions = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(oldSubjectParent);
        Vector<String> childSubjects = DbTableSubject.getSubjectsWithParent(oldSubjectParent);
        for (String childSubject : childSubjects) {
            if (!childSubject.contentEquals(subjectChild)) {
                Vector<String> partChildQuestions = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(childSubject);
                childQuestions.addAll(partChildQuestions);
            }
        }

        for (String id : childQuestions) {
            parentQuestions.remove(id);
        }

        for (String questionId : parentQuestions) {
            DbTableRelationQuestionSubject.removeRelationSubjectQuestion(oldSubjectParent, questionId);
        }
        //END remove the old question-subject relations

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        String sql = 	"DELETE FROM subject_subject_relation WHERE ID_SUBJECT_GLOBAL_CHILD = (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, subjectChild);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "INSERT OR IGNORE INTO subject_subject_relation (ID_SUBJECT_GLOBAL_PARENT, ID_SUBJECT_GLOBAL_CHILD) SELECT DISTINCT ID_SUBJECT_GLOBAL," +
                "(SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE  SUBJECT=?)" +
                "FROM subjects WHERE  SUBJECT=?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, subjectChild);
            pstmt.setString(2, subjectParent);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        Vector<String> questionIDs = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subjectChild);
        for (int i = 0; i < questionIDs.size(); i++) {
            DbTableRelationQuestionSubject.addRelationQuestionSubject(questionIDs.get(i),"");
        }
    }

    static public Vector<Vector<String>> getAllSubjectIDsRelations() {
        Vector<Vector<String>> subjectsPairs = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_SUBJECT_GLOBAL_PARENT,ID_SUBJECT_GLOBAL_CHILD FROM subject_subject_relation;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                subjectsPairs.add(new Vector<>());
                subjectsPairs.get(subjectsPairs.size() - 1).add(rs.getString("ID_SUBJECT_GLOBAL_PARENT"));
                subjectsPairs.get(subjectsPairs.size() - 1).add(rs.getString("ID_SUBJECT_GLOBAL_CHILD"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return subjectsPairs;
    }

    public static void removeRelationsForSubject(String subject) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM subject_subject_relation " +
                    "WHERE ID_SUBJECT_GLOBAL_PARENT=(SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT='" + subject + "');";
            stmt.executeUpdate(sql);
            sql = "DELETE FROM subject_subject_relation " +
                    "WHERE ID_SUBJECT_GLOBAL_CHILD=(SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT='" + subject + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void deleteRelationWhereSubjectIsChild(String subject) {
        String sql = "DELETE FROM subject_subject_relation WHERE ID_SUBJECT_GLOBAL_CHILD=(SELECT ID_SUBJECT_GLOBAL " +
                "FROM subjects WHERE SUBJECT=?)";
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
