package koeko.KoekoSyncCollect;

import koeko.view.Professor;
import koeko.view.Subject;
import koeko.view.RelationQuestionSubject;
import koeko.database_management.DbTableProfessor;
import koeko.database_management.DbTableQuestionMultipleChoice;
import koeko.database_management.DbTableRelationQuestionSubject;
import koeko.database_management.DbTableSubject;
import koeko.questions_management.QuestionMultipleChoice;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;

public class SyncOperations {
    static String connectionString = "jdbc:mysql://localhost/koeko_collect?";
    static String userName = "testuser";
    static String userPass = "mysqltest99**";
    //static String connectionString = "jdbc:mysql://188.226.155.245/koeko_collect?user=koeko_testClient&password=ko&KOwird34jolI";
    //static String connectionString = "jdbc:mysql://188.226.155.245/koeko_collect?user=root&password=henearkr";

    static public void SyncAll() throws Exception {
        // Check if connection works, exit if not
        CheckMySQLConnection();

        // Before synchronisation, make sure the prof is known in the global database
        Professor professor = DbTableProfessor.getProfessor();
        CreateOrUpdateProfessor(professor);

        // First step, launch sp to copy selection from web to koeko
        GetSelectionFromWEB(professor.get_muid());

        // Second step, upload data to koeko

        Vector<Subject> sbjVector = DbTableSubject.getSubjects();
        Enumeration en = sbjVector.elements();
        while(en.hasMoreElements()) {
            Subject sbj = (Subject) en.nextElement();
            CreateOrUpdateSubject(sbj);
        }

        Vector<QuestionMultipleChoice> qcmVector = DbTableQuestionMultipleChoice.getQuestionsMultipleChoice();
        en = qcmVector.elements();
        while(en.hasMoreElements()) {
            QuestionMultipleChoice qcm = (QuestionMultipleChoice) en.nextElement();
            CreateOrUpdateQuestionMultipleChoice(qcm, professor.get_muid());

            // Get the subjects linked to the question
            Vector<RelationQuestionSubject> subjectsLinked = DbTableRelationQuestionSubject.getSubjectsForQuestion(qcm.getID());
            UpdateSubjectQuestionRelation(qcm.getQCM_MUID(), subjectsLinked);
        }

        // Third step, launch sp to update web with new data
        SyncCollect2WEB();

        // Fourth step, download selected items to local DB


    }


    // Check
    private static void CheckMySQLConnection() {
        // Connexion à mysql
        Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", userPass);
                properties.setProperty("useSSL", "false");
        properties.setProperty("autoReconnect", "true");

        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            Connection connect = null;
            connect = DriverManager.getConnection(connectionString, properties);
            connect.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    private static Connection ConnectToMySQL() {
        // Connexion à mysql
        Properties properties = new Properties();
        properties.setProperty("user", userName);
        properties.setProperty("password", userPass);
        properties.setProperty("useSSL", "false");
        properties.setProperty("autoReconnect", "true");

        Connection connect = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager.getConnection(connectionString, properties);
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
            stmt =  c.prepareCall("{CALL spSetQuestionMultipleChoice(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
            stmt.setInt(1, Integer.parseInt(qcm.getLEVEL()));
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
            stmt.setInt(13, qcm.getNB_CORRECT_ANS());
            stmt.setString(14, qcm.getIMAGE());
            stmt.setString(15, qcm.getQCM_MUID());
            stmt.setString(16, ownerMUID);
            stmt.setTimestamp(17, qcm.getQCM_UPD_TMS());
            stmt.execute();
            String muid = stmt.getString(15);
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


    static private void CreateOrUpdateSubject(Subject sbj) {
        Connection c = null;
        CallableStatement stmt = null;
        stmt = null;
        try {
            c = ConnectToMySQL();
            c.setAutoCommit(false);
            stmt =  c.prepareCall("{CALL spSetSubject(?, ?, ?)}");
            stmt.setString(1, sbj.get_subjectName());
            stmt.setString(2, sbj.get_subjectMUID());
            stmt.setTimestamp(3, sbj.get_sbjUpdDts());
            stmt.execute();
            String muid = stmt.getString(2);
            if (muid != null) {
                sbj.set_subjectMUID(muid);
                DbTableSubject.setSubjectMUID(sbj.get_subjectId(), muid);
            }
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


    static private void UpdateSubjectQuestionRelation(String questionMUID, Vector<RelationQuestionSubject> subjectsLinked) {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            c = ConnectToMySQL();
            c.setAutoCommit(false);

            // Delete all relations to subjects for this question

            Statement delStmt = c.createStatement();
            String sql = "DELETE FROM subject_question_relation WHERE SQR_QUE_MUID='" + questionMUID + "';";
            delStmt.executeUpdate(sql);
            delStmt.close();

            Enumeration en = subjectsLinked.elements();
            while(en.hasMoreElements()) {
                RelationQuestionSubject rqs = (RelationQuestionSubject) en.nextElement();
                String sqlIns = "INSERT INTO koeko_collect.subject_question_relation (SQR_SBJ_MUID, SQR_QUE_MUID, SQR_QUE_TYP, SQR_LEVEL) VALUE ('" +
                                rqs.get_subjectMUID() + "','" + questionMUID + "','MCQ'," + rqs.get_level() + ");";
                stmt =  c.createStatement();
                stmt.executeUpdate(sqlIns);
                stmt.close();
            }
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


    static private void SyncCollect2WEB() {
        Connection c = null;
        CallableStatement stmt = null;
        stmt = null;
        try {
            c = ConnectToMySQL();
            c.setAutoCommit(false);
            stmt =  c.prepareCall("{CALL koeko_collect.spSyncToWEB()}");
            stmt.execute();
            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

}
