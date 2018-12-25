package koeko.database_management;

import koeko.view.RelationQuestionQuestion;
import koeko.view.RelationQuestionTest;
import koeko.view.Utilities;

import java.sql.*;
import java.util.*;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableRelationQuestionQuestion {
    static public void createTableRelationQuestionQuestion(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS question_question_relation " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL_1      TEXT     NOT NULL, " +
                    " ID_GLOBAL_2      TEXT     NOT NULL, " +
                    " TEST_ID      TEXT, " +
                    " TEST      TEXT     NOT NULL, " +
                    " CONDITION      TEXT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (ID_GLOBAL_1, ID_GLOBAL_2, TEST)) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public void addRelationQuestionQuestion(String idGlobal1, String idGlobal2, String test, String condition) {
        addRelationQuestionQuestion(idGlobal1, idGlobal2, test, "0", condition);
    }
    /**
     * adds a relation between 2 questions. If the condition is empty, adds a horizontal relation.
     * @param idGlobal1
     * @param idGlobal2
     * @param test
     * @param testid
     * @param condition
     */
    static public void addRelationQuestionQuestion(String idGlobal1, String idGlobal2, String test, String testid, String condition) {
        if (testid.substring(0,1).contentEquals("-")) {
            testid = testid.substring(1);
        }
        String sql = 	"INSERT OR IGNORE INTO question_question_relation (ID_GLOBAL_1, ID_GLOBAL_2, TEST_ID, TEST, CONDITION) " +
                "VALUES (?,?,?,?,?)";
        DbUtils.updateWithFiveParam(sql, idGlobal1, idGlobal2, testid, test, condition);
    }

    static public Set<String> getQuestionsLinkedToTest(String test) {
        Set<String> questionIDs = new LinkedHashSet<>();
        String sql = "SELECT ID_GLOBAL_1,ID_GLOBAL_2 FROM question_question_relation WHERE TEST=?;";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, test);
            ResultSet rs = stmt.executeQuery();
            while ( rs.next() ) {
                questionIDs.add(rs.getString("ID_GLOBAL_1"));
                questionIDs.add(rs.getString("ID_GLOBAL_2"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return questionIDs;
    }

    static public ArrayList<String> getQuestionsLinkedToTestId(String testId) {
        testId = Utilities.setPositiveIdSign(testId);
        Set<String> questionIDs = new LinkedHashSet<>();
        String sql = "SELECT ID_GLOBAL_1,ID_GLOBAL_2 FROM question_question_relation WHERE TEST_ID=?;";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, testId);
            ResultSet rs = stmt.executeQuery();
            while ( rs.next() ) {
                questionIDs.add(rs.getString("ID_GLOBAL_1"));
                questionIDs.add(rs.getString("ID_GLOBAL_2"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> questionIdsArray = new ArrayList<>();
        for (String questionId : questionIDs) {
            if (!questionId.contentEquals("0")) {
                questionIdsArray.add(questionId);
            }
        }

        //if the array is empty, we assume the id was a question
        if (questionIdsArray.size() == 0) {
            questionIdsArray.add(testId);
        }

        return questionIdsArray;
    }

    static public ArrayList<String> getFirstLayerQuestionIdsFromTestName(String testName) {
        ArrayList<String> questionIds = new ArrayList<>();
        String query = "SELECT ID_GLOBAL_2 FROM question_question_relation WHERE ID_GLOBAL_1=? AND TEST=? AND CONDITION=?";

        ArrayList<String> nextId = new ArrayList<>();
        nextId.add("0");
        while (nextId.size() > 0) {
            questionIds.add(nextId.get(0));
            nextId = DbUtils.getArrayStringWithThreeParams(query, nextId.get(0), testName, "");
        }
        questionIds.remove(0);

        return questionIds;
    }

    static public Vector<String> getQuestionsLinkedToQuestion(String questionID, String test) {
        Vector<String> questionIDs = new Vector<>();
        String sql = "SELECT ID_GLOBAL_2, CONDITION FROM question_question_relation WHERE ID_GLOBAL_1=? AND TEST=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, questionID);
            stmt.setString(2, test);
            ResultSet rs = stmt.executeQuery();
            while ( rs.next() ) {
                //We test that the condition is not "" to avoid branching "brother questions"
                if (rs.getString("CONDITION").length() > 0) {
                    questionIDs.add(rs.getString("ID_GLOBAL_2"));
                }
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return questionIDs;
    }

    static public ArrayList<RelationQuestionQuestion> getQuestionsRelationsLinkedToTest(String testId) {
        ArrayList<RelationQuestionQuestion> relationQuestionQuestions = new ArrayList<>();

        String sql = "SELECT * FROM question_question_relation WHERE TEST_ID=?";
        try (Connection connection = Utilities.getDbConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);) {

            pstmt.setString(1, testId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RelationQuestionQuestion relationQuestionQuestion = new RelationQuestionQuestion();
                relationQuestionQuestion.setTestId(testId);
                relationQuestionQuestion.setIdGlobal1(rs.getString("ID_GLOBAL_1"));
                relationQuestionQuestion.setIdGlobal2(rs.getString("ID_GLOBAL_2"));
                relationQuestionQuestion.setCondition(rs.getString("CONDITION"));
                relationQuestionQuestions.add(relationQuestionQuestion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return relationQuestionQuestions;
    }



    static public String getFormattedQuestionsLinkedToTest(String test) {
        Vector<String> testMap = new Vector<>();
        String sql = "SELECT * FROM question_question_relation WHERE TEST=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, test);
            ResultSet rs = stmt.executeQuery();
            while ( rs.next() ) {
                testMap.add(rs.getString("ID_GLOBAL_1") + "|||" + rs.getString("CONDITION") + "|||" + rs.getString("ID_GLOBAL_2"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //the code below reorders and formats to give 1 string containing a map of the test
        Vector<String> testIdsOrdered = new Vector<>();
        Vector<String> nodesIDs = new Vector<>();

        //we need this vector to see which IDs have null parent/bros (the ones appearing less than twice on the right)
        Vector<String> rightNodesIDs = new Vector<>();

        //we build an array with all the IDs appearing on both sides of the relation
        for (String relation : testMap) {
            String[] ids = relation.split("\\|\\|\\|");
            if (!nodesIDs.contains(ids[0])) {
                nodesIDs.add(relation.split("\\|\\|\\|")[0]);
            }

            if (!nodesIDs.contains(ids[2])) {
                nodesIDs.add(relation.split("\\|\\|\\|")[2]);
            }

            rightNodesIDs.add(relation.split("\\|\\|\\|")[2]);
        }

        //add the null->node parent/bro relations
        for (String nodeId : nodesIDs) {
            String broRelation = "||||||" + nodeId;
            String parentRelation = "|||CONDITION|||" + nodeId;
            for (String relation : testMap) {
                if (relation.split("\\|\\|\\|")[2].contentEquals(nodeId)) {
                    if (relation.split("\\|\\|\\|")[1].length() > 0) {
                        parentRelation = "";
                    } else {
                        broRelation = "";
                    }
                }
            }

            if (broRelation.length() > 0) {
                testMap.add(broRelation);
            }
            if (parentRelation.length() > 0) {
                testMap.add(parentRelation);
            }
        }


        DbTableRelationQuestionQuestion.recursiveAdd(testMap, nodesIDs, testIdsOrdered, "", "");

        String formattedTestMap = "";

        for (String formattedRelation : testIdsOrdered) {
            formattedTestMap += formattedRelation + "|||";
        }

        return formattedTestMap;
    }

    /**
     * recursively adds to the test IDs a new node (ID)  in the right order according to this scheme:
     * recursiveAdd(bro, parent) {
     *             add(nextNode)
     *             recursiveAdd(bro,nextNode)
     *             recursiveAdd(nextNode,parent)
     *         }
     * To later build a string which is a map of the test as:  ///nextNodeID1;;;relNodeID1:::condition1;;;relNodeID2;;;condition2|||nextNodeID2;;; ... ///
     * @param testMap
     * @param testMapFormattedAnsOrdered
     * @param bigBrotherID
     * @param parentID
     */
    static private void recursiveAdd(Vector<String> testMap, Vector<String> nodesIDs, Vector<String> testMapFormattedAnsOrdered, String bigBrotherID, String parentID) {
        String nextNode = "";

        Vector<String> nodesIDsCopy = (Vector<String>) nodesIDs.clone();
        //nodesIDsCopy.addAll(nodesIDsCopy);

        //remove ID if the left side of relation is not bigBro or parent
        for (String relation : testMap) {
            if (relation.split("\\|\\|\\|")[1].length() > 0 && !relation.split("\\|\\|\\|")[0].contentEquals(parentID)) {
                nodesIDsCopy.remove(relation.split("\\|\\|\\|")[2]);
            } else if (relation.split("\\|\\|\\|")[1].length() == 0 && !relation.split("\\|\\|\\|")[0].contentEquals(bigBrotherID)) {
                nodesIDsCopy.remove(relation.split("\\|\\|\\|")[2]);
            }
        }

        if (nodesIDsCopy.size() == 1) {
            nextNode = nodesIDsCopy.get(0);
            String nextNodeWithRelations = nextNode;
            //add the relations to other IDs as: ///nextNodeID1;;;relNodeID1:::condition1;;;relNodeID2;;;condition2|||nextNodeID2///
            for (String relation : testMap) {
                if (relation.split("\\|\\|\\|")[0].contentEquals(nextNode)) {
                    nextNodeWithRelations += ";;;" + relation.split("\\|\\|\\|")[2] + ":::" + relation.split("\\|\\|\\|")[1] + ":::END:::";
                }
            }

            //add the next node to the test IDs
            testMapFormattedAnsOrdered.add(nextNodeWithRelations);

            //call the recursive methods
            DbTableRelationQuestionQuestion.recursiveAdd(testMap, nodesIDs, testMapFormattedAnsOrdered, "", nextNode);
            DbTableRelationQuestionQuestion.recursiveAdd(testMap, nodesIDs, testMapFormattedAnsOrdered, nextNode, parentID);
        } else if (nodesIDsCopy.size() > 1){
            System.out.println("Error when selecting next node in recursiveAdd: we should have 1 or 0 ID remaining and we have:" + nodesIDs.size() + " IDs");
        }

    }

    static public void removeRelationsWithQuestion(String questionID) {
        String sql = "DELETE FROM question_question_relation WHERE ID_GLOBAL_1=?";
        DbUtils.updateWithOneParam(sql, questionID);
        sql = "DELETE FROM question_question_relation WHERE ID_GLOBAL_2=?";
        DbUtils.updateWithOneParam(sql, questionID);
    }

    static public void removeRelationsWithTest(String test) {
        String sql = "DELETE FROM question_question_relation WHERE TEST=?";
        DbUtils.updateWithOneParam(sql, test);
    }

    public static void moveUp(String questionId, String bigBrotherId, String testId) {
        String sql = "UPDATE question_question_relation SET ID_GLOBAL_1=? WHERE ID_GLOBAL_2=? AND ID_GLOBAL_1=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, questionId, questionId, bigBrotherId, "",testId);

        sql = "UPDATE question_question_relation SET ID_GLOBAL_2=? WHERE ID_GLOBAL_2=? AND ID_GLOBAL_1=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, bigBrotherId, questionId, questionId, "",testId);

        sql = "UPDATE question_question_relation SET ID_GLOBAL_1=? WHERE ID_GLOBAL_2!=? AND ID_GLOBAL_1=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, bigBrotherId, bigBrotherId, questionId, "",testId);

        sql = "UPDATE question_question_relation SET ID_GLOBAL_2=? WHERE ID_GLOBAL_2=? AND ID_GLOBAL_1!=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, questionId, bigBrotherId, questionId, "",testId);
    }

    public static void moveDown(String questionId, String littleBrotherId, String testId) {
        String sql = "UPDATE question_question_relation SET ID_GLOBAL_1=? WHERE ID_GLOBAL_2=? AND ID_GLOBAL_1=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, littleBrotherId, littleBrotherId, questionId, "",testId);

        sql = "UPDATE question_question_relation SET ID_GLOBAL_2=? WHERE ID_GLOBAL_2=? AND ID_GLOBAL_1=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, questionId, littleBrotherId, littleBrotherId, "",testId);

        sql = "UPDATE question_question_relation SET ID_GLOBAL_1=? WHERE ID_GLOBAL_2!=? AND ID_GLOBAL_1=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, questionId, questionId, littleBrotherId, "",testId);

        sql = "UPDATE question_question_relation SET ID_GLOBAL_2=? WHERE ID_GLOBAL_2=? AND ID_GLOBAL_1!=? AND CONDITION=? AND TEST_ID=?";
        DbUtils.updateWithFiveParam(sql, littleBrotherId, questionId, littleBrotherId, "",testId);
    }
}
