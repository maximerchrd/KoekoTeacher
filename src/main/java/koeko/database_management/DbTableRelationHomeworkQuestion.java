package koeko.database_management;

import koeko.view.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DbTableRelationHomeworkQuestion {
    static private String KEY_TABLE_HOMEWORK_QUESTION = "homework_question_relation";
    static private String KEY_HOMEWORK_NAME = "homework_name";
    static private String KEY_QUESTION_ID = "question_id";

    static public void createTableHomeworkQuestionRelation() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_HOMEWORK_QUESTION +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_HOMEWORK_NAME + " TEXT  NOT NULL, " +
                KEY_QUESTION_ID + " TEXT) ";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public ArrayList<String> getQuestionIdsFromHomeworkName(String homeworkName) {
        ArrayList<String> studentIds = new ArrayList<>();
        String sql = "SELECT " + KEY_QUESTION_ID + " FROM " + KEY_TABLE_HOMEWORK_QUESTION + " WHERE " + KEY_HOMEWORK_NAME + "=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, homeworkName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentIds.add(rs.getString(KEY_QUESTION_ID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studentIds;
    }

    static public void insertHomeworkQuestionRelation(String homeworkName, String questionId) {
        String sql = "INSERT OR REPLACE INTO " + KEY_TABLE_HOMEWORK_QUESTION + " (" + KEY_HOMEWORK_NAME + "," +
                KEY_QUESTION_ID + ") VALUES(?,?)";
        DbUtils.updateWithTwoParam(sql, homeworkName, questionId);
    }

    static public void deleteHomeworkQuestionRelation(String homeworkName, String questionId) {
        String sql = "DELETE FROM " + KEY_TABLE_HOMEWORK_QUESTION + " WHERE " + KEY_HOMEWORK_NAME + " = ? AND "
                + KEY_QUESTION_ID + "=?";
        DbUtils.updateWithTwoParam(sql, homeworkName, questionId);
    }
}
