package koeko.database_management;

import koeko.questions_management.QuestionShortAnswer;
import koeko.view.QuestionView;
import koeko.view.Utilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableQuestionShortAnswer {
    static public void createTableQuestionShortAnswer(Connection connection, Statement statement) {
        try {
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS short_answer_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " LEVEL      INT     NOT NULL, " +
                    " QUESTION           TEXT    NOT NULL, " +
                    " AUTOMATIC_CORRECTION      INT     NOT NULL, " +
                    " IMAGE_PATH           TEXT    NOT NULL, " +
                    " MODIF_DATE       TEXT, " +
                    " HASH_CODE       TEXT, " +
                    " TIMER_SECONDS       INTEGER, " +
                    " IDENTIFIER        VARCHAR(15))";
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public String addShortAnswerQuestion(QuestionShortAnswer quest) {
        String idGlobal = "-1";
        String sql = "INSERT INTO short_answer_questions (ID_GLOBAL,LEVEL," +
                "QUESTION,AUTOMATIC_CORRECTION,IMAGE_PATH,IDENTIFIER,MODIF_DATE, TIMER_SECONDS, HASH_CODE) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        try {
            if (quest.getUID().length() < 15) {
                idGlobal = DbTableQuestionGeneric.addGenericQuestion(1);
            } else {
                DbTableQuestionGeneric.addGenericQuestion(1, quest.getUID());
                idGlobal = quest.getUID();
            }

            Connection c = Utilities.getDbConnection();
            c.setAutoCommit(false);
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, idGlobal);
            stmt.setString(2, quest.getLEVEL());
            stmt.setString(3, quest.getQUESTION());
            stmt.setInt(4, -1);
            stmt.setString(5, quest.getIMAGE());
            stmt.setString(6, quest.getUID());
            stmt.setString(7, Utilities.TimestampForNowAsString());
            stmt.setInt(8,quest.getTimerSeconds());
            stmt.setString(9, quest.computeShortHashCode());
            stmt.executeUpdate();
            stmt.close();
            c.commit();
            c.close();

            for (int i = 0; i < quest.getANSWER().size(); i++) {
                DbTableAnswerOptions.addAnswerOption(idGlobal, quest.getANSWER().get(i));
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return idGlobal;
    }

    static public void updateShortAnswerQuestion(QuestionShortAnswer quest) {
        String sql = "UPDATE short_answer_questions SET QUESTION=?, IMAGE_PATH=?, " +
                "MODIF_DATE=?, HASH_CODE=?, TIMER_SECONDS=? WHERE ID_GLOBAL=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, quest.getQUESTION());
            stmt.setString(2, quest.getIMAGE());
            stmt.setString(3, Utilities.TimestampForNowAsString());
            stmt.setString(4, quest.computeShortHashCode());
            stmt.setString(5, quest.getID());
            stmt.setInt(6, quest.getTimerSeconds());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public List<QuestionShortAnswer> getAllShortAnswersQuestions() throws Exception {
        List<QuestionShortAnswer> questionShortAnswerArrayList = new ArrayList<QuestionShortAnswer>();
        // Select All Query
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM short_answer_questions;");
            while (rs.next()) {
                QuestionShortAnswer quest = new QuestionShortAnswer();
                //quest.setSUBJECT(rs.getString(2));
                quest.setID(rs.getString(2));
                quest.setLEVEL(rs.getString(3));
                quest.setQUESTION(rs.getString(4));
                quest.setIMAGE(rs.getString(5));
                ArrayList<String> answers = new ArrayList<>();
                Statement stmt2 = c.createStatement();
                ResultSet rs2 = stmt2.executeQuery("SELECT OPTION FROM answer_options " +
                        "INNER JOIN question_answeroption_relation ON answer_options.ID_ANSWEROPTION_GLOBAL = question_answeroption_relation.ID_ANSWEROPTION_GLOBAL " +
                        "INNER JOIN short_answer_questions ON question_answeroption_relation.ID_GLOBAL = short_answer_questions.ID_GLOBAL " +
                        "WHERE short_answer_questions.ID_GLOBAL = '" + quest.getID() + "';");
                while (rs2.next()) {
                    answers.add(rs2.getString(1));
                }
                quest.setANSWER(answers);
                questionShortAnswerArrayList.add(quest);
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return questionShortAnswerArrayList;
    }

    static public QuestionShortAnswer getShortAnswerQuestionWithId(String questionId) {
        QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
        questionShortAnswer.setID(questionId);
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT LEVEL,QUESTION,IMAGE_PATH, TIMER_SECONDS FROM short_answer_questions WHERE ID_GLOBAL='" + questionId + "';";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                questionShortAnswer.setLEVEL(rs.getString("LEVEL"));
                questionShortAnswer.setQUESTION(rs.getString("QUESTION"));
                questionShortAnswer.setIMAGE(rs.getString("IMAGE_PATH"));
                questionShortAnswer.setTimerSeconds(rs.getInt("TIMER_SECONDS"));
            }
            ArrayList<String> answers = new ArrayList<>();
            rs = stmt.executeQuery("SELECT OPTION FROM answer_options " +
                    "INNER JOIN question_answeroption_relation ON answer_options.ID_ANSWEROPTION_GLOBAL = question_answeroption_relation.ID_ANSWEROPTION_GLOBAL " +
                    "INNER JOIN short_answer_questions ON question_answeroption_relation.ID_GLOBAL = short_answer_questions.ID_GLOBAL " +
                    "WHERE short_answer_questions.ID_GLOBAL = '" + questionShortAnswer.getID() + "';");
            while (rs.next()) {
                answers.add(rs.getString(1));
            }
            questionShortAnswer.setANSWER(answers);
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        questionShortAnswer.setObjectives(DbTableLearningObjectives.getObjectiveForQuestionID(questionId));
        questionShortAnswer.setSubjects(DbTableSubject.getSubjectsForQuestionID(questionId));
        return questionShortAnswer;
    }

    static public String getShortAnswerQuestionIDWithUID(String identifier) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String query = "SELECT ID_GLOBAL FROM short_answer_questions WHERE IDENTIFIER='" + identifier + "';";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                return rs.getString("ID_GLOBAL");
            }
            stmt.close();
            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return "";
    }

    static public Vector<QuestionView> getQuestionViews() {
        Vector<QuestionView> questionViews = new Vector<>();

        String sql = "SELECT * FROM short_answer_questions WHERE MODIF_DATE > (SELECT LAST_TS FROM syncop) OR LENGTH(TRIM(IDENTIFIER))<15;";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                QuestionView questionMultipleChoiceView = new QuestionView();
                questionMultipleChoiceView.setID(rs.getString("ID_GLOBAL"));
                questionMultipleChoiceView.setTYPE(1);
                questionMultipleChoiceView.setQUESTION(rs.getString("QUESTION"));
                questionMultipleChoiceView.setQCM_MUID(rs.getString("IDENTIFIER"));
                questionMultipleChoiceView.setIMAGE(rs.getString("IMAGE_PATH"));
                questionMultipleChoiceView.setQCM_UPD_TMS(rs.getTimestamp("MODIF_DATE"));
                questionMultipleChoiceView.setTimerSeconds(rs.getInt("TIMER_SECONDS"));
                questionViews.add(questionMultipleChoiceView);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "SELECT OPTION FROM answer_options ao " +
                "INNER JOIN question_answeroption_relation rel ON rel.ID_ANSWEROPTION_GLOBAL = ao.ID_ANSWEROPTION_GLOBAL " +
                "INNER JOIN short_answer_questions sha ON sha.ID_GLOBAL = rel.ID_GLOBAL " +
                " WHERE sha.ID_GLOBAL = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < questionViews.size(); i++) {
                pstmt.setString(1,questionViews.get(i).getID());
                ResultSet rs = pstmt.executeQuery();

                int j = 0;
                while (rs.next()) {
                    switch (j) {
                        case 0:
                            questionViews.get(i).setOPT0(rs.getString("OPTION"));
                            break;
                        case 1:
                            questionViews.get(i).setOPT1(rs.getString("OPTION"));
                            break;
                        case 2:
                            questionViews.get(i).setOPT2(rs.getString("OPTION"));
                            break;
                        case 3:
                            questionViews.get(i).setOPT3(rs.getString("OPTION"));
                            break;
                        case 4:
                            questionViews.get(i).setOPT4(rs.getString("OPTION"));
                            break;
                        case 5:
                            questionViews.get(i).setOPT5(rs.getString("OPTION"));
                            break;
                        case 6:
                            questionViews.get(i).setOPT6(rs.getString("OPTION"));
                            break;
                        case 7:
                            questionViews.get(i).setOPT7(rs.getString("OPTION"));
                            break;
                        case 8:
                            questionViews.get(i).setOPT8(rs.getString("OPTION"));
                            break;
                        default:
                            questionViews.get(i).setOPT9(questionViews.get(i).getOPT9() + rs.getString("OPTION") + "///");
                    }
                    j++;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return questionViews;
    }

    public static QuestionView getQuestionViewWithId(String questionID) {
        QuestionView questionView = new QuestionView();

        String sql = "SELECT * FROM short_answer_questions WHERE ID_GLOBAL = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, questionID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                questionView.setID(rs.getString("ID_GLOBAL"));
                questionView.setTYPE(1);
                questionView.setQUESTION(rs.getString("QUESTION"));
                questionView.setQCM_MUID(rs.getString("IDENTIFIER"));
                questionView.setIMAGE(rs.getString("IMAGE_PATH"));
                questionView.setQCM_UPD_TMS(rs.getTimestamp("MODIF_DATE"));
                questionView.setHashCode(rs.getString("HASH_CODE"));
                questionView.setTimerSeconds(rs.getInt("TIMER_SECONDS"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "SELECT OPTION FROM answer_options ao " +
                "INNER JOIN question_answeroption_relation rel ON rel.ID_ANSWEROPTION_GLOBAL = ao.ID_ANSWEROPTION_GLOBAL " +
                "INNER JOIN short_answer_questions sha ON sha.ID_GLOBAL = rel.ID_GLOBAL " +
                " WHERE sha.ID_GLOBAL = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1,questionView.getID());
                ResultSet rs = pstmt.executeQuery();

                int j = 0;
                while (rs.next()) {
                    switch (j) {
                        case 0:
                            questionView.setOPT0(rs.getString("OPTION"));
                            break;
                        case 1:
                            questionView.setOPT1(rs.getString("OPTION"));
                            break;
                        case 2:
                            questionView.setOPT2(rs.getString("OPTION"));
                            break;
                        case 3:
                            questionView.setOPT3(rs.getString("OPTION"));
                            break;
                        case 4:
                            questionView.setOPT4(rs.getString("OPTION"));
                            break;
                        case 5:
                            questionView.setOPT5(rs.getString("OPTION"));
                            break;
                        case 6:
                            questionView.setOPT6(rs.getString("OPTION"));
                            break;
                        case 7:
                            questionView.setOPT7(rs.getString("OPTION"));
                            break;
                        case 8:
                            questionView.setOPT8(rs.getString("OPTION"));
                            break;
                        default:
                            questionView.setOPT9(questionView.getOPT9() + rs.getString("OPTION") + "///");
                    }
                    j++;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return questionView;
    }
}