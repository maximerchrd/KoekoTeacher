package koeko.view;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Utilities {
    static public Map<String, String> codeToLanguageMap = new LinkedHashMap<>();
    static public Map<String, String> languageToCodeMap = new LinkedHashMap<>();

    //private static Connection connection;
    private static HikariDataSource hikariDataSource = null;

    public static Connection getDbConnection() throws Exception {
//        if(connection == null){
//            Class.forName("org.sqlite.JDBC");
//            connection = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
//        } else if (connection.isClosed()) {
//            connection = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
//        }
        Connection connection;
        if (hikariDataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:learning_tracker.db");
            config.setPoolName("learning_tracker_pool");
            config.setMaximumPoolSize(1);
            config.setMinimumIdle(2);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 256);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            hikariDataSource = new HikariDataSource(config);
            connection = hikariDataSource.getConnection();
        } else {
            connection = hikariDataSource.getConnection();
        }
//        Class.forName("org.sqlite.JDBC");
//        Connection connection = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
        return connection;
    }

    public static void initCodeToLanguageMap() {
        Utilities.codeToLanguageMap.put("en", "English");
        Utilities.codeToLanguageMap.put("de", "Deutsch");
        Utilities.codeToLanguageMap.put("fr", "Français");
    }

    public static void initLanguageToCodeMap() {
        Utilities.languageToCodeMap.put("English", "en");
        Utilities.languageToCodeMap.put("Deutsch", "de");
        Utilities.languageToCodeMap.put("Français", "fr");
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

    public static Timestamp StringToTimestamp(String timestampString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(timestampString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        return timestamp;
    }

    public static String localUniqueID() {
        Long time = System.nanoTime();
        String uniqueId = String.valueOf(time);
        if (uniqueId.length() > 14) {
            uniqueId = uniqueId.substring(0, 14);
        }
        return uniqueId;
    }

    static public String setPositiveIdSign(String id) {
        if (id.contains("--")){
            id = id.replace("--","");
        } else if (id.contains("-")) {
            id = id.replace("-", "");
        }
        return id;
    }
}
