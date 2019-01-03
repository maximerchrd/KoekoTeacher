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
import java.util.Locale;

public class DbTableHomework {
    static private String KEY_TABLE_HOMEWORK = "homework";
    static private String KEY_NAME = "name";
    static private String KEY_IDCODE= "id_code";
    static private String KEY_DUEDATE = "due_date";

    static public void createTableHomeworks() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_HOMEWORK +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " TEXT  NOT NULL, " +
                KEY_IDCODE + " TEXT, " +
                KEY_DUEDATE + " TEXT) ";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void insertHomework(Homework homework) {
        String sql = "INSERT OR REPLACE INTO " + KEY_TABLE_HOMEWORK + " (" + KEY_NAME + "," +
                KEY_IDCODE + "," + KEY_DUEDATE + ") VALUES(?,?,?)";
        DbUtils.updateWithThreeParam(sql, homework.getName(), homework.getIdCode(), homework.getDueDate().toString());
    }

    static public void updateHomework(Homework homework, String oldName) {
        String sql = "UPDATE " + KEY_TABLE_HOMEWORK + " SET " + KEY_NAME + "=?, " + KEY_IDCODE + "=?, " + KEY_DUEDATE
                + "=? WHERE " + KEY_NAME + "=?";
        DbUtils.updateWithFourParam(sql, homework.getName(), homework.getIdCode(), homework.getDueDate().toString(),
                oldName);
    }

    static public Boolean checkIfNameAlreadyExists(String homeworkName) {
        String sql = "SELECT * FROM " + KEY_TABLE_HOMEWORK + " WHERE " + KEY_NAME + " = ?";
        Boolean nameExists = false;
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, homeworkName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                nameExists = true;
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nameExists;
    }

    static public ArrayList<Homework> getAllHomeworks() {
        ArrayList<Homework> homeworks = new ArrayList<>();
        ArrayList<Homework> oldHomeworks = new ArrayList<>();
        String sql = "SELECT * FROM " + KEY_TABLE_HOMEWORK;
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Homework homework = new Homework();
                homework.setName(rs.getString(KEY_NAME));
                homework.setIdCode(rs.getString(KEY_IDCODE));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(rs.getString(KEY_DUEDATE), formatter);
                homework.setDueDate(date);
                Boolean inserted = false;
                if (homework.getDueDate().isBefore(LocalDate.now())) {
                    for (int i = 0; i < oldHomeworks.size(); i++) {
                        if (homework.getDueDate().isAfter(oldHomeworks.get(i).getDueDate())) {
                            oldHomeworks.add(i, homework);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) {
                        oldHomeworks.add(0, homework);
                    }
                } else {
                    for (int i = 0; i < homeworks.size(); i++) {
                        if (homework.getDueDate().isBefore(homeworks.get(i).getDueDate())) {
                            homeworks.add(i, homework);
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) {
                        homeworks.add(homework);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        homeworks.addAll(oldHomeworks);
        return homeworks;
    }

    static public void deleteHomework(String homeworkName) {
        String sql = "DELETE FROM " + KEY_TABLE_HOMEWORK + " WHERE " + KEY_NAME + " = ?";
        DbUtils.updateWithOneParam(sql, homeworkName);
    }
}
