package koeko.database_management;

import koeko.view.Homework;
import koeko.view.Utilities;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DbTableHomework {
    static private String KEY_TABLE_HOMEWORK = "homework";
    static private String KEY_UID = "HW_UID";
    static private String KEY_NAME = "name";
    static private String KEY_IDCODE= "id_code";
    static private String KEY_DUEDATE = "due_date";
    static private String KEY_MODIF_DATE = "modif_date";

    static public void createTableHomeworks() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_HOMEWORK +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_UID + " TEXT, " +
                KEY_NAME + " TEXT  NOT NULL, " +
                KEY_IDCODE + " TEXT, " +
                KEY_DUEDATE + " TEXT, " +
                KEY_MODIF_DATE + " TEXT) ";
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    static public void insertHomework(Homework homework) {
        String sql = "INSERT OR REPLACE INTO " + KEY_TABLE_HOMEWORK + " (" + KEY_UID + "," + KEY_NAME + "," +
                KEY_IDCODE + "," + KEY_DUEDATE + "," + KEY_MODIF_DATE + ") VALUES(?,?,?,?,?)";
        DbUtils.updateWithFiveParam(sql, homework.getUid(), homework.getName(), homework.getIdCode(), homework.getDueDate().toString(), Utilities.TimestampForNowAsString());
    }

    static public void updateHomework(Homework homework, String oldName) {
        String sql = "UPDATE " + KEY_TABLE_HOMEWORK + " SET " + KEY_NAME + "=?, " + KEY_IDCODE + "=?, " + KEY_DUEDATE
                + "=?," + KEY_MODIF_DATE + "=?  WHERE " + KEY_NAME + "=?";
        DbUtils.updateWithFiveParam(sql, homework.getName(), homework.getIdCode(), homework.getDueDate().toString(),
                Utilities.TimestampForNowAsString(), oldName);
        DbTableRelationHomeworkQuestion.updateHomeworkName(homework.getName(), oldName);
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
                homework.setUid(rs.getString(KEY_UID));
                homework.setName(rs.getString(KEY_NAME));
                homework.setIdCode(rs.getString(KEY_IDCODE));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(rs.getString(KEY_DUEDATE), formatter);
                homework.setDueDate(date.toString());
                Boolean inserted = false;
                if (date.isBefore(LocalDate.now())) {
                    for (int i = 0; i < oldHomeworks.size(); i++) {
                        if (date.isAfter(LocalDate.parse(oldHomeworks.get(i).getDueDate(), formatter))) {
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
                        if (date.isBefore(LocalDate.parse(homeworks.get(i).getDueDate(), formatter))) {
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

    static public Homework getHomeworkWithName(String hwName) {
        Homework homework = new Homework();
        String sql = "SELECT * FROM " + KEY_TABLE_HOMEWORK + " WHERE " + KEY_NAME + "=?";
        try (Connection c = Utilities.getDbConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, hwName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                homework.setName(hwName);
                homework.setUid(rs.getString(KEY_UID));
                homework.setIdCode(rs.getString(KEY_IDCODE));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(rs.getString(KEY_DUEDATE), formatter);
                homework.setDueDate(date.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return homework;
    }

    static public void deleteHomework(String homeworkName) {
        String sql = "DELETE FROM " + KEY_TABLE_HOMEWORK + " WHERE " + KEY_NAME + " = ?";
        DbUtils.updateWithOneParam(sql, homeworkName);
    }

    public static ArrayList<Homework> getHomeworksForSyncing() {
        ArrayList<Homework> homeworks = new ArrayList<>();
        String sql = "SELECT * FROM " + KEY_TABLE_HOMEWORK;
        Timestamp lastSync = Utilities.StringToTimestamp(DBTableSyncOp.GetLastSyncOp());
        try (Connection c = Utilities.getDbConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString(KEY_IDCODE) != null && rs.getString(KEY_IDCODE).length() > 0 &&
                        lastSync.before(Utilities.StringToTimestamp(rs.getString(KEY_MODIF_DATE)))) {
                    Homework homework = new Homework();
                    homework.setUid(rs.getString(KEY_UID));
                    homework.setName(rs.getString(KEY_NAME));
                    homework.setIdCode(rs.getString(KEY_IDCODE));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(rs.getString(KEY_DUEDATE), formatter);
                    homework.setDueDate(date.toString());
                    homeworks.add(homework);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Homework homework : homeworks) {
            homework.setQuestions(DbTableRelationHomeworkQuestion.getQuestionIdsFromHomeworkName(homework.getName()));
        }

        return homeworks;
    }

    public static void setHomeworkUID(String uid, String name) {
        String sql = "UPDATE " + KEY_TABLE_HOMEWORK + " SET " + KEY_UID + "=? WHERE " + KEY_NAME + "=?";
        DbUtils.updateWithTwoParam(sql, uid, name);
    }

    public static void updateHomeworkTimestamp(String hwName) {
        String sql = "UPDATE " + KEY_TABLE_HOMEWORK + " SET " + KEY_MODIF_DATE + "=? WHERE " + KEY_NAME + "=?";
        DbUtils.updateWithTwoParam(sql, Utilities.TimestampForNowAsString(), hwName);
    }
}
