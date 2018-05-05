package com.wideworld.learningtrackerteacher.controllers;

public class Professor {
    private String _id_prof;
    private String _firstname;
    private String _lastname;
    private String _muid;

    private Professor() {
    }

    public static Professor createProfessor(String id, String firstname, String lastname, String muid) {
        Professor prof = new Professor();
        prof._id_prof = id;
        prof._firstname = firstname;
        prof._lastname = lastname;
        prof._muid = muid;
        return prof;
    }

    public String get_id_prof() {
        return _id_prof;
    }

    public void set_id_prof(String _id_prof) {
        this._id_prof = _id_prof;
    }

    public String get_firstname() {
        return _firstname;
    }

    public void set_firstname(String _firstname) {
        this._firstname = _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public void set_lastname(String _lastname) {
        this._lastname = _lastname;
    }

    public String get_muid() {
        return _muid;
    }

    public void set_muid(String _muid) {
        this._muid = _muid;
    }


}
