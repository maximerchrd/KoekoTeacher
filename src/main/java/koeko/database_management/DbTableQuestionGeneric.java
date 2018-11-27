package koeko.database_management;

import koeko.questions_management.QuestionGeneric;
import koeko.view.Utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 * Question Type: 0 = question multiple choice; 1 = question short answer
 */
public class DbTableQuestionGeneric {
    static public void createTableQuestionGeneric(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS generic_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL           INT    NOT NULL, " +
                    " REMOVED_STATE           INT    NOT NULL, " +
                    " QUESTION_TYPE      INT     NOT NULL) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public String addGenericQuestion(int questionType) throws Exception {
        String questID = "-1";
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            questID = Utilities.localUniqueID();
            String sql = 	"REPLACE INTO generic_questions (ID_GLOBAL,REMOVED_STATE,QUESTION_TYPE) " +
                    "VALUES ('" +
                    questID + "','" +
                    0 + "','" +
                    questionType +"');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return questID;
    }

    static public void addGenericQuestion(int questionType, String globalID) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = 	"REPLACE INTO generic_questions (ID_GLOBAL,REMOVED_STATE,QUESTION_TYPE) " +
                    "VALUES ('" +
                    globalID + "','" +
                    0 + "','" +
                    questionType +"');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static public ArrayList<QuestionGeneric> getAllGenericQuestions() throws Exception{
        ArrayList<QuestionGeneric> questionGenericArrayList = new ArrayList<QuestionGeneric>();
        // Select All Query
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM generic_questions WHERE REMOVED_STATE=0;" );
            while ( rs.next() ) {
                QuestionGeneric quest = new QuestionGeneric();
                quest.setGlobalID(rs.getString("ID_GLOBAL"));
                quest.setIntTypeOfQuestion(rs.getInt("QUESTION_TYPE"));
                questionGenericArrayList.add(quest);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Read " + questionGenericArrayList.size() + " generic questions.");
        return questionGenericArrayList;
    }

    static public Vector<String> getAllGenericQuestionsIds() {
        Vector<String> questionIdsArrayList = new Vector<>();

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT ID_GLOBAL FROM generic_questions WHERE REMOVED_STATE=0;" );
            while ( rs.next() ) {
                questionIdsArrayList.add(rs.getString("ID_GLOBAL"));
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return questionIdsArrayList;
    }

    static public Integer getQuestionTypeFromIDGlobal (String idGlobal) {
        Integer questionType = -1;
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM generic_questions WHERE ID_GLOBAL = '" + idGlobal + "';" );
            if ( rs.next()) {
               questionType = rs.getInt("QUESTION_TYPE");
            }
            rs.close();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
        return questionType;
    }

    static public void removeQuestion (String idGlobal) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            String sql = "UPDATE generic_questions SET REMOVED_STATE = '1' WHERE ID_GLOBAL = '" + idGlobal + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}