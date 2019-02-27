package koeko.database_management;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by maximerichard on 24.11.17.
 */
public class DbTableSettings {
    static public int homeworkKeysLength = 10;
    static private String settingKey = "SETTING_KEY";
    static private String settingValue = "SETTING_VALUE";
    static private String nearbyModeKey = "NearbyMode";
    static private String correctionModeKey = "CorrectionMode";
    static private String forcedSyncKey = "ForcedSync";
    static private String uiModeKey = "UiMode";
    static private String teacherNameKey = "TeacherName";
    static private String homeworkKey = "HomeworkKey";
    static private String lastSyncTime = "LastSyncTime";
    static private String language = "Language";
    static private String sound = "Sound";

    static private int currentSoundValue = -1;

    static public void createTableSettings(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS settings " +
                    "(ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    settingKey + " TEXT," +
                    settingValue + " TEXT) ";
            statement.executeUpdate(sql);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(0);
        }

        if (getLastSyncTime().length() == 0) {
            insertNewSetting(nearbyModeKey, "0");
            insertNewSetting(correctionModeKey, "1");
            insertNewSetting(forcedSyncKey, "0");
            insertNewSetting(uiModeKey, "0");
            insertNewSetting(teacherNameKey, "No Name");
            insertNewSetting(lastSyncTime, "2018-01-01 01:01:01.000000000");
            insertNewSetting(language, "en");
            insertNewSetting(sound, "1");
        }
    }

    static private void insertNewSetting(String newSettingKey, String newSettingValue) {
        String sql = "INSERT INTO settings (" + settingKey + "," + settingValue + ") VALUES(?,?)";
        DbUtils.updateWithTwoParam(sql, newSettingKey, newSettingValue);
    }

    static private Integer getIntegerSetting(String settingName) {
        Integer integerSetting;
        String query = "SELECT " + settingValue + " FROM settings WHERE " + settingKey + "=?";
        String stringSetting = DbUtils.getStringValueWithOneParam(query, settingName);
        try {
            integerSetting = Integer.valueOf(stringSetting);
        } catch (NumberFormatException e) {
            integerSetting = -1;
            e.printStackTrace();
        }
        return integerSetting;
    }

    static private String getStringSetting(String settingName) {
        String query = "SELECT " + settingValue + " FROM settings WHERE " + settingKey + "=?";
        String setting = DbUtils.getStringValueWithOneParam(query, settingName);
        return setting;
    }

    static public Integer getNearbyMode() {
        return getIntegerSetting(nearbyModeKey);
    }

    static public Integer getCorrectionMode() {
        return getIntegerSetting(correctionModeKey);
    }

    static public Integer getForceSync() {
        return getIntegerSetting(forcedSyncKey);
    }

    static public Integer getUIMode() {
        return getIntegerSetting(uiModeKey);
    }

    static public String getTeacherName() {
        return getStringSetting(teacherNameKey);
    }

    static public String getLastSyncTime() {
        return getStringSetting(lastSyncTime);
    }

    static public Timestamp getLastSyncAsTimestamp() {
        String lastUpdTime = DbTableSettings.getLastSyncTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(lastUpdTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp lastSyncTime = new java.sql.Timestamp(parsedDate.getTime());
        return lastSyncTime;
    }

    static public ArrayList<String> getHomeworkKeys() {
        String sql = "SELECT " + settingValue + " FROM settings WHERE " + settingKey + "=?";
        return DbUtils.getArrayStringWithOneParam(sql, homeworkKey);
    }

    static public String getLanguage() {
        return getStringSetting(language);
    }

    static public int getSound() {
        if (currentSoundValue == -1) {
            currentSoundValue = getIntegerSetting(sound);
        }
        return currentSoundValue;
    }

    static public void insertNearbyMode(Integer nearbyMode) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey + "=?";
        DbUtils.updateWithTwoParam(sql, nearbyMode.toString(), nearbyModeKey);
    }

    static public void insertHomeworkKey(String hwKey) {
        insertNewSetting(homeworkKey, hwKey);
    }

    static public void insertUIMode(Integer UIMode) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey + "=?";
        DbUtils.updateWithTwoParam(sql, UIMode.toString(), uiModeKey);
    }

    static public void insertCorrectionMode(Integer correctionMode) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey + "=?";
        DbUtils.updateWithTwoParam(sql, correctionMode.toString(), correctionModeKey);
    }

    static public void insertForceSync(Integer forceSync) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey + "=?";
        DbUtils.updateWithTwoParam(sql, forceSync.toString(), forcedSyncKey);
    }

    static public void insertTeacherName(String name) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey + "=?";
        DbUtils.updateWithTwoParam(sql, name, teacherNameKey);
    }

    static public void insertSyncTime(String paramLastSyncTime) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey + "=?";
        DbUtils.updateWithTwoParam(sql, paramLastSyncTime, lastSyncTime);
    }

    public static void insertLanguage(String languageCode) {
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey +"=?";
        DbUtils.updateWithTwoParam(sql, languageCode, language);
    }

    public static void insertSound(Integer soundValue) {
        currentSoundValue = soundValue;
        String sql = "UPDATE settings SET " + settingValue + " = ? WHERE " + settingKey +"=?";
        DbUtils.updateWithTwoParam(sql, soundValue.toString(), sound);
    }
}
