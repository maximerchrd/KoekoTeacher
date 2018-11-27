package koeko.database_management;

import koeko.view.Objective;
import koeko.view.RelationQuestionObjective;
import koeko.view.Utilities;

import java.sql.*;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionObjective {
    static public void createTableRelationQuestionObjective(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_objective_relation " +
                    "(ID_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_OBJECTIVE_GLOBAL      INT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (ID_GLOBAL, ID_OBJECTIVE_GLOBAL)) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    static public void addRelationQuestionObjective(String objective) {
        String sql = 	"INSERT OR IGNORE INTO question_objective_relation (ID_GLOBAL, ID_OBJECTIVE_GLOBAL) " +
                "SELECT t1.ID_GLOBAL,t2.ID_OBJECTIVE_GLOBAL FROM generic_questions t1, learning_objectives t2 " +
                "WHERE t1.ID_QUESTION = (SELECT MAX(ID_QUESTION) FROM generic_questions) " +
                "AND t2.OBJECTIVE=?;";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, objective);
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void addRelationQuestionObjective(String questionID, String objective) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO question_objective_relation (ID_GLOBAL, ID_OBJECTIVE_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_OBJECTIVE_GLOBAL FROM generic_questions t1, learning_objectives t2 " +
                    "WHERE t1.ID_GLOBAL = '" + questionID + "' " +
                    "AND t2.OBJECTIVE='" + objective + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void addRelationQuestionObjective(RelationQuestionObjective relationQuestionObjective) throws Exception {
        String questionID;
        int questionType = 0;
        questionID = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionIDWithUID(relationQuestionObjective.get_questionMUID());
        if (questionID.contentEquals("")) {
            questionType = 1;
            questionID = DbTableQuestionShortAnswer.getShortAnswerQuestionIDWithUID(relationQuestionObjective.get_questionMUID());
        }
        Objective objective = DbTableLearningObjectives.getObjectiveFromIdentifier(relationQuestionObjective.get_objectiveMUID());
        String objectiveName = objective.get_objectiveName();
        String sql;
        if (questionType == 0) {
            sql = "INSERT OR IGNORE INTO question_objective_relation (ID_GLOBAL, ID_OBJECTIVE_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_OBJECTIVE_GLOBAL FROM multiple_choice_questions t1, learning_objectives t2 " +
                    "WHERE t1.ID_GLOBAL = ? AND t2.OBJECTIVE= ?";
        } else {
            sql = "INSERT OR IGNORE INTO question_objective_relation (ID_GLOBAL, ID_OBJECTIVE_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_OBJECTIVE_GLOBAL FROM short_answer_questions t1, learning_objectives t2 " +
                    "WHERE t1.ID_GLOBAL = ? AND t2.OBJECTIVE= ?";
        }
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement pstmt = c.prepareStatement(sql)){

            pstmt.setString(1, questionID);
            pstmt.setString(2, objectiveName);

            pstmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public Vector<RelationQuestionObjective> getObjectivesLinkedToQuestion(String questionID) {
        Vector<RelationQuestionObjective> relationQuestionObjectives = new Vector<>();

        Integer questionType = DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(questionID);
        String query;
        if (questionType == 0) {
            query = "SELECT learning_objectives.IDENTIFIER FROM learning_objectives " +
                    "INNER JOIN question_objective_relation ON learning_objectives.ID_OBJECTIVE_GLOBAL = question_objective_relation.ID_OBJECTIVE_GLOBAL " +
                    "INNER JOIN multiple_choice_questions ON multiple_choice_questions.ID_GLOBAL = question_objective_relation.ID_GLOBAL " +
                    "where multiple_choice_questions.ID_GLOBAL=?";
        } else {
            query = "SELECT learning_objectives.IDENTIFIER FROM learning_objectives " +
                    "INNER JOIN question_objective_relation ON learning_objectives.ID_OBJECTIVE_GLOBAL = question_objective_relation.ID_OBJECTIVE_GLOBAL " +
                    "INNER JOIN short_answer_questions ON short_answer_questions.ID_GLOBAL = question_objective_relation.ID_GLOBAL " +
                    "where short_answer_questions.ID_GLOBAL=?";
        }
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // set the corresponding param
            pstmt.setString(1, questionID);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                RelationQuestionObjective relation = new RelationQuestionObjective();
                relation.set_questionId(questionID);
                relation.set_objectiveId(rs.getString("IDENTIFIER"));
                relationQuestionObjectives.add(relation);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return relationQuestionObjectives;
    }

    /**
     * method to check if it is needed to insert a relation question/subject
     * @param rqo
     * @throws Exception
     */
    static public boolean checkIfExists(RelationQuestionObjective rqo) throws Exception {
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
            String sql = "SELECT  COUNT(1) FROM question_objective_relation qor " +
                    "JOIN learning_objectives obj ON obj.ID_SUBJECT_GLOBAL = qor.ID_SUBJECT_GLOBAL " +
                    "JOIN multiple_choice_questions mcq ON mcq.ID_GLOBAL=qor.ID_GLOBAL " +
                    "WHERE mcq.IDENTIFIER='" + rqo.get_questionMUID() + "'and obj.IDENTIFIER='" + rqo.get_objectiveMUID() + "';";
            ResultSet result_query = stmt.executeQuery(sql);
            bExists = (Integer.parseInt(result_query.getString(1)) > 0);
            if (!bExists) {
                sql = "SELECT  COUNT(1) FROM question_objective_relation qor " +
                        "JOIN learning_objectives obj ON obj.ID_SUBJECT_GLOBAL = qor.ID_SUBJECT_GLOBAL " +
                        "JOIN short_answer_questions shrtaq ON shrtaq.ID_GLOBAL=qor.ID_GLOBAL " +
                        "WHERE shrtaq.IDENTIFIER='" + rqo.get_questionMUID() + "'and obj.IDENTIFIER='" + rqo.get_objectiveMUID() + "';";
                result_query = stmt.executeQuery(sql);
                bExists = (Integer.parseInt(result_query.getString(1)) > 0);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return bExists;
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
            String sql = "DELETE FROM question_objective_relation WHERE ID_GLOBAL='" + questionID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }
}
