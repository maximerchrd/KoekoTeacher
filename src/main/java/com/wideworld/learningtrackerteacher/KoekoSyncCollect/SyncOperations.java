package com.wideworld.learningtrackerteacher.KoekoSyncCollect;


import com.wideworld.learningtrackerteacher.database_management.DbTableProfessor;
import com.wideworld.learningtrackerteacher.database_management.DbTableQuestionMultipleChoice;
import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;
import com.wideworld.learningtrackerteacher.students_management.Professor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Vector;
import java.util.Enumeration;

public class SyncOperations {

    static public void SyncAll() throws Exception {
        // Check if connection works, exit if not
        CheckMySQLConnection();

        // Before synchronisation, make sure the prof is known in the global database
        Professor professor = DbTableProfessor.getProfessor();
        CreateOrUpdateProfessor(professor);

        // First step, launch sp to copy selection from web to collect
        GetSelectionFromWEB(professor.get_muid());

        // Second step, upload data to collect
        Vector<QuestionMultipleChoice> qcmVector = DbTableQuestionMultipleChoice.getQuestionsMultipleChoice();
        Enumeration en = qcmVector.elements();
        while(en.hasMoreElements()) {
            QuestionMultipleChoice qcm = (QuestionMultipleChoice) en.nextElement();
            CreateOrUpdateQuestionMultipleChoice(qcm, professor.get_muid());
        }

        // Third step, launch sp to update web with new data

        // Fourth step, download selected items to local DB

    }


    // Check
    private static void CheckMySQLConnection() {
        // Connexion à mysql

        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            Connection connect = null;
            connect = DriverManager.getConnection("jdbc:mysql://localhost/global_collect?"
                    + "user=testuser&password=mysqltest99**");
            connect.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    private static Connection ConnectToMySQL() {
        // Connexion à mysql

        Connection connect = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager.getConnection("jdbc:mysql://localhost/global_collect?"
                    + "user=testuser&password=mysqltest99**");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return connect;
    }

    static private void CreateOrUpdateProfessor(Professor prof) {
        Connection c = null;
        CallableStatement stmt = null;
        stmt = null;
        try {
            c = ConnectToMySQL();
            c.setAutoCommit(false);
            stmt =  c.prepareCall("{CALL spSetProfessor(?, ?, ?)}");
            stmt.setString(1, prof.get_alias());
            stmt.setString(2, prof.get_muid());
            stmt.setTimestamp(3, prof.get_timestamp());
            stmt.execute();
            String muid = stmt.getString(2);
            if (muid != null) {
                prof.set_muid(muid);
                DbTableProfessor.setProfessorMUID(prof.get_id_prof(), muid);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


    static private void GetSelectionFromWEB(String profMuid) {
        Connection c = null;
        CallableStatement stmt = null;
        stmt = null;
        try {
            c = ConnectToMySQL();
            c.setAutoCommit(false);
            stmt =  c.prepareCall("{CALL spGetSelectionFromWEB(?)}");
            stmt.setString(1,profMuid);
            stmt.execute();
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


    static private void CreateOrUpdateQuestionMultipleChoice(QuestionMultipleChoice qcm, String ownerMUID) {
        Connection c = null;
        CallableStatement stmt = null;
        stmt = null;
        try {
            c = ConnectToMySQL();
            c.setAutoCommit(false);
            stmt =  c.prepareCall("{CALL spSetQuestionMultipleChoice(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            stmt.setString(1, qcm.getLEVEL());
            stmt.setString(2, qcm.getQUESTION());
            stmt.setString(3, qcm.getOPT0());
            stmt.setString(4, qcm.getOPT1());
            stmt.setString(5, qcm.getOPT2());
            stmt.setString(6, qcm.getOPT3());
            stmt.setString(7, qcm.getOPT4());
            stmt.setString(8, qcm.getOPT5());
            stmt.setString(9, qcm.getOPT6());
            stmt.setString(10, qcm.getOPT7());
            stmt.setString(11, qcm.getOPT8());
            stmt.setString(12, qcm.getOPT9());
            stmt.setInt(23, qcm.getNB_CORRECT_ANS());
            stmt.setString(24, qcm.getIMAGE());
            stmt.setString(25, qcm.getQCM_MUID());
            stmt.setString(26, ownerMUID);
            stmt.setTimestamp(27, qcm.getQCM_UPD_TMS());
            stmt.execute();
            String muid = stmt.getString(25);
            if (muid != null) {
                qcm.setQCM_MUID(muid);
                DbTableQuestionMultipleChoice.setQuestionMultipleChoiceMUID(qcm.getID(), muid);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


}
