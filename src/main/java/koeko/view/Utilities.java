package koeko.view;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utilities {

    public static String TimestampForNowAsString() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        return zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"));
    }

    public static Timestamp TimestampForNow() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
        String sdt = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"));
        return Timestamp.valueOf(sdt);
    }
}
