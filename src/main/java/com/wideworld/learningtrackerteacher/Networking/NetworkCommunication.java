package com.wideworld.learningtrackerteacher.Networking;

import com.wideworld.learningtrackerteacher.LearningTracker;
import com.wideworld.learningtrackerteacher.controllers.*;
import com.wideworld.learningtrackerteacher.database_management.*;
import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;
import com.wideworld.learningtrackerteacher.questions_management.QuestionShortAnswer;
import com.wideworld.learningtrackerteacher.questions_management.Test;
import com.wideworld.learningtrackerteacher.students_management.Classroom;
import com.wideworld.learningtrackerteacher.students_management.Student;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by maximerichard on 03/02/17.
 */
public class NetworkCommunication {
    static public NetworkCommunication networkCommunicationSingleton;
    private LearningTrackerController learningTrackerController = null;

    private FileInputStream fis = null;
    private BufferedInputStream bis = null;
    private OutputStream serverOutStream = null;
    private ArrayList<OutputStream> outstream_list;
    private ArrayList<InputStream> instream_list;
    private int number_of_clients = 0;
    private Classroom aClass = null;
    private int network_solution = 0; //0: all devices connected to same wifi router
    final private int PORTNUMBER = 9090;

    private ArrayList<ArrayList<Integer>> questionIdsForGroups;
    private ArrayList<ArrayList<String>> studentNamesForGroups;


    public NetworkCommunication(LearningTrackerController learningTrackerController) {
        this.learningTrackerController = learningTrackerController;
        networkCommunicationSingleton = this;
        questionIdsForGroups = new ArrayList<>();
        studentNamesForGroups = new ArrayList<>();
    }

    public NetworkCommunication() {
        networkCommunicationSingleton = this;
    }

    public LearningTrackerController getLearningTrackerController() {
        return learningTrackerController;
    }

