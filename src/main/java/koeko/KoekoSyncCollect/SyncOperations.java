package koeko.KoekoSyncCollect;

import koeko.IniFile;
import koeko.database_management.*;
import koeko.view.Professor;
import koeko.view.Subject;
import koeko.view.Utilities;
import koeko.view.RelationQuestionSubject;
import koeko.view.QuestionMultipleChoiceView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;

public class SyncOperations {
    static String connectionString = "jdbc:mysql://localhost/koeko_collect?";
    static String userName = "testuser";
    static String userPass = "mysqltest99**";
    //static String connectionString = "jdbc:mysql://188.226.155.245/koeko_collect?user=koeko_testClient&password=ko&KOwird34jolI";
//    static String connectionString = "jdbc:mysql://188.226.155.245/koeko_collect?";
//    static String userName = "root";
//    static String userPass = "henearkr";

    static private TCPCommunication _tcpcom;

    static private void InitializeTransfer(InetAddress serverAddress, int serverPort) throws Exception {
        Socket socket = new Socket(serverAddress, serverPort);

        IniFile ini = new IniFile("./src/main/java/koeko/koeko.ini");
        String imagePath = ini.getString("File", "SourceImagePath", ".\\");
        _tcpcom = new TCPCommunication(socket, imagePath);
    }

    static public void SyncAll(InetAddress serverAddress, int serverPort) throws Exception {
        // Create the connection to the server for synchronisation
        InitializeTransfer(serverAddress, serverPort);

<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
        // Before synchronisation, make sure the prof is known in the global database
        Professor professor = DbTableProfessor.getProfessor();
        CreateOrUpdateProfessor(professor);

        // First step, launch sp to copy selection from web to koeko
        boolean bOK = _tcpcom.GetSelectionFromWEB(professor.get_muid());

        // Second step, upload data to koeko

        Vector<Subject> sbjVector = DbTableSubject.getSubjects();
        Enumeration en = sbjVector.elements();
        while(en.hasMoreElements()) {
            Subject sbj = (Subject) en.nextElement();
            CreateOrUpdateSubject(sbj);
        }

        Vector<QuestionMultipleChoiceView> qcmVector = DbTableQuestionMultipleChoice.getQuestionsMultipleChoiceView();
        en = qcmVector.elements();
        while(en.hasMoreElements()) {
            QuestionMultipleChoiceView qcm = (QuestionMultipleChoiceView) en.nextElement();
            CreateOrUpdateQuestionMultipleChoice(qcm, professor.get_muid());

            // Get the subjects linked to the question
            Vector<RelationQuestionSubject> subjectsLinked = DbTableRelationQuestionSubject.getSubjectsForQuestion(qcm.getID());
            if (!subjectsLinked.isEmpty())
                UpdateSubjectQuestionRelation(qcm.getQCM_MUID(), subjectsLinked);
        }

        // Third step, launch sp to update web with new data
        _tcpcom.SyncCollect2WEB();
        System.out.println("WEB synchronized");

        // Fourth step, download selected items to local DB
        _tcpcom.DownloadSelection();
        System.out.println("Selection downloaded");

        // Termine le processus de synchronisation avec le serveur
        _tcpcom.EndSynchronisation();
        System.out.println("Ending synchronization");

        // Note la dernière synchro
        DBTableSyncOp.SetLastSyncOp(Utilities.TimestampForNowAsString());
        System.out.println("Marking sync time");
    }

<<<<<<< Updated upstream
/*
=======
>>>>>>> Stashed changes

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
*/

    static private void CreateOrUpdateProfessor(Professor prof) {
        try {
            String muid = _tcpcom.SendSerializableObject(prof);
            if (muid != null) {
                prof.set_muid(muid);
                DbTableProfessor.setProfessorMUID(prof.get_id_prof(), muid);
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static private void CreateOrUpdateQuestionMultipleChoice(QuestionMultipleChoiceView qcm, String ownerMUID) {
        try {
            if (!qcm.getIMAGE().equals("none")) {
                boolean bOK = _tcpcom.SendFile(qcm.getIMAGE());
                if (!bOK) throw new Exception("File upload failed!");
            }
            String muid = _tcpcom.SendSerializableObject(qcm);
            if (muid != null) {
                qcm.setQCM_MUID(muid);
                DbTableQuestionMultipleChoice.setQuestionMultipleChoiceMUID(qcm.getID(), muid);
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static private void CreateOrUpdateSubject(Subject sbj) {
        try {
            String muid = _tcpcom.SendSerializableObject(sbj);
            if (muid != null) {
                sbj.set_subjectMUID(muid);
                DbTableSubject.setSubjectMUID(sbj.get_subjectId(), muid);
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static private void UpdateSubjectQuestionRelation(String questionMUID, Vector<RelationQuestionSubject> subjectsLinked) {
        boolean bOK = _tcpcom.RemoveSubjectRelation(questionMUID);
        if (bOK) {
            try {
                Enumeration en = subjectsLinked.elements();
                while(en.hasMoreElements()) {
                    RelationQuestionSubject rqs = (RelationQuestionSubject) en.nextElement();
                    rqs.set_questionMUID(questionMUID);
                    String muid = _tcpcom.SendSerializableObject(rqs);
                }
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }

    }

}
