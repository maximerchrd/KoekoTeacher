package koeko.view;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class Utilities {
    static public Map<String, String> codeToLanguageMap = new LinkedHashMap<>();
    static public Map<String, String> languageToCodeMap = new LinkedHashMap<>();

    private static Connection connection;

    public static Connection getDbConnection() throws Exception {
        if(connection == null){
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
        } else if (connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
        }
        return connection;
    }

    public static void initCodeToLanguageMap() {
        Utilities.codeToLanguageMap.put("eng", "English");
        Utilities.codeToLanguageMap.put("deu", "Deutsch");
        Utilities.codeToLanguageMap.put("fra", "Français");
    }

    public static void initLanguageToCodeMap() {
        Utilities.languageToCodeMap.put("English", "eng");
        Utilities.languageToCodeMap.put("Deutsch", "deu");
        Utilities.languageToCodeMap.put("Français", "fra");
    }


    // Prepare string for the insert in database
    public static String StringToSQL(String sData) {
        String sResult = sData.replace("'", "''");
        return sResult;
    }

    public static String TimestampForNowAsString() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        return zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public static Timestamp TimestampForNow() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        String sdt = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        return Timestamp.valueOf(sdt);
    }

    public static String localUniqueID() {
        Long time = System.nanoTime();
        String uniqueId = String.valueOf(time);
        if (uniqueId.length() > 14) {
            uniqueId = uniqueId.substring(0, 14);
        }
        return uniqueId;
    }
}
