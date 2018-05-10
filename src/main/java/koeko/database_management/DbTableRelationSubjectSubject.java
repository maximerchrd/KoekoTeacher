package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Vector;

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
     * @param subjectParent,subjectChild
     * @throws Exception
     */
    static public void addRelationSubjectSubject(String subjectParent, String subjectChild) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT OR IGNORE INTO subject_subject_relation (ID_SUBJECT_GLOBAL_PARENT, ID_SUBJECT_GLOBAL_CHILD) SELECT DISTINCT ID_SUBJECT_GLOBAL," +
                    "(SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE  SUBJECT='" + subjectChild + "')" +
                    "FROM subjects WHERE  SUBJECT='" + subjectParent + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        Vector<String> questionIDs = DbTableRelationQuestionSubject.getQuestionsIdsForSubject(subjectChild);
        for (int i = 0; i < questionIDs.size(); i++) {
            DbTableRelationQuestionSubject.addRelationQuestionSubject(Integer.valueOf(questionIDs.get(i)),"");
        }
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
}