    /**
     * starts the bluetooth server
     *
     * @throws IOException
     */
    public void startServer() throws IOException {

        if (network_solution == 0) {
            // First we create a server socket and bind it to port 9090.
            ServerSocket myServerSocket = new ServerSocket(PORTNUMBER);


            // wait for an incoming connection...
            /*System.out.println("Server is waiting for an incoming connection on host="
                    + InetAddress.getLocalHost().getHostAddress() + "; "
                    + InetAddress.getLocalHost().getCanonicalHostName() + "; "
                    + InetAddress.getLocalHost().getHostName()
                    + " port=" + myServerSocket.getLocalPort());*/


            //Wait for client connection
            System.out.println("\nServer Started. Waiting for clients to connect...");
            outstream_list = new ArrayList<OutputStream>();
            instream_list = new ArrayList<InputStream>();
            aClass = new Classroom();
            Thread connectionthread = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            //listening to client connection and accept it
                            Socket skt = myServerSocket.accept();
                            Student student = new Student();
                            student.setInetAddress(skt.getInetAddress());
                            System.out.println("Student with address: " + student.getInetAddress() + " accepted. Waiting for next client to connect");

                            try {
                                //register student
                                number_of_clients++;
                                student.setInputStream(skt.getInputStream());
                                student.setOutputStream(skt.getOutputStream());
                                if (!aClass.studentAlreadyInClass(student)) {
                                    aClass.addStudentIfNotInClass(student);
                                    System.out.println("aClass.size() = " + aClass.getClassSize() + " adding student: " + student.getInetAddress().toString());
                                    if (SettingsController.nearbyMode == 0) {
                                        SendNewConnectionResponse(student.getOutputStream(), 0);
                                    } else if (SettingsController.nearbyMode == 1) {
                                        // WARNING: smaller than 1 because the connection string is not yet received.
                                        // If the protocol is changed, this MUST BE modified as well
                                        if (aClass.getNbAndroidDevices() < 1) {
                                            SendNewConnectionResponse(student.getOutputStream(), 1);
                                        } else {
                                            SendNewConnectionResponse(student.getOutputStream(), 2);
                                        }
                                    }

                                } else {
                                    student.setInputStream(skt.getInputStream());
                                    student.setOutputStream(skt.getOutputStream());
                                    aClass.updateStudent(student);

                                    if (SettingsController.nearbyMode == 0) {
                                        SendNewConnectionResponse(student.getOutputStream(), 0);
                                    } else if (SettingsController.nearbyMode == 1) {
                                        // WARNING: smaller than 1 because the connection string is not yet received.
                                        // If the protocol is changed, this MUST BE modified as well
                                        if (aClass.getNbAndroidDevices() < 1) {
                                            SendNewConnectionResponse(student.getOutputStream(), 1);
                                        } else {
                                            SendNewConnectionResponse(student.getOutputStream(), 2);
                                        }
                                    }
                                }

                                //start a new thread for listening to each student
                                listenForClient(aClass.getStudents_array().get(aClass.indexOfStudentWithAddress(student.getInetAddress().toString())));

                                //send the active questions
                                ArrayList<Integer> activeIDs = (ArrayList<Integer>) LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().clone();
                                for (Iterator<Integer> iterator = activeIDs.iterator(); iterator.hasNext();) {
                                    if (iterator.next() < 0) {
                                        iterator.remove();
                                    }
                                }
                                if (activeIDs.size() > 0) {
                                    try {
                                        sendMultipleChoiceWithID(activeIDs.get(0), student.getOutputStream());
                                        sendShortAnswerQuestionWithID(activeIDs.get(0), student.getOutputStream());
                                        System.out.println("address: " + student.getInetAddress());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e2) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                        }

                    }
                }
            };
            connectionthread.start();
        }
    }

    public void SendQuestionID(int QuestID) throws IOException {
        String questIDString = "QID:MLT///" + String.valueOf(QuestID) + "///" + String.valueOf(SettingsController.correctionMode);
        byte[] bytearraystring = questIDString.getBytes(Charset.forName("UTF-8"));
        ArrayList<Student> StudentsArray = aClass.getStudents_array();
        System.out.println("sending id: " + questIDString);
        System.out.println("to " + StudentsArray.size() + " students");
        for (int i = 0; i < StudentsArray.size(); i++) {
            OutputStream tempOutputStream = StudentsArray.get(i).getOutputStream();
            try {
                tempOutputStream.write(bytearraystring, 0, bytearraystring.length);
                tempOutputStream.flush();
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    public void SendQuestionID(int QuestID, Student student) {
        student = aClass.getStudentWithName(student.getName());
        try {
            SendQuestionID(QuestID, student.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SendQuestionID(int QuestID, OutputStream singleStudentOutputStream) throws IOException {
        if (singleStudentOutputStream != null) {
            String questIDString = "QID:MLT///" + String.valueOf(QuestID) + "///";
            byte[] bytearraystring = questIDString.getBytes(Charset.forName("UTF-8"));
            System.out.println("sending question: " + questIDString + " to single student");
            try {
                singleStudentOutputStream.write(bytearraystring, 0, bytearraystring.length);
                singleStudentOutputStream.flush();
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    public void SendQuestionIDs(ArrayList<Integer> QuestID, OutputStream singleStudentOutputStream) throws IOException {
        if (singleStudentOutputStream != null) {
            byte[] bytearray = DataConversion.questionsSetToBytesArray(QuestID);
            try {
                singleStudentOutputStream.write(bytearray, 0, bytearray.length);
                singleStudentOutputStream.flush();
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    public void sendMultipleChoiceWithID(int questionID, OutputStream singleStudentOutputStream) throws IOException {
        QuestionSendingController.readyToActivate = false;
        QuestionMultipleChoice questionMultipleChoice = null;
        try {
            questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (questionMultipleChoice.getQUESTION().length() > 0) {
            String question_text = questionMultipleChoice.getQUESTION() + "///";
            question_text += questionMultipleChoice.getOPT0() + "///";
            question_text += questionMultipleChoice.getOPT1() + "///";
            question_text += questionMultipleChoice.getOPT2() + "///";
            question_text += questionMultipleChoice.getOPT3() + "///";
            question_text += questionMultipleChoice.getOPT4() + "///";
            question_text += questionMultipleChoice.getOPT5() + "///";
            question_text += questionMultipleChoice.getOPT6() + "///";
            question_text += questionMultipleChoice.getOPT7() + "///";
            question_text += questionMultipleChoice.getOPT8() + "///";
            question_text += questionMultipleChoice.getOPT9() + "///";
            question_text += questionMultipleChoice.getID() + "///";
            question_text += questionMultipleChoice.getNB_CORRECT_ANS() + "///";
            Vector<String> subjectsVector = DbTableSubject.getSubjectsForQuestionID(questionID);
            int l = 0;
            for (l = 0; l < subjectsVector.size(); l++) {
                question_text += subjectsVector.get(l) + "|||";
            }
            if (l == 0) question_text += " ";
            question_text += "///";
            Vector<String> objectivesVector = DbTableLearningObjectives.getObjectiveForQuestionID(questionMultipleChoice.getID());
            for (l = 0; l < objectivesVector.size(); l++) {
                question_text += objectivesVector.get(l) + "|||";
            }
            if (l == 0) question_text += " ";
            question_text += "///";

            // send file : the sizes of the file and of the text are given in the first 40 bytes (separated by ":")
            int intfileLength = 0;
            File myFile = new File(questionMultipleChoice.getIMAGE());
            if (!questionMultipleChoice.getIMAGE().equals("none") && myFile.exists() && !myFile.isDirectory()) {
                question_text += questionMultipleChoice.getIMAGE().split("/")[questionMultipleChoice.getIMAGE().split("/").length - 1];
                intfileLength = (int) myFile.length();
            } else {
                question_text += questionMultipleChoice.getIMAGE() + "///";
            }

            //writing of the first 40 bytes
            byte[] bytearraytext = question_text.getBytes(Charset.forName("UTF-8"));
            int textbyteslength = bytearraytext.length;
            byte[] bytearray = new byte[40 + textbyteslength + intfileLength];
            String fileLength;
            fileLength = "MULTQ";
            fileLength += ":" + String.valueOf(intfileLength);
            fileLength += ":" + String.valueOf(textbyteslength) + ":";
            System.out.println("fileLength: " + fileLength);
            byte[] bytearraystring = fileLength.getBytes(Charset.forName("UTF-8"));
            for (int k = 0; k < bytearraystring.length; k++) {
                bytearray[k] = bytearraystring[k];
            }

            //copy the textbytes into the array which will be sent
            for (int k = 0; k < bytearraytext.length; k++) {
                bytearray[k + 40] = bytearraytext[k];
            }

            //write the file into the bytearray   !!! tested up to 630000 bytes, does not work with file of 4,7MB
            if (!questionMultipleChoice.getIMAGE().equals("none") && myFile.exists() && !myFile.isDirectory()) {
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(bytearray, 40 + textbyteslength, intfileLength);
            }
            System.out.println("Sending " + questionMultipleChoice.getIMAGE() + "(" + intfileLength + " bytes)");
            int arraylength = bytearray.length;
            System.out.println("Sending " + arraylength + " bytes in total");
            if (singleStudentOutputStream == null) {
                for (int i = 0; i < aClass.getClassSize(); i++) {
                    OutputStream tempOutputStream = aClass.getStudents_array().get(i).getOutputStream();
                    try {
                        tempOutputStream.write(bytearray, 0, arraylength);
                        tempOutputStream.flush();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            } else {
                try {
                    singleStudentOutputStream.write(bytearray, 0, arraylength);
                    singleStudentOutputStream.flush();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }

            System.out.println("Done.");
        }
    }

    public void sendShortAnswerQuestionWithID(int questionID, OutputStream singleStudentOutputStream) throws IOException {
        QuestionSendingController.readyToActivate = false;
        QuestionShortAnswer questionShortAnswer = null;
        try {
            questionShortAnswer = DbTableQuestionShortAnswer.getShortAnswerQuestionWithId(questionID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (questionShortAnswer.getQUESTION().length() > 0) {
            String question_text = questionShortAnswer.getQUESTION() + "///";
            question_text += questionShortAnswer.getID() + "///";

            //add answers
            ArrayList<String> answersArray = questionShortAnswer.getANSWER();
            for (int i = 0; i < answersArray.size(); i++) {
                question_text += answersArray.get(i) + "|||";
            }
            if (answersArray.size() == 0) question_text += " ";
            question_text += "///";

            //add subjects
            Vector<String> subjectsVector = DbTableSubject.getSubjectsForQuestionID(questionID);
            int l = 0;
            for (l = 0; l < subjectsVector.size(); l++) {
                question_text += subjectsVector.get(l) + "|||";
            }
            if (l == 0) question_text += " ";
            question_text += "///";

            //add objectives
            Vector<String> objectivesVector = DbTableLearningObjectives.getObjectiveForQuestionID(questionShortAnswer.getID());
            for (l = 0; l < objectivesVector.size(); l++) {
                question_text += objectivesVector.get(l) + "|||";
            }
            if (l == 0) question_text += " ";
            question_text += "///";

            // send file : the sizes of the file and of the text are given in the first 40 bytes (separated by ":")
            int intfileLength = 0;
            File myFile = new File(questionShortAnswer.getIMAGE());
            ;
            if (!questionShortAnswer.getIMAGE().equals("none") && myFile.exists() && !myFile.isDirectory()) {
                question_text += questionShortAnswer.getIMAGE().split("/")[questionShortAnswer.getIMAGE().split("/").length - 1];
                intfileLength = (int) myFile.length();
            } else {
                question_text += questionShortAnswer.getIMAGE() + "///";
            }

            //writing of the first 40 bytes
            byte[] bytearraytext = question_text.getBytes(Charset.forName("UTF-8"));
            int textbyteslength = bytearraytext.length;
            byte[] bytearray = new byte[40 + textbyteslength + intfileLength];
            String fileLength;
            fileLength = "SHRTA";
            fileLength += ":" + String.valueOf(intfileLength);
            fileLength += ":" + String.valueOf(textbyteslength) + ":";
            byte[] bytearraystring = fileLength.getBytes(Charset.forName("UTF-8"));
            for (int k = 0; k < bytearraystring.length; k++) {
                bytearray[k] = bytearraystring[k];
            }

            //copy the textbytes into the array which will be sent
            for (int k = 0; k < bytearraytext.length; k++) {
                bytearray[k + 40] = bytearraytext[k];
            }

            //write the file into the bytearray   !!! tested up to 630000 bytes, does not work with file of 4,7MB
            if (!questionShortAnswer.getIMAGE().equals("none") && myFile.exists() && !myFile.isDirectory()) {
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(bytearray, 40 + textbyteslength, intfileLength);
            }
            System.out.println("Sending " + questionShortAnswer.getIMAGE() + "(" + intfileLength + " bytes)");
            int arraylength = bytearray.length;
            System.out.println("Sending " + arraylength + " bytes in total");
            if (singleStudentOutputStream == null) {
                for (int i = 0; i < aClass.getClassSize(); i++) {
                    OutputStream tempOutputStream = aClass.getStudents_array().get(i).getOutputStream();
                    try {
                        tempOutputStream.write(bytearray, 0, arraylength);
                        tempOutputStream.flush();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            } else {
                try {
                    singleStudentOutputStream.write(bytearray, 0, arraylength);
                    singleStudentOutputStream.flush();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }

            System.out.println("Done.");
        }
    }

    public void sendTestWithID(int testID, OutputStream singleStudentOutputStream) throws IOException {
        Test testToSend = new Test();
        try {
            testToSend = DbTableTests.getTestWithID(testID);
            byte [] bytesArray = DataConversion.testToBytesArray(testToSend);
            if (singleStudentOutputStream == null) {
                for (int i = 0; i < aClass.getClassSize(); i++) {
                    OutputStream tempOutputStream = aClass.getStudents_array().get(i).getOutputStream();
                    try {
                        tempOutputStream.write(bytesArray, 0, bytesArray.length);
                        tempOutputStream.flush();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            } else {
                try {
                    singleStudentOutputStream.write(bytesArray, 0, bytesArray.length);
                    singleStudentOutputStream.flush();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method that listen for the client data transfers
     *
     * @throws IOException
     */
    private void listenForClient(final Student arg_student) throws IOException {
        //for (int i = 0; i < aClass.getClassSize(); i++) {
        final InputStream answerInStream = arg_student.getInputStream();
        Thread listeningthread = new Thread() {
            public void run() {
                int bytesread = 0;
                while (bytesread >= 0) {
                    try {
                        byte[] in_bytearray = new byte[1000];
                        bytesread = answerInStream.read(in_bytearray);
                        System.out.println(bytesread + " bytes read");
                        if (bytesread >= 1000) System.out.println("Answer too large for bytearray");
                        if (bytesread >= 0) {
                            String answerString = new String(in_bytearray, 0, bytesread, "UTF-8");
                            System.out.println("received answer: " + answerString);
                            if (answerString.split("///")[0].contains("ANSW")) {
                                //arg_student.setName(answerString.split("///")[2]);
                                double eval = DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(Integer.valueOf(answerString.split("///")[5]),
                                        answerString.split("///")[2], answerString.split("///")[3], answerString.split("///")[0]);
                                SendEvaluation(eval, Integer.valueOf(answerString.split("///")[5]), arg_student);

                                //find out to which group the student and answer belong
                                Integer groupIndex = 0;
                                Integer questID = Integer.valueOf(answerString.split("///")[5]);
                                for (int i = 0; i < studentNamesForGroups.size(); i++) {
                                    if (studentNamesForGroups.get(i).contains(arg_student.getName()) && questionIdsForGroups.get(i).contains(questID)) {
                                        groupIndex = i;
                                        questionIdsForGroups.get(i).remove(questID);
                                    }
                                }
                                learningTrackerController.addAnswerForUser(arg_student, answerString.split("///")[3], answerString.split("///")[4], eval,
                                        Integer.valueOf(answerString.split("///")[5]), groupIndex);
                                Integer nextQuestion = arg_student.getNextQuestionID(Integer.valueOf(answerString.split("///")[5]));
                                System.out.println("student: " + arg_student.getName() + ";former question: " + questID + "; nextQuestion:" + nextQuestion);
                                for (Integer testid : arg_student.getTestQuestions()) {
                                    System.out.println(testid);
                                }
                                if (nextQuestion != -1) {
                                    SendQuestionID(nextQuestion, arg_student.getOutputStream());
                                }

                                //set evaluation if question belongs to a test
                                if (arg_student.getActiveTest().getIdsQuestions().contains(questID)) {
                                    System.out.println("inserting question evaluation for test");
                                    int questionIndex = arg_student.getActiveTest().getIdsQuestions().indexOf(questID);
                                    if (questionIndex < arg_student.getActiveTest().getQuestionsEvaluations().size() && questionIndex >= 0) {
                                        arg_student.getActiveTest().getQuestionsEvaluations().set(questionIndex,eval);
                                    }
                                    Boolean testCompleted = true;
                                    for (Double questEval : arg_student.getActiveTest().getQuestionsEvaluations()) {
                                        if (questEval < 0) {
                                            testCompleted = false;
                                        }
                                    }
                                    if (testCompleted) {
                                        Double testEval = 0.0;
                                        for (Double questEval : arg_student.getActiveTest().getQuestionsEvaluations()) {
                                            testEval += questEval;
                                        }
                                        testEval = testEval /  arg_student.getActiveTest().getQuestionsEvaluations().size();
                                        arg_student.getActiveTest().setTestEvaluation(testEval);
                                        DbTableIndividualQuestionForStudentResult.addIndividualTestEval(arg_student.getActiveTest().getIdTest(),arg_student.getName(),testEval);
                                    }
                                }
                            } else if (answerString.split("///")[0].contains("CONN")) {
                                Student student = arg_student;
                                student.setAddress(answerString.split("///")[1]);
                                student.setName(answerString.split("///")[2]);
                                Integer studentID = DbTableStudents.addStudent(answerString.split("///")[1], answerString.split("///")[2]);
                                if (studentID == -2) {
                                    popUpIfStudentIdentifierCollision(student.getName());
                                }
                                student.setStudentID(studentID);

                                //update the tracking of questions on device



                                learningTrackerController.addUser(student, true);
                                aClass.updateStudent(student);
                                if (aClass.studentAlreadyInClass(student) && answerString.contains("Android")) {
                                    aClass.setNbAndroidDevices(aClass.getNbAndroidDevices() + 1);
                                    System.out.println("Increasing the number of connected android devices");
                                }
                                aClass.getStudentsPath().put(student.getInetAddress().toString(),student.getOutputStream());
                            } else if (answerString.split("///")[0].contains("DISC")) {
                                Student student = new Student(answerString.split("///")[1], answerString.split("///")[2]);
                                learningTrackerController.userDisconnected(student);
                                if (answerString.contains("Android")) {
                                    aClass.setNbAndroidDevices(aClass.getNbAndroidDevices() - 1);
                                }
                            } else if (answerString.split("///")[0].contains("GOTIT")) {
                                String questionID = answerString.split("///")[1];
                                System.out.println("client received question: " + questionID);
                                if (LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().contains(Integer.valueOf(questionID))) {
                                    int IDindex = LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().indexOf(Integer.valueOf(questionID));
                                    if (LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().size() > IDindex + 1) {
                                        sendMultipleChoiceWithID(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(IDindex + 1), arg_student.getOutputStream());
                                        sendShortAnswerQuestionWithID(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(IDindex + 1), arg_student.getOutputStream());
                                    }
                                    //add the ID to the ID list for the student inside the class
                                    LearningTracker.studentGroupsAndClass.get(0).getStudentWithName(arg_student.getName()).getDeviceQuestions().add(questionID);
                                    System.out.println("transfer finished? " + LearningTracker.studentGroupsAndClass.get(0).allQuestionsOnDevices());
                                    if (LearningTracker.studentGroupsAndClass.get(0).allQuestionsOnDevices()) {
                                        QuestionSendingController.readyToActivate = true;
                                    }
                                }
                            } else if (answerString.split("///")[0].contains("FORWARD")) {
                                if (answerString.split("///").length > 3) {
                                    answerString = answerString.substring(answerString.lastIndexOf(answerString.split("///")[1]) + 3);
                                    if (answerString.split("///")[1].contains("CONN")) {
                                        ReceptionProtocol.receivedCONN(arg_student,answerString,aClass);
                                    }
                                } else {
                                    System.out.println("Problem reading forwarded string: truncated");
                                }
                            }
                        } else {

                        }
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        if (e1.toString().contains("Connection reset")) {
                            bytesread = -1;
                        }
                    }
                }
            }
        };
        listeningthread.start();
        //}
    }

    private void SendEvaluation(double evaluation, int questionID, Student student) {
        String evalToSend = "EVAL///" + evaluation + "///" + questionID + "///";
        System.out.println("sending: " + evalToSend);
        byte[] bytes = new byte[40];
        int bytes_length = 0;
        try {
            bytes_length = evalToSend.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < bytes_length; i++) {
            try {
                bytes[i] = evalToSend.getBytes("UTF-8")[i];
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            student.getOutputStream().write(bytes, 0, bytes.length);
            student.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void UpdateEvaluation(double evaluation, Integer questionID, Integer studentID) {
        Student student = aClass.getStudentWithID(studentID);
        String evalToSend = "UPDEV///" + evaluation + "///" + questionID + "///";
        System.out.println("sending: " + evalToSend);
        byte[] bytes = new byte[40];
        int bytes_length = 0;
        try {
            bytes_length = evalToSend.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < bytes_length; i++) {
            try {
                bytes[i] = evalToSend.getBytes("UTF-8")[i];
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            student.getOutputStream().write(bytes, 0, bytes.length);
            student.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SendCorrection(Integer questionID) {
        String messageToSend = "CORR///";
        messageToSend += String.valueOf(questionID) + "///";
        byte[] bytes = new byte[40];
        int bytes_length = 0;
        try {
            bytes_length = messageToSend.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < bytes_length; i++) {
            try {
                bytes[i] = messageToSend.getBytes("UTF-8")[i];
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            for (Student student : aClass.getStudents_array()) {
                student.getOutputStream().write(bytes, 0, bytes.length);
                student.getOutputStream().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Informations for error; studentGroupsAndClass stored in class object:");
            for (Student student : aClass.getStudents_array()) {
                System.out.println("name: " + student.getName() + "; ip: " + student.getAddress());
            }
        }
    }

    private void SendNewConnectionResponse(OutputStream arg_outputStream, Integer nearbyMode) throws IOException {
        String response;
        if (nearbyMode == 1) {
            response = "SERVR///ADVER///";
        } else if (nearbyMode == 2) {
            response = "SERVR///DISCV///";
        } else {
            response = "SERVR///NONEA///";
        }
        byte[] bytes = new byte[40];
        int bytes_length = response.getBytes("UTF-8").length;
        for (int i = 0; i < bytes_length; i++) {
            bytes[i] = response.getBytes("UTF-8")[i];
        }
        arg_outputStream.write(bytes, 0, bytes.length);
        arg_outputStream.flush();
        //serverOutStream.write(bytes, 0, bytes.length);
        //serverOutStream.flush();
    }

    public Classroom getClassroom() {
        return aClass;
    }

    public void removeQuestion(int index) {
        learningTrackerController.removeQuestion(index);
    }

    public void addQuestion(String question, Integer ID, Integer group) {
        learningTrackerController.addQuestion(question, ID, group);
    }

    public void activateTest(ArrayList<Integer> questionIds, Integer testID) {
        if (questionIds.size() > 0) {
            try {
                SendQuestionID(questionIds.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Student student : aClass.getStudents_array()) {
            student.setTestQuestions((ArrayList<Integer>) questionIds.clone());
            Test studentTest = new Test();
            studentTest.setIdTest(testID);
            studentTest.setIdsQuestions((ArrayList<Integer>) questionIds.clone());
            for (Integer ignored : questionIds) {
                studentTest.getQuestionsEvaluations().add(-1.0);
            }
            student.setActiveTest(studentTest);
        }
    }

    public void activateTestForGroup(ArrayList<Integer> questionIds, ArrayList<String> students, Integer testID) {

        //first reinitialize if groups array are same size as number of groups (meaning we are in a new groups session)
        if (questionIdsForGroups.size() == LearningTracker.studentGroupsAndClass.size() - 1) {
            questionIdsForGroups.clear();
            studentNamesForGroups.clear();
        }
        //add ids(clone it because we want to remove its content later without affecting the source array) and students to group arrays
        questionIdsForGroups.add(new ArrayList<>());
        for (Integer id : questionIds) {
            questionIdsForGroups.get(questionIdsForGroups.size() - 1).add(id);
        }
        studentNamesForGroups.add(students);

        for (String studentName : students) {
            Student student = aClass.getStudentWithName(studentName);
            if (questionIds.size() > 0) {
                try {
                    //get the first question ID which doesn't correspond to a test
                    int i = 0;
                    for (; i < questionIds.size() && questionIds.get(i) < 0; i++) {}
                    SendQuestionID(questionIds.get(i), student.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            student.setTestQuestions((ArrayList<Integer>) questionIds.clone());

            //following code not used for now
            if (testID != 0) {
                Test studentTest = new Test();
                studentTest.setIdTest(testID);
                studentTest.setIdsQuestions((ArrayList<Integer>) questionIds.clone());
                for (Integer ignored : questionIds) {
                    studentTest.getQuestionsEvaluations().add(-1.0);
                }
                student.setActiveTest(studentTest);
            }
        }
    }

    public void activateTestSynchroneousQuestions(ArrayList<Integer> questionIds, ArrayList<String> students, Integer testID) {

        if (students.size() == 0) {
            ArrayList<Student> studentsArray = aClass.getStudents_array();
            for (Student std : studentsArray) {
                students.add(std.getName());
            }
        }

        //first reinitialize if groups array are same size as number of groups (meaning we are in a new groups session)
        /*if (questionIdsForGroups.size() == LearningTracker.studentGroupsAndClass.size() - 1) {
            questionIdsForGroups.clear();
            studentNamesForGroups.clear();
        }
        //add question IDs(clone it because we want to remove its content later without affecting the source array) and students to group arrays
        questionIdsForGroups.add(new ArrayList<>());
        for (Integer id : questionIds) {
            questionIdsForGroups.get(questionIdsForGroups.size() - 1).add(id);
        }
        studentNamesForGroups.add(students);*/

        for (String studentName : students) {
            Student student = aClass.getStudentWithName(studentName);
            if (questionIds.size() > 0) {
                try {
                    SendQuestionIDs(questionIds, student.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            student.setTestQuestions((ArrayList<Integer>) questionIds.clone());
            Test studentTest = new Test();
            studentTest.setSynchroneousQuestionsTest(true);
            studentTest.setIdTest(testID);
            studentTest.setIdsQuestions((ArrayList<Integer>) questionIds.clone());
            for (Integer ignored : questionIds) {
                studentTest.getQuestionsEvaluations().add(-1.0);
            }
            student.setActiveTest(studentTest);
        }
    }


    public void popUpIfStudentIdentifierCollision( String studentName) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(LearningTracker.studentsVsQuestionsTableControllerSingleton);
                VBox dialogVbox = new VBox(20);
                dialogVbox.getChildren().add(new Text(studentName + " is trying to connect but has a different " +
                        "device identifier \n than the student with the same name already registered."));
                Scene dialogScene = new Scene(dialogVbox, 400, 40);
                dialog.setScene(dialogScene);
                dialog.show();
                }
        });
    }
}
