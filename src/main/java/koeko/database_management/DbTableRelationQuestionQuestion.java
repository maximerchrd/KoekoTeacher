package koeko.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
                    " TEST      TEXT     NOT NULL, " +
                    " CONDITION      TEXT     NOT NULL, " +
                    " CONSTRAINT unq UNIQUE (ID_GLOBAL_1, ID_GLOBAL_2, TEST)) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * adds a relation between 2 questions. If the condition is empty, adds a horizontal relation.
     * @param idGlobal1
     * @param idGlobal2
     * @param test
     * @param condition
     */
    static public void addRelationQuestionQuestion(String idGlobal1, String idGlobal2, String test, String condition) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"INSERT OR IGNORE INTO question_question_relation (ID_GLOBAL_1, ID_GLOBAL_2, TEST, CONDITION) " +
                    "VALUES ('" + idGlobal1 + "','" + idGlobal2 + "','" + test + "','" + condition + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public Vector<String> getQuestionsLinkedToQuestion(String questionID, String test) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        Vector<String> questionIDs = new Vector<>();
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT ID_GLOBAL_2 FROM question_question_relation WHERE ID_GLOBAL_1='" + questionID + "' AND TEST='" + test + "';";
            ResultSet rs = stmt.executeQuery( sql );
            while ( rs.next() ) {
                questionIDs.add(rs.getString("ID_GLOBAL_2"));
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

    static public String getFormattedQuestionsLinkedToTest(String test) {
        Vector<String> testMap = new Vector<>();
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "SELECT * FROM question_question_relation WHERE TEST='" + test + "';";
            ResultSet rs = stmt.executeQuery( sql );
            while ( rs.next() ) {
                testMap.add(rs.getString("ID_GLOBAL_1") + "|||" + rs.getString("CONDITION") + "|||" + rs.getString("ID_GLOBAL_2"));
            }
            stmt.close();

            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
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
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_question_relation WHERE ID_GLOBAL1='" + questionID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public void removeRelationsWithTest(String test) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM question_question_relation WHERE TEST='" + test + "';";
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
