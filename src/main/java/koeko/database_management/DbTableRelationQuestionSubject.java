package koeko.database_management;

import koeko.view.RelationQuestionSubject;
import koeko.view.Utilities;

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
            e.printStackTrace();
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
    static public void addRelationQuestionSubject(String subject) {
        String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT t1.ID_GLOBAL,t2.ID_SUBJECT_GLOBAL," +
                "'1' FROM generic_questions t1, subjects t2 WHERE t1.ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM generic_questions) " +
                "AND t2.SUBJECT=?;";
        try ( Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, subject);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }


    /**
     * method to check if it is needed to insert a relation question/subject
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
            c = Utilities.getDbConnection();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT  COUNT(1) FROM question_subject_relation qsr " +
                        "JOIN subjects sbj ON sbj.ID_SUBJECT_GLOBAL = qsr.ID_SUBJECT_GLOBAL " +
                        "JOIN multiple_choice_questions mcq ON mcq.ID_GLOBAL=qsr.ID_GLOBAL " +
                        "WHERE mcq.IDENTIFIER='" + rqs.get_questionMUID() + "'and sbj.IDENTIFIER='" + rqs.get_subjectMUID() + "';";
            ResultSet result_query = stmt.executeQuery(sql);
            bExists = (Integer.parseInt(result_query.getString(1)) > 0);
            if (!bExists) {
                sql = "SELECT  COUNT(1) FROM question_subject_relation qsr " +
                        "JOIN subjects sbj ON sbj.ID_SUBJECT_GLOBAL = qsr.ID_SUBJECT_GLOBAL " +
                        "JOIN short_answer_questions shrtaq ON shrtaq.ID_GLOBAL=qsr.ID_GLOBAL " +
                        "WHERE shrtaq.IDENTIFIER='" + rqs.get_questionMUID() + "'and sbj.IDENTIFIER='" + rqs.get_subjectMUID() + "';";
                result_query = stmt.executeQuery(sql);
                bExists = (Integer.parseInt(result_query.getString(1)) > 0);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            c.close();
        }
        return bExists;
    }

    /**
     * method to get the id global of the subject base on the IDENTIFIER
     * @param rqs
     * @throws Exception
     */
    static public String getSubjectId(RelationQuestionSubject rqs) throws Exception {
        // Check if the relation question/subject exists already
        String sql = "SELECT  ID_SUBJECT_GLOBAL FROM subjects WHERE IDENTIFIER=?";
        String sbj_id = "0";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, rqs.get_subjectMUID());
            ResultSet result_query = stmt.executeQuery(sql);
            sbj_id = result_query.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sbj_id;
    }

    /**
     * method to get the id global of the subject base on the IDENTIFIER
     * @param rqs
     * @throws Exception
     */
    static public String getQUestionId(RelationQuestionSubject rqs) throws Exception {
        // Check if the relation question/subject exists already
        String rec_id = "0";
        String sql = "SELECT  ID_GLOBAL FROM multiple_choice_questions " +
                "WHERE IDENTIFIER=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, rqs.get_questionMUID());
            ResultSet result_query = stmt.executeQuery(sql);
            rec_id = result_query.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
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

        String sbj_id = getSubjectId(rqs);
        String mcq_id = getQUestionId(rqs);
        String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) VALUES (?,?,?);";

        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, mcq_id);
            stmt.setString(2, sbj_id);
            stmt.setString(3, String.valueOf(rqs.get_level()));
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
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
        //one get the list of all subjects linked to the question (also check parents subjects)
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

        try (Connection c = Utilities.getDbConnection()) {
            c.setAutoCommit(false);

            for (int i = 0; i < allSubjectsVector.size(); i++) {
                String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                        "WHERE ID_GLOBAL='" + questionID + "' " +
                        "AND ID_SUBJECT_GLOBAL= (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = ?)";
                PreparedStatement stmt = c.prepareStatement(query);
                stmt.setString(1, allSubjectsVector.get(i));
                ResultSet rs = stmt.executeQuery();
                Vector<String> queries = new Vector<>();
                while (rs.next()) {
                    queries.add(rs.getString("ID_GLOBAL"));
                }
                if (queries.size() == 0) {
                    String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT t1.ID_GLOBAL,t2.ID_SUBJECT_GLOBAL," +
                            "'1' FROM generic_questions t1, subjects t2 WHERE t1.ID_GLOBAL = ? AND t2.SUBJECT=?";
                    stmt = c.prepareStatement(sql);
                    stmt.setString(1, questionID);
                    stmt.setString(2, allSubjectsVector.get(i));
                    stmt.executeUpdate();
                }
                stmt.close();
            }
            c.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Vector<String> getQuestionsIdsForSubject(String subject) {
        Vector<String> questionIDs = new Vector<>();
        String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                "WHERE ID_SUBJECT_GLOBAL = (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = ?);";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, subject);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                questionIDs.add(rs.getString("ID_GLOBAL"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionIDs;
    }

    static public void removeRelationsWithQuestion(String questionID) {
        String sql = "DELETE FROM question_subject_relation WHERE ID_GLOBAL=?;";
        DbUtils.updateWithOneParam(sql, questionID);
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
        Integer questionType = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(questionId);
        String query;
        if (questionType == 0) {
            query = "SELECT subjects.IDENTIFIER, SUBJECT_LEVEL FROM subjects " +
                    "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                    "INNER JOIN multiple_choice_questions ON multiple_choice_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                    "where multiple_choice_questions.ID_GLOBAL=?";
        } else {
            query = "SELECT subjects.IDENTIFIER, SUBJECT_LEVEL FROM subjects " +
                    "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                    "INNER JOIN short_answer_questions ON short_answer_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                    "where short_answer_questions.ID_GLOBAL=?";
        }

        try (Connection c = Utilities.getDbConnection();
                    PreparedStatement stmt = c.prepareStatement(query)) {
            stmt.setString(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sbjMUID = rs.getString("IDENTIFIER");
                int level = rs.getInt("SUBJECT_LEVEL");
                RelationQuestionSubject rqs = new RelationQuestionSubject(questionId, sbjMUID, level);
                subjects.add(rqs);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
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
