package koeko.view;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Professor implements Serializable {
    private String _id_prof;
    private String _alias;
    private String _muid;
    private Timestamp _updateTS;
    private String _language;
    private String _synchronizationKey = "";

    public Professor() {
    }

    public static Professor createProfessor(String id, String alias, String muid) {
        Professor prof = new Professor();
        prof._id_prof = id;
        prof._alias = alias;
        prof._muid = muid;
        return prof;
    }

    public String get_id_prof() {
        return _id_prof;
    }

    public void set_id_prof(String _id_prof) {
        this._id_prof = _id_prof;
    }

    public String get_alias() {
        return _alias;
    }

    public void set_alias(String _alias) {
        this._alias = _alias;
    }

    public String get_muid() {
        return _muid;
    }

    public void set_muid(String _muid) {
        this._muid = _muid;
    }

    public Timestamp get_timestamp() {
        // ZonedDateTime zdt;
        //String sdt;
        if (_updateTS == null) {
            ZonedDateTime zdt = ZonedDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("UTC"));
            String sdt = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"));
            _updateTS = Timestamp.valueOf(sdt);
        }
        return _updateTS;
    }

    public void set_timestamp(Timestamp _updateTS) {
        this._updateTS = _updateTS;
    }

    public String get_language() {
        return _language;
    }

    public void set_language(String _language) {
        this._language = _language;
    }

    public String get_synchronizationKey() {
        return _synchronizationKey;
    }

    public void set_synchronizationKey(String _synchronizationKey) {
        this._synchronizationKey = _synchronizationKey;
    }
}
