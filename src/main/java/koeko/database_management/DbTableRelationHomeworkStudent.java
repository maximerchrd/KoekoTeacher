package koeko.database_management;

import koeko.controllers.LeftBar.HomeworkControlling.Homework;
import koeko.view.Utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DbTableRelationHomeworkStudent {
    static private String KEY_TABLE_HOMEWORK_STUDENT = "homework_student_relation";
    static private String KEY_HOMEWORK_NAME = "name";
    static private String KEY_STUDENT_ID = "student_id";

    static public void createTableHomeworkStudentRelation() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_HOMEWORK_STUDENT +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_HOMEWORK_NAME + " TEXT  NOT NULL, " +
                KEY_STUDENT_ID + " TEXT) ";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public ArrayList<String> getStudentIdsFromHomeworkName(String homeworkName) {
        ArrayList<String> studentIds = new ArrayList<>();
        String sql = "SELECT " + KEY_STUDENT_ID + " FROM " + KEY_TABLE_HOMEWORK_STUDENT + " WHERE " + KEY_HOMEWORK_NAME + "=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, homeworkName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                studentIds.add(rs.getString(KEY_STUDENT_ID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return studentIds;
    }

    static public void insertHomeworkStudentRelation(String homeworkName, String studenId) {
        String sql = "INSERT OR REPLACE INTO " + KEY_TABLE_HOMEWORK_STUDENT + " (" + KEY_HOMEWORK_NAME + "," +
                KEY_STUDENT_ID + ") VALUES(?,?)";
        DbUtils.updateWithTwoParam(sql, homeworkName, studenId);
    }

    static public void deleteHomeworkStudentRelation(String homeworkName, String studenId) {
        String sql = "DELETE FROM " + KEY_TABLE_HOMEWORK_STUDENT + " WHERE " + KEY_HOMEWORK_NAME + " = ? AND "
                + KEY_STUDENT_ID + "=?";
        DbUtils.updateWithTwoParam(sql, homeworkName, studenId);
    }
}
