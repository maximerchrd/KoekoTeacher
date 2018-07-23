package koeko.database_management;

import koeko.view.RelationQuestionSubject;

import java.sql.*;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionSubject {
    static public void createTableSubject(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_subject_relation " +
                    "(ID_SUBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_SUBJECT_GLOBAL      INT     NOT NULL, " +
                    " SUBJECT_LEVEL      INT NOT NULL) ";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and a subject
     * by linking the last added question with the subject given as parameter
     *
     * @param subject
     * @throws Exception
     */
    static public void addRelationQuestionSubject(String subject) throws Exception {
        //first get the list of all subjects linked to the question

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT t1.ID_GLOBAL,t2.ID_SUBJECT_GLOBAL," +
                    "'1' FROM generic_questions t1, subjects t2 WHERE t1.ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM generic_questions) " +
                    "AND t2.SUBJECT='" + subject + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }


    /**
     * method to check if it is neede to insert a relation question/subject
     * @param rqs
     * @throws Exception
     */
    static public boolean checkIfExists(RelationQuestionSubject rqs) throws Exception {
        // Check if the relation question/subject exists already
        boolean bExists = true;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT  COUNT(1) FROM question_subject_relation qsr " +
                    "JOIN subjects sbj ON sbj.ID_SUBJECT_GLOBAL = qsr.ID_SUBJECT_GLOBAL " +
                    "JOIN multiple_choice_questions mcq ON mcq.ID_GLOBAL=qsr.ID_GLOBAL " +
                    "WHERE mcq.IDENTIFIER='" + rqs.get_questionMUID() + "'and sbj.IDENTIFIER='" + rqs.get_subjectMUID() + "';";
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

    /**
     * method to get the id global of the subject base on the IDENTIFIER
     * @param rqs
     * @throws Exception
     */
    static public int getSubjectId(RelationQuestionSubject rqs) throws Exception {
        // Check if the relation question/subject exists already
        int sbj_id = 0;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT  ID_SUBJECT_GLOBAL FROM subjects " +
                    "WHERE IDENTIFIER='" + rqs.get_subjectMUID() + "';";
            ResultSet result_query = stmt.executeQuery(sql);
            sbj_id = Integer.parseInt(result_query.getString(1));
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return sbj_id;
    }

    /**
     * method to get the id global of the subject base on the IDENTIFIER
     * @param rqs
     * @throws Exception
     */
    static public int getQUestionId(RelationQuestionSubject rqs) throws Exception {
        // Check if the relation question/subject exists already
        int rec_id = 0;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT  ID_GLOBAL FROM multiple_choice_questions " +
                    "WHERE IDENTIFIER='" + rqs.get_questionMUID() + "';";
            ResultSet result_query = stmt.executeQuery(sql);
            rec_id = Integer.parseInt(result_query.getString(1));
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return rec_id;
    }

    /**
     * Method that adds a relation between a question and a subject
     * by linking the last added question with the subject given as parameter
     *
     * @param rqs
     * @throws Exception
     */
    static public void addIfNeededRelationQuestionSubject(RelationQuestionSubject rqs) throws Exception {
        // Check if the question exists already
        if (checkIfExists(rqs))
            return;

        int sbj_id = getSubjectId(rqs);
        int mcq_id = getQUestionId(rqs);
        // insert the relation
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) VALUES (" +
                    mcq_id + "," + sbj_id + "," + rqs.get_level() + ");";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }


    /**
     * Method that adds a relation between a question and a subject
     * by linking the question with questionID to the subject given as parameter
     *
     * @param subject,questionID
     * @throws Exception
     */
    static public void addRelationQuestionSubject(String questionID, String subject) {
        //first get the list of all subjects linked to the question (also check parents subjects)
        Vector<String> subjectsVector = DbTableSubject.getSubjectsForQuestionID(questionID);
        Vector<String> allSubjectsVector = new Vector<>();
        if (!allSubjectsVector.contains(subject)) {
            allSubjectsVector.add(subject);
        }
        for (int i = 0; i < subjectsVector.size(); i++) {
            if (!allSubjectsVector.contains(subjectsVector.get(i))) {
                allSubjectsVector.add(subjectsVector.get(i));
            }
            Vector<String> parentsSubjectsVector = DbTableSubject.getAllParentsSubjects(subjectsVector.get(i));
            for (int j = 0; j < parentsSubjectsVector.size(); j++) {
                if (!allSubjectsVector.contains(parentsSubjectsVector.get(j))) {
                    allSubjectsVector.add(parentsSubjectsVector.get(j));
                }
            }
        }

        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            for (int i = 0; i < allSubjectsVector.size(); i++) {
                String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                        "WHERE ID_GLOBAL='" + questionID + "' " +
                        "AND ID_SUBJECT_GLOBAL= (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = '" + allSubjectsVector.get(i) + "');";
                ResultSet rs = stmt.executeQuery(query);
                Vector<String> queries = new Vector<>();
                while (rs.next()) {
                    queries.add(rs.getString("ID_GLOBAL"));
                }
                if (queries.size() == 0) {
                    String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT t1.ID_GLOBAL,t2.ID_SUBJECT_GLOBAL," +
                            "'1' FROM generic_questions t1, subjects t2 WHERE t1.ID_GLOBAL = '" + questionID + "' " +
                            "AND t2.SUBJECT='" + allSubjectsVector.get(i) + "';";
                    stmt.executeUpdate(sql);
                }
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public Vector<String> getQuestionsIdsForSubject(String subject) {
        Vector<String> questionIDs = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();

            String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                    "WHERE ID_SUBJECT_GLOBAL = (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = '" + subject + "');";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionIDs.add(rs.getString("ID_GLOBAL"));
            }

            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return questionIDs;
    }

    static public void removeRelationsWithQuestion(String questionID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_subject_relation WHERE ID_GLOBAL='" + questionID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public void removeRelationSubjectQuestion(String subjectname, String questionID) {
        Connection c = null;
        PreparedStatement stmt = null;
        stmt = null;
        String subjectId = DbTableSubject.getSujectIdentifier(subjectname);
        String sql = "DELETE FROM question_subject_relation WHERE ID_GLOBAL=? AND " +
                "ID_SUBJECT_GLOBAL = ? ";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, questionID);
            pstmt.setString(2, subjectId);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    static public Vector<RelationQuestionSubject> getSubjectsForQuestion(String questionId) {
        Vector<RelationQuestionSubject> subjects = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT subjects.IDENTIFIER, SUBJECT_LEVEL FROM subjects " +
                    "INNER JOIN question_subject_relation on subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                    "INNER JOIN multiple_choice_questions ON multiple_choice_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                    "where ID_QUESTION=" + questionId + ";";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String sbjMUID = rs.getString("IDENTIFIER");
                int level = rs.getInt("SUBJECT_LEVEL");
                RelationQuestionSubject rqs = new RelationQuestionSubject(questionId, sbjMUID, level);
                subjects.add(rqs);
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

    static public void removeRelationWithSubject(String subject) {
        String sql = 	"DELETE FROM question_subject_relation WHERE ID_SUBJECT_GLOBAL=(SELECT ID_SUBJECT_GLOBAL " +
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
