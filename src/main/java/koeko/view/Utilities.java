package koeko.view;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utilities {

    // Prepare string for the insert in database
    public static String StringToSQL(String sData) {
        String sResult = sData.replace("'", "''");
        return sResult;
    }

    public static String TimestampForNowAsString() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        return zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"));
    }

    public static Timestamp TimestampForNow() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        String sdt = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"));
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
