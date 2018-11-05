package koeko.database_management;

import koeko.questions_management.Test;
import koeko.questions_management.QuestionGeneric;
import koeko.view.QuestionView;
import koeko.view.Utilities;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableTest {
    static public void createTableTest(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS test " +
                    "(ID_TEST       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_TEST_GLOBAL      INT     NOT NULL, " +
                    " NAME      TEXT     NOT NULL, " +
                    " MEDIA_FILE      TEXT, " +
                    " SEND_MEDIA_FILE      INT, " +
                    " TEST_MODE           INT    NOT NULL," +
                    " QUANTITATIVE_EVAL           TEXT    NOT NULL," +
                    " MEDALS       TEXT, " +
                    " MODIF_DATE       TEXT, " +
                    " IDENTIFIER        VARCHAR(15))";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public ArrayList<Test> getAllTests() {
        ArrayList<Test> tests = new ArrayList<>();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_TEST_GLOBAL,NAME,TEST_MODE FROM test;";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Test newTest = new Test();
                newTest.setTestName(rs.getString("NAME"));
                newTest.setIdTest(rs.getString("ID_TEST_GLOBAL"));
                newTest.setTestMode(rs.getInt("TEST_MODE"));
                ArrayList<String> newQuestionIDsList = DbTableRelationQuestionQuestion.getFirstLayerQuestionIdsFromTestName(String.valueOf(newTest.getTestName()));
                ArrayList<QuestionGeneric> questionGenericArrayList = new ArrayList<>();
                for (int i = 0; i < newQuestionIDsList.size(); i++) {
                    QuestionGeneric newQuestionGeneric = new QuestionGeneric();
                    newQuestionGeneric.setGlobalID(newQuestionIDsList.get(i));
                    newQuestionGeneric.setIntTypeOfQuestion(DbTableQuestionGeneric.getQuestionTypeFromIDGlobal(String.valueOf(newQuestionIDsList.get(i))));
                    questionGenericArrayList.add(newQuestionGeneric);
                }
                newTest.setIdsQuestions(newQuestionIDsList);
                newTest.setGenericQuestions(questionGenericArrayList);
                tests.add(newTest);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return tests;
    }

    static public ArrayList<QuestionView> getAllTestViews() {
        ArrayList<QuestionView> testViews = new ArrayList<>();

        String sql = 	"SELECT * FROM test";
        try (Connection conn = Utilities.getDbConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                QuestionView testView = new QuestionView();
                testView.setTYPE(2);
                testView.setID(rs.getString("ID_TEST_GLOBAL"));
                testView.setQCM_MUID(rs.getString("IDENTIFIER"));
                testView.setQUESTION(rs.getString("NAME"));
                String modifDate = rs.getString("MODIF_DATE");
                testView.setQCM_UPD_TMS(Utilities.StringToTimestamp(modifDate));
                testViews.add(testView);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return testViews;
    }

    static public Test getLastTests() {
        Test newTest = null;
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_TEST_GLOBAL,NAME FROM test WHERE ID_TEST = (SELECT MAX(ID_TEST) FROM test);";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                newTest = new Test();
                newTest.setTestName(rs.getString("NAME"));
                newTest.setIdTest(rs.getString("ID_TEST_GLOBAL"));
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return newTest;
    }

    /**
     * testMode: 0 = certificative assessment; 1 = formative assessment with no question order; 2 = formative assessment
     * with order enforced
     * @param testID
     * @return
     */
    static public Test getTestWithID(String testID) {
        Test newTest = new Test();
        if (Long.valueOf(testID) < 0) {
            testID = String.valueOf(Long.valueOf(testID));
        }
        newTest.setIdTest(testID);
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT NAME,TEST_MODE, MEDIA_FILE, SEND_MEDIA_FILE FROM test WHERE ID_TEST_GLOBAL = '" + testID + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                newTest = new Test();
                newTest.setTestName(rs.getString("NAME"));
                newTest.setTestMode(rs.getInt("TEST_MODE"));
                newTest.setMediaFileName(rs.getString("MEDIA_FILE"));
                newTest.setSendMediaFile(rs.getInt("SEND_MEDIA_FILE"));
                newTest.setIdTest(testID);
            }
            stmt.close();
            c.commit();
            c.close();

            //get the objectives and insert them inside the test object
            newTest.setObjectives(DbTableRelationObjectiveTest.getObjectivesFromTestName(newTest.getTestName()));
            newTest.setObjectivesIDs(DbTableRelationObjectiveTest.getObjectivesIDsFromTestName(newTest.getTestName()));
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return newTest;
    }
    static public String addTest(Test newTest) {
        String testID = "";

        String sql = 	"REPLACE INTO test (ID_TEST_GLOBAL, NAME, TEST_MODE, QUANTITATIVE_EVAL, MODIF_DATE)" +
                "VALUES (?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            if (newTest.getIdTest().length() < 15) {
                testID = Utilities.localUniqueID();
            } else {
                testID = newTest.getIdTest();
            }
            pstmt.setString(1, testID);
            pstmt.setString(2, newTest.getTestName());
            pstmt.setString(3, String.valueOf(newTest.getTestMode()));
            pstmt.setString(4, "-1");
            pstmt.setString(5, Utilities.TimestampForNowAsString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return testID;
    }

    static public void setMedals(String name, String medals) {
        String sql = "UPDATE test SET MEDALS = ? WHERE NAME = ?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            PreparedStatement pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, medals);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void setMediaFile(String mediaFile, String testName) {
        String sql = "UPDATE test SET MEDIA_FILE = ?, SEND_MEDIA_FILE = ? WHERE NAME = ?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, mediaFile);
            pstmt.setInt(2, 1);
            pstmt.setString(3, testName);
            pstmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void setSendMediaFile(int sendMedia, String testName) {
        String sql = "UPDATE test SET SEND_MEDIA_FILE = ? WHERE NAME = ?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = c.prepareStatement(sql)) {
            pstmt.setInt(1, sendMedia);
            pstmt.setString(2, testName);
            pstmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public String getMediaFileName(String idTest) {
        Integer sendMediaFile = 0;
        idTest = Utilities.setPositiveIdSign(idTest);
        String mediaFile = "";
        String sql = "SELECT MEDIA_FILE, SEND_MEDIA_FILE FROM test WHERE ID_TEST_GLOBAL = ?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, idTest);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mediaFile = rs.getString("MEDIA_FILE");
                sendMediaFile = rs.getInt("SEND_MEDIA_FILE");
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        if (sendMediaFile != 1) {
            mediaFile = "";
        }
        return mediaFile;
    }

    static public String getMedals(String name) {
        String medals = "";
        String sql = "SELECT MEDALS FROM test WHERE NAME = ?";
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = c.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                medals = rs.getString("MEDALS");
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }

        return medals;
    }

    static public void removeTestWithID(String ID) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM test WHERE ID_TEST_GLOBAL = '" + ID + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void removeTestWithName(String testName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "DELETE FROM test WHERE NAME = '" + testName + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        DbTableRelationQuestionQuestion.removeRelationsWithTest(testName);
    }
    static public void renameTest(String global_id, String newName) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE test SET NAME = '" + newName + "', MODIF_DATE = '" + Utilities.TimestampForNowAsString() +
                    "' WHERE ID_TEST_GLOBAL = '" + global_id + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void changeTestMode(String global_id, Integer testMode) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "UPDATE test SET TEST_MODE = '" + testMode + "', MODIF_DATE = '" + Utilities.TimestampForNowAsString() +
                    "' WHERE ID_TEST_GLOBAL = '" + global_id + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public static String getTestIdWithName(String test) {
        String testId = "";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_TEST_GLOBAL FROM test WHERE NAME = '" + test + "';";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                testId = rs.getString("ID_TEST_GLOBAL");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return testId;
    }
}
