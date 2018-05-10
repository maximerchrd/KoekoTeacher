package collect;

import java.sql.Timestamp;

public class Professor {
    private String _id_prof;
    private String _alias;
    private String _muid;
    private Timestamp _updateTS;

    private Professor() {
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
        return _updateTS;
    }

    public void set_timestamp(Timestamp _updateTS) {
        this._updateTS = _updateTS;
    }


}
