package koeko.KoekoSyncCollect;

import koeko.IniFile;
import koeko.Koeko;
import koeko.database_management.*;
import koeko.view.*;

import java.net.InetAddress;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
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

    static public void SyncAll(InetAddress serverAddress, int serverPort, Boolean reDownload) throws Exception {
        // Create the connection to the server for synchronisation
        InitializeTransfer(serverAddress, serverPort);

        // Before synchronisation, make sure the prof is known in the global database
        Professor professor = DbTableProfessor.getProfessor();
        if (professor == null) {
            Koeko.leftBarController.promptGenericPopUp("Enter your pseudo before synchronizing.", "Create teacher");
        } else {
            CreateOrUpdateProfessor(professor);

            // FIRST STEP, launch sp to copy selection from web to koeko
            boolean bOK = _tcpcom.GetSelectionFromWEB(professor.get_muid(), reDownload);

            // SECOND STEP, upload data to koeko

            //Send the subjects
            Vector<Subject> sbjVector = DbTableSubject.getSubjects();
            //add language to subjects
            for (Subject subject : sbjVector) {
                subject.set_subjectLanguage(professor.get_language());
            }

            Enumeration en = sbjVector.elements();
            while (en.hasMoreElements()) {
                Subject sbj = (Subject) en.nextElement();
                CreateOrUpdateSubject(sbj);
            }

            //Send the objectives
            Vector<Objective> objectives = DbTableLearningObjectives.getObjectives(professor.get_language());
            en = objectives.elements();
            while (en.hasMoreElements()) {
                Objective objective = (Objective) en.nextElement();
                CreateOrUpdateObjective(objective);
            }

            Vector<QuestionView> qcmVector = DbTableQuestionMultipleChoice.getQuestionsMultipleChoiceView();
            qcmVector.addAll(DbTableQuestionShortAnswer.getQuestionViews());
            qcmVector.addAll(DbTableTest.getAllTestViews());
            en = qcmVector.elements();
            while (en.hasMoreElements()) {
                QuestionView qcm = (QuestionView) en.nextElement();
                qcm.setLANGUAGE(professor.get_language());
                CreateOrUpdateQuestionMultipleChoice(qcm, professor.get_muid());

                //Get the question relations if we are uploading a test
                ArrayList<RelationQuestionQuestion> relationQuestionQuestions = DbTableRelationQuestionQuestion.getQuestionsRelationsLinkedToTest(qcm.getQCM_MUID());
                if (!relationQuestionQuestions.isEmpty()) {
                    UpdateQuestionQuestionRelation(qcm.getQCM_MUID(), relationQuestionQuestions);
                }

                // Get the subjects linked to the question
                Vector<RelationQuestionSubject> subjectsLinked = DbTableRelationQuestionSubject.getSubjectsForQuestion(qcm.getID());
                if (!subjectsLinked.isEmpty()) {
                    UpdateSubjectQuestionRelation(qcm.getQCM_MUID(), subjectsLinked);
                }

                Vector<RelationQuestionObjective> objectivesLinked = DbTableRelationQuestionObjective.getObjectivesLinkedToQuestion(qcm.getID());
                if (!objectivesLinked.isEmpty()) {
                    UpdateObjectiveQuestionRelation(qcm.getQCM_MUID(), objectivesLinked);
                }
            }

            //Send homeworks
            ArrayList<Homework> homeworks = DbTableHomework.getHomeworksForSyncing();
            for (Homework homework : homeworks) {
                String uid = _tcpcom.SendSerializableObject(homework);
                if (uid != null && uid.length() == 15 && !uid.contentEquals(homework.getUid())) {
                    DbTableHomework.setHomeworkUID(uid, homework.getName());
                }
            }

            //Send the logs
            ArrayList<Log> logsToSend = DbTableLogs.getLogs(0);
            for (Log logToSend : logsToSend) {
                SendLog(logToSend);
            }

            // THIRD STEP, launch sp to update web with new data
            _tcpcom.SyncCollect2WEB();
            System.out.println("WEB synchronized");

            // FOURTH STEP, download selected items to local DB
            _tcpcom.DownloadSelection();
            System.out.println("Selection downloaded");

            // End syncing processus
            _tcpcom.EndSynchronisation();
            System.out.println("Ending synchronization");

            // Take note of last sync
            DbTableSettings.insertSyncTime(Utilities.TimestampForNowAsString());
            System.out.println("Marking sync time");
        }
    }

    static public void RequestNewHomeworkKey(InetAddress serverAddress, int serverPort) throws Exception {
        // Create the connection to the server for synchronisation
        InitializeTransfer(serverAddress, serverPort);

        Professor professor = DbTableProfessor.getProfessor();
        if (professor == null) {
            Koeko.leftBarController.promptGenericPopUp("Enter your pseudo before synchronizing.", "Create teacher");
        } else {
            boolean bOK = _tcpcom.RequestHomeworkKey(professor.get_muid());
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
            e.printStackTrace();
        }
        return connect;
    }

    static private void CreateOrUpdateProfessor(Professor prof) {
        try {
            String muid = _tcpcom.SendSerializableObject(prof);
            if (muid != null) {
                prof.set_muid(muid);
                DbTableProfessor.setProfessorMUID(prof.get_id_prof(), muid);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    static private void CreateOrUpdateQuestionMultipleChoice(QuestionView qcm, String ownerMUID) {
        try {
            if (qcm.getIMAGE() != null) {
                if (!qcm.getIMAGE().equals("none")) {
                    boolean bOK = _tcpcom.SendFile(qcm.getIMAGE());
                    //TODO catch this exception
                    if (!bOK) throw new Exception("File upload failed!");
                }
            } else {
                qcm.setIMAGE("none");
            }
            String muid = _tcpcom.SendSerializableObject(qcm);
            if (muid != null) {
                qcm.setQCM_MUID(muid);
                DbTableQuestionMultipleChoice.setResourceMUID(qcm.getID(), muid);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            }
        }
    }

    static private void CreateOrUpdateObjective(Objective objective) {
        try {
            String muid = _tcpcom.SendSerializableObject(objective);
            if (muid != null) {
                objective.set_objectiveMUID(muid);
                DbTableLearningObjectives.setObjectiveUID(objective.get_objectiveName(), muid);
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    static private void UpdateObjectiveQuestionRelation(String questionMUID, Vector<RelationQuestionObjective> objectivesLinked) {
        boolean bOK = _tcpcom.RemoveObjectiveRelation(questionMUID);
        if (bOK) {
            try {
                Enumeration en = objectivesLinked.elements();
                while(en.hasMoreElements()) {
                    RelationQuestionObjective rqo = (RelationQuestionObjective) en.nextElement();
                    rqo.set_questionMUID(questionMUID);
                    String muid = _tcpcom.SendSerializableObject(rqo);
                }
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
        }
    }

    static private void UpdateQuestionQuestionRelation(String testUID, ArrayList<RelationQuestionQuestion> relationQuestionQuestions) {
        boolean bOK = _tcpcom.RemoveTestRelation(testUID);
        if (bOK) {
            try {
                for (RelationQuestionQuestion relationQuestionQuestion : relationQuestionQuestions) {
                    _tcpcom.SendSerializableObject(relationQuestionQuestion);
                }
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
        }
    }

    private static void SendLog(Log logToSend) {
        try {
            String objectReceived = _tcpcom.SendSerializableObject(logToSend);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

}
