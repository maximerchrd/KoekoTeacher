package koeko.Networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import koeko.Koeko;
import koeko.Networking.OtherTransferables.*;
import koeko.Tools.FilesHandler;
import koeko.controllers.Game.Game;
import koeko.controllers.Game.GameView;
import koeko.controllers.Game.StudentCellView;
import koeko.controllers.LearningTrackerController;
import koeko.controllers.SettingsController;
import koeko.database_management.*;
import koeko.functionalTesting;
import koeko.questions_management.*;
import koeko.students_management.Classroom;
import koeko.students_management.Student;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import koeko.view.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by maximerichard on 03/02/17.
 */
public class NetworkCommunication {
    static public NetworkCommunication networkCommunicationSingleton;
    static final public int prefixSize = 80;
    private LearningTrackerController learningTrackerController = null;
    public Classroom aClass = null;

    static public int network_solution = 0; //0: all devices connected to same wifi router; 1: 3 layers with nearby connections
    private int nextHotspotNumber = 1;

    private FileInputStream fis = null;
    private BufferedInputStream bis = null;
    final private int PORTNUMBER = 9090;
    private Vector<String> disconnectiongStudents;
    private ArrayList<ArrayList<String>> questionIdsForGroups;
    private ArrayList<ArrayList<String>> studentNamesForGroups;
    private NetworkState networkStateSingleton;
    private BlockingQueue<PayloadForSending> sendingQueue;
    private AtomicBoolean queueIsFinished;
    private PrintWriter writer;

    //For speed testing
    static public Long sendingStartTime = 0L;
    static public int fileLength = 0;


    public NetworkCommunication(LearningTrackerController learningTrackerController) {
        this.learningTrackerController = learningTrackerController;
        networkCommunicationSingleton = this;
        questionIdsForGroups = new ArrayList<>();
        studentNamesForGroups = new ArrayList<>();
        disconnectiongStudents = new Vector<>();
        networkStateSingleton = new NetworkState();
        this.sendingQueue = new LinkedBlockingQueue<>();
        queueIsFinished = new AtomicBoolean(true);
        try {
            writer = new PrintWriter("test_output.txt", "UTF-8");
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
            writer.println(timeStamp + "\t" + "start");
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public LearningTrackerController getLearningTrackerController() {
        return learningTrackerController;
    }

    public NetworkState getNetworkStateSingleton() {
        return networkStateSingleton;
    }

    /**
     * starts the server
     *
     * @throws IOException
     */
    public void startServer() throws IOException {


        //Send the ip of the computer to everyone on the subnet
        Thread sendIPthread = new Thread(() -> {
            while (true) {
                try {
                    sendIpAddress();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sendIPthread.start();


        // we create a server socket and bind it to port 9090.
        ServerSocket myServerSocket = new ServerSocket(PORTNUMBER);

        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        aClass = Koeko.studentGroupsAndClass.get(0);
        Thread connectionthread = new Thread(() -> {
            while (true) {
                try {
                    //listening to client connection and accept it
                    Socket skt = myServerSocket.accept();
                    Student student = new Student();
                    student.setInetAddress(skt.getInetAddress());
                    student.setPort(String.valueOf(skt.getPort()));
                    System.out.println("Student with address: " + student.getInetAddress() + " accepted. Waiting for next client to connect");

                    try {
                        //register student
                        student.setInputStream(skt.getInputStream());
                        student.setOutputStream(skt.getOutputStream());
                        if (!aClass.studentAlreadyInClass(student)) {
                            aClass.addStudentIfNotInClass(student);
                            System.out.println("aClass.size() = " + aClass.getClassSize() + ". Added student: " + student.getInetAddress().toString());
                        } else {
                            student = aClass.updateStudentStreams(student);
                        }

                        //start a new thread for listening to each student
                        listenForClient(aClass.getStudents().get(aClass.indexOfStudentWithAddress(student.getInetAddress().toString())));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
            }
        });
        connectionthread.start();
    }

    public void sendQuestionID(String QuestID, ArrayList<Student> students) {
        System.out.println("Sending question to " + students.size() + " students");
        //if question ID is negative (=test), change its sign
        try {
            if (Long.valueOf(QuestID) < 0) {
                QuestionGeneric.changeIdSign(QuestID);
            }
        } catch (NumberFormatException e) {
            System.err.println("sendQuestionID(): number format exception while sending " + QuestID);
        }
        //set active id for network state
        networkStateSingleton.setActiveID(QuestID);

        for (int i = 0; i < students.size(); i++) {
            students.set(i, aClass.getStudentWithName(students.get(i).getName()));
        }


        ArrayList<OutputStream> handledOutputStreams = new ArrayList<>();

        for (Student student : students) {
            if (student.getOutputStream() != null) {
                if (!handledOutputStreams.contains(student.getOutputStream())) {
                    QuestionIdentifier questionIdentifier = new QuestionIdentifier();
                    questionIdentifier.setPrefix(TransferPrefix.stateUpdate);
                    questionIdentifier.setIdentifier(QuestID);
                    questionIdentifier.setCorrectionMode(SettingsController.correctionMode);
                    try {
                        sendTransferableObjectWithoutId(questionIdentifier, student);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //if Nearby activated (layers), try to prevent sending twice to same outputStream
                    if (NetworkCommunication.network_solution == 1 && !handledOutputStreams.contains(student.getOutputStream())) {
                        handledOutputStreams.add(student.getOutputStream());
                    }
                }
            } else {
                System.out.println("Problem sending ID: outputstream is null; probably didnt receive acknowledgment of receipt on time");
            }
        }
    }

    public void sendMultipleChoiceWithID(String questionID, Student student) {
        QuestionView questionMultipleChoice = DbTableQuestionMultipleChoice.getQuestionMultipleChoiceView(questionID);
        if (questionMultipleChoice.getQUESTION().length() > 0) {
            sendTransferableObjectWithId(questionMultipleChoice, student, questionID);
            sendSubjectsAndObjectsWithQuestionId(questionID, student);
        } else {
            System.err.println("Sending empty question");
        }
    }

    public void sendShortAnswerQuestionWithID(String questionID, Student student) {
        QuestionView questionShortAnswer = DbTableQuestionShortAnswer.getQuestionViewWithId(questionID);
        if (questionShortAnswer.getQUESTION().length() > 0) {
            sendTransferableObjectWithId(questionShortAnswer, student, questionID);
            sendSubjectsAndObjectsWithQuestionId(questionID, student);
        } else {
            System.err.println("Sending empty question");
        }
    }

    private void sendSubjectsAndObjectsWithQuestionId(String questionId, Student student) {
        ArrayList<SubjectTransferable> subjects = DbTableSubject.getSubjectsObjectsForQuestionID(questionId);
        for (SubjectTransferable subject : subjects) {
            subject.setPrefix(TransferPrefix.resource);
            sendTransferableObjectWithId(subject, student, questionId);
        }
        ArrayList<ObjectiveTransferable> objectives = DbTableLearningObjectives.getObjectiveObjectsForQuestionID(questionId);
        for (ObjectiveTransferable objective : objectives) {
            objective.setPrefix(TransferPrefix.resource);
            sendTransferableObjectWithId(objective, student, questionId);
        }
    }

    public void sendTransferableObjectWithId(TransferableObject transferableObject, Student student, String objectId) {
        int intfileLength = 0;
        for (String file : transferableObject.getFiles()) {
            File imageFile = new File(FilesHandler.mediaDirectory + file);
            intfileLength = Long.valueOf(imageFile.length()).intValue();
            if (!file.equals("none") && imageFile.exists() && !imageFile.isDirectory()) {
                sendMediaFile(imageFile, student);
            }
        }

        //for testing
        if (functionalTesting.transferRateTesting) {
            NetworkCommunication.fileLength = intfileLength;
            NetworkCommunication.sendingStartTime = System.nanoTime();
        }

        writeToOutputStream(student, objectId, transferableObject.objectToByteArray());
        networkStateSingleton.getQuestionIdsToSend().add(objectId);
        sendOnlyId(student, objectId);
    }

    public void sendTransferableObjectWithoutId(TransferableObject transferableObject, Student student) throws IOException {
        for (String file : transferableObject.getFiles()) {
            int intfileLength = 0;
            File imageFile = new File(FilesHandler.mediaDirectory + file);
            if (!file.equals("none") && imageFile.exists() && !imageFile.isDirectory()) {
                sendMediaFile(imageFile, student);
            }
        }

        writeToOutputStream(student, transferableObject.objectToByteArray());
    }

    public ArrayList<String> sendTestWithID(String testID, Student student) {
        Test testToSend = new Test();
        try {
            testToSend = DbTableTest.getTestWithID(testID);
            testToSend.setMedalsInstructions(DbTableTest.getMedals(testToSend.getTestName()));
            sendTest(testToSend, student);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if the student is connecting (student!=null) then add the id to the deviceQuestions
        if (student != null) {
            if (Long.valueOf(testID) > 0) {
                testID = QuestionGeneric.changeIdSign(testID);
            }
            student.getDeviceQuestions().add(testID);
        }

        return testToSend.getIdsQuestions();
    }

    private void sendTest(Test test, Student student) throws IOException {
        TestView testView = new TestView();
        testView.setIdTest(test.getIdTest());
        testView.setTestName(test.getTestName());
        testView.setTestMode(test.getTestMode());

        String objectives = "";
        for (int i = 0; i < test.getObjectives().size() && i < test.getObjectivesIDs().size(); i++) {
            objectives += test.getObjectivesIDs().get(i) + "/|/";
            objectives += test.getObjectives().get(i) + "|||";
        }
        testView.setObjectives(objectives);
        testView.setMedalInstructions(test.getMedalsInstructions());
        //shorten media file name
        if (test.getSendMediaFile() == 1 && test.getMediaFileName() != null && test.getMediaFileName().length() > 14) {
            testView.setMediaFileName(test.getMediaFileName().substring(test.getMediaFileName().length() - 14, test.getMediaFileName().length()));
        }

        String testMap = DbTableRelationQuestionQuestion.getFormattedQuestionsLinkedToTest(test.getTestName());
        testView.setTestMap(testMap);
        testView.setUpdateTime(test.getUpdateTime());

        //insert the question ids into the test
        ArrayList<String> questionIDs = new ArrayList<>();
        String[] questionMapIDs = testMap.split("\\|\\|\\|");
        for (int i = 0; i < questionMapIDs.length; i++) {
            if (!questionMapIDs[i].contentEquals("")) {
                questionIDs.add(questionMapIDs[i].split(";;;")[0]);
            }
        }
        test.setIdsQuestions(questionIDs);

        sendTransferableObjectWithoutId(testView, student);
    }

    public void sendTestEvaluation(String studentName, String test, String objective, String evaluation) throws IOException {
        Student student = aClass.getStudentWithName(studentName);
        if (student.getOutputStream() != null) {
            Evaluation evaluationObject = new Evaluation();
            evaluationObject.setEvaluationType(EvaluationTypes.objectiveEvaluation);
            evaluationObject.setEvaluation(Double.parseDouble(evaluation));
            evaluationObject.setIdentifier(DbTableLearningObjectives.getObjectiveIdFromName(objective));
            evaluationObject.setName(objective);
            evaluationObject.setTestIdentifier(DbTableTest.getTestIdWithName(test));
            evaluationObject.setTestName(test);
            sendTransferableObjectWithoutId(evaluationObject, student);
        }
    }

    /**
     * method that listen for the client data transfers
     *
     * @throws IOException
     */
    private void listenForClient(final Student arg_student) {
        final InputStream answerInStream = arg_student.getInputStream();
        Thread listeningthread = new Thread(() -> {
            int bytesread = 0;
            Boolean ableToRead = true;
            while (bytesread >= 0 && ableToRead) {
                try {
                    byte[] in_bytearray = new byte[TransferPrefix.prefixSize];
                    bytesread = answerInStream.read(in_bytearray, 0, TransferPrefix.prefixSize);
                    if (bytesread >= 0) {
                        String answerString = new String(in_bytearray, 0, bytesread, "UTF-8");
                        System.out.println(arg_student.getName() + ":" + System.nanoTime() / 1000000000 + ":" + bytesread + " message:" + answerString);
                        ClientToServerTransferable transferablePrefix = ClientToServerTransferable.Companion.stringToTransferable(answerString);

                        switch (transferablePrefix.getPrefix()) {
                            case CtoSPrefix.connectionPrefix:
                                ReceptionProtocol.receivedCONN(arg_student, aClass, transferablePrefix, answerInStream);
                                break;
                            case CtoSPrefix.resourceIdsPrefix:
                                ReceptionProtocol.receivedRESIDS(transferablePrefix, answerInStream, networkStateSingleton, arg_student);
                                break;
                            case CtoSPrefix.unableToReadPrefix:
                                System.out.println("Communication over?");
                                break;
                            default:
                                System.err.println("Implement informing user that the app might need update");

                        }

                        if (answerString.split("///")[0].contains("ANSW")) {
                            //arg_student.setName(answerString.split("///")[2]);
                            double eval = DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(answerString.split("///")[5],
                                    arg_student.getStudentID(), answerString.split("///")[3], answerString.split("///")[0]);
                            sendEvaluation(eval, answerString.split("///")[5], arg_student);

                            //find out to which group the student and answer belong
                            Integer groupIndex = -1;
                            String questID = answerString.split("///")[5];
                            for (int i = 0; i < Koeko.studentGroupsAndClass.size(); i++) {
                                if (Koeko.studentGroupsAndClass.get(i).getOngoingQuestionsForStudent().get(arg_student.getName()) != null &&
                                        Koeko.studentGroupsAndClass.get(i).getOngoingQuestionsForStudent().get(arg_student.getName()).contains(String.valueOf(questID))) {
                                    groupIndex = i;
                                    Koeko.studentGroupsAndClass.get(i).getOngoingQuestionsForStudent().get(arg_student.getName())
                                            .remove(questID);
                                }
                            }

                            if (groupIndex == -1) {
                                groupIndex = 0;
                                for (int i = 0; i < studentNamesForGroups.size(); i++) {
                                    if (studentNamesForGroups.get(i).contains(arg_student.getName()) && questionIdsForGroups.get(i).contains(questID)) {
                                        groupIndex = i;
                                        questionIdsForGroups.get(i).remove(questID);
                                    }
                                }
                            }

                            Koeko.studentsVsQuestionsTableControllerSingleton.addAnswerForUser(answerString.split("///")[2], answerString.split("///")[3], answerString.split("///")[4], eval,
                                    answerString.split("///")[5], groupIndex);
                            String nextQuestion = arg_student.getNextQuestionID(answerString.split("///")[5]);
                            System.out.println("student: " + arg_student.getName() + ";former question: " + questID + "; nextQuestion:" + nextQuestion);
                            for (String testid : arg_student.getTestQuestions()) {
                                System.out.println(testid);
                            }
                            if (!nextQuestion.contentEquals("-1")) {
                                ArrayList<Student> singleStudent = new ArrayList<>();
                                singleStudent.add(arg_student);
                                sendQuestionID(nextQuestion, singleStudent);
                            }

                            //set evaluation if question belongs to a test
                            if (arg_student.getActiveTest().getIdsQuestions().contains(questID)) {
                                System.out.println("inserting question evaluation for test");
                                int questionIndex = arg_student.getActiveTest().getIdsQuestions().indexOf(questID);
                                if (questionIndex < arg_student.getActiveTest().getQuestionsEvaluations().size() && questionIndex >= 0) {
                                    arg_student.getActiveTest().getQuestionsEvaluations().set(questionIndex, eval);
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
                                    testEval = testEval / arg_student.getActiveTest().getQuestionsEvaluations().size();
                                    arg_student.getActiveTest().setTestEvaluation(testEval);
                                    DbTableIndividualQuestionForStudentResult.addIndividualTestEval(arg_student.getActiveTest().getIdTest(), arg_student.getName(), testEval);
                                }
                            }

                            //increase score if a game is on
                            for (Game game : Koeko.activeGames) {
                                Koeko.gameControllerSingleton.scoreIncreased(eval, game, arg_student);
                            }
                        } else if (answerString.split("///")[0].contentEquals("CONN")) {
//                            Student student = ReceptionProtocol.receivedCONN(arg_student, answerString, aClass);
//
//                            //copy some basic informations because arg_student is used to write the answer into the table
//                            //Student.essentialCopyStudent(student, arg_student);
//
//                            //if RESIDS were merged with CONN
//                            if (answerString.contains("RESIDS")) {
//                                String substring = answerString.substring(answerString.indexOf("RESIDS"));
//                                ReceptionProtocol.receivedRESIDS(substring, networkStateSingleton, arg_student);
//                            }
                        } else if (answerString.split("///")[0].contains("RESIDS")) {
                            //ReceptionProtocol.receivedRESIDS(answerString, networkStateSingleton, arg_student);
                        } else if (answerString.split("///")[0].contains("DISC")) {
                            networkStateSingleton.disconnectDevice(answerString.split("///")[1]);
                            Student student = aClass.getStudentWithUniqueID(arg_student.getUniqueDeviceID());
                            student.setUniqueDeviceID(answerString.split("///")[1]);
                            student.setName(answerString.split("///")[2]);
                            student.setConnected(false);
                            if (answerString.contains("close-connection")) {
                                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getStudentsToConnectionStatus()
                                        .add(student.getUniqueDeviceID());
                                learningTrackerController.userDisconnected(student);

                                System.out.println("Student device really disconnecting. We should close the connection");
                                arg_student.getOutputStream().flush();
                                arg_student.getOutputStream().close();
                                arg_student.setOutputStream(null);
                                arg_student.getInputStream().close();
                                arg_student.setInputStream(null);
                                ableToRead = false;
                            } else if (answerString.contains("locked")) {
                                disconnectiongStudents.remove(student.getUniqueDeviceID());
                                arg_student.getOutputStream().flush();
                                arg_student.getOutputStream().close();
                                arg_student.setOutputStream(null);
                                arg_student.getInputStream().close();
                                arg_student.setInputStream(null);
                                ableToRead = false;
                            } else {
                                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getStudentsToConnectionStatus()
                                        .add(student.getUniqueDeviceID());
                                disconnectiongStudents.add(student.getUniqueDeviceID());
                                Thread studentDisconnectionQThread = new Thread() {
                                    public void run() {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        if (disconnectiongStudents.contains(student.getUniqueDeviceID())) {
                                            learningTrackerController.userDisconnected(student);
                                            disconnectiongStudents.remove(student.getUniqueDeviceID());
                                        }
                                    }
                                };
                                studentDisconnectionQThread.start();
                            }
                        } else if (answerString.split("///")[0].contains("OK")) {
                            String uuid = "";
                            if (answerString.split("///")[0].split(":").length > 1) {
                                uuid = answerString.split("///")[0].split(":")[1];
                            } else {
                                System.err.println("Received OK but no identifier associated!");
                            }
                            ArrayList<String> receptionArray = new ArrayList<String>(Arrays.asList(answerString.split("///")));
                            for (String receivedString : receptionArray) {
                                if (!receivedString.contains("OK:") && !uuid.contentEquals("no identifier")) {
                                    networkStateSingleton.getStudentsToSyncedIdsMap().get(uuid).add(receivedString);
                                    if (networkStateSingleton.getStudentsToSyncedIdsMap().get(uuid).containsAll(networkStateSingleton.getQuestionIdsToSend())) {
                                        Student student = aClass.getStudentWithUniqueID(uuid);
                                        if (student != null) {
                                            networkStateSingleton.toggleSyncStateForStudent(student, NetworkState.STUDENT_SYNCED);
                                        }
                                    }
                                }
                            }

                            //For sending speed testing, provided that we only send one question multiple choice
                            if (functionalTesting.transferRateTesting) {
                                Long sendingTime = System.nanoTime() - NetworkCommunication.sendingStartTime;
                                Double sendingTimeDouble = sendingTime / 1000000000.0;
                                System.out.println("Sending time: " + sendingTimeDouble + "; File size: " + NetworkCommunication.fileLength +
                                        "; Sending Speed: " + String.format("%.2f", NetworkCommunication.fileLength / sendingTimeDouble / 1000000)
                                        + "MB/s");
                            }
                        } else if (answerString.split("///")[0].contains("ACCUSERECEPTION")) {
                            int nbAccuses = answerString.split("ACCUSERECEPTION", -1).length - 1;
                            functionalTesting.nbAccuseReception += nbAccuses;
                            System.out.println(functionalTesting.nbAccuseReception);
                            if (functionalTesting.nbAccuseReception >= (functionalTesting.numberStudents * functionalTesting.numberOfQuestions)) {
                                functionalTesting.endTimeQuestionSending = System.currentTimeMillis();
                            }
                        } else if (answerString.split("///")[0].contains("ACTID")) {
                            if (answerString.split("///").length >= 2) {
                                String activeID = answerString.split("///")[1];
                                if (Long.valueOf(activeID) < 0) {
                                    activeID = QuestionGeneric.changeIdSign(activeID);
                                }
                                networkStateSingleton.getStudentsToActiveIdMap().put(arg_student.getUniqueDeviceID(), activeID);
                            }
                        } else if (answerString.contains("ENDTRSM")) {
                            sendActiveIds(arg_student);
                        } else if (answerString.split("///")[0].contentEquals("HOTSPOTIP")) {
                            for (SubNet subNet : networkStateSingleton.getSubNets()) {
                                if (subNet.getPassword().contentEquals(answerString.split("///")[2])) {
                                    subNet.setIpAddress(answerString.split("///")[1]);
                                }
                            }
                        } else if (answerString.split("///")[0].contentEquals("SUCCESS")) {
                            networkStateSingleton.subnetSuccess(answerString.split("///")[1]);
                            System.out.println("Received SUCCESS");
                        } else if (answerString.split("///")[0].contentEquals("FAIL")) {
                            networkStateSingleton.operationFailed(answerString.split("///")[1]);
                            System.out.println("Received FAIL");
                        } else if (answerString.split("///")[0].contentEquals("READY")) {
                            Koeko.gameControllerSingleton.studentReady(answerString.split("///")[1]);
                        } else if (answerString.split("///")[0].contentEquals("GAMESET")) {
                            ReceptionProtocol.receivedGAMESET(answerString.split("///")[2], arg_student);
                        } else if (answerString.split("///")[0].contentEquals("GAMETEAM")) {
                            Platform.runLater(() -> {
                                if (Koeko.gameControllerSingleton == null) {
                                    Koeko.questionSendingControllerSingleton.openGameController();
                                    Koeko.gameControllerSingleton.setQrMode();
                                }
                                Koeko.gameControllerSingleton.addPlayerFromQrCode(answerString.split("///")[1], answerString.split("///")[2], arg_student);
                            });
                        } else if (answerString.contains("RECONNECTED")) {
                            String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
                            writer.println(timeStamp + "\t" + answerString.split("///")[1]);
                            writer.flush();
                        } else if (answerString.split("///")[0].contentEquals("REQUEST")) {
                            sendResourceWithId(answerString.split("///")[2], answerString.split("///")[1]);
                        } else if (answerString.split("///")[0].contentEquals("RESULT")) {
                            ReceptionProtocol.receivedRESULT(answerString, answerInStream, arg_student.getStudentID());
                        }
                    } else {
                        System.out.println("Communication over?");
                    }
                } catch (SocketException sockex) {
                    if (sockex.toString().contains("timed out")) {
                        System.out.println("Socket exception: read timed out");
                    } else if (sockex.toString().contains("Connection reset")) {
                        System.out.println("Socket exception: connection reset");
                    } else {
                        System.out.println("Other Socket exception");
                    }
                    bytesread = -1;
                } catch (IOException e1) {
                    System.out.println("Some other IOException occured");
                    if (e1.toString().contains("Connection reset")) {
                        bytesread = -1;
                    }
                }
            }
            writer.close();
        });
        listeningthread.start();
    }

    private void sendResourceWithId(String resourceId, String studentId) {
        Student student = aClass.getStudentWithUniqueID(studentId);
        System.out.println("Resource requested: " + resourceId + ". Sending to: " + student.getName());
        try {
            Long longId = Long.valueOf(resourceId);
            if (longId < 0) {
                ArrayList<String> questionIds = sendTestWithID(Utilities.setPositiveIdSign(resourceId), student);
                for (String questionId : questionIds) {
                    QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(questionId);
                    if (questionMultipleChoice.getQUESTION().length() > 0) {
                        sendMultipleChoiceWithID(questionId, student);
                    } else {
                        sendShortAnswerQuestionWithID(questionId, student);
                    }
                }
                //Added delay to give enough time to put the questions in the queue before the ID
                for (int i = 0; i < 40 && !networkStateSingleton.getStudentsToSyncedIdsMap().get(studentId).containsAll(questionIds); i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                QuestionMultipleChoice questionMultipleChoice = DbTableQuestionMultipleChoice.getMultipleChoiceQuestionWithID(resourceId);
                if (questionMultipleChoice.getQUESTION().length() > 0) {
                    sendMultipleChoiceWithID(resourceId, student);
                } else {
                    sendShortAnswerQuestionWithID(resourceId, student);
                }
                //Added delay to give enough time to put the questions in the queue before the ID
                for (int i = 0; i < 20 && !networkStateSingleton.getStudentsToSyncedIdsMap().get(studentId).contains(resourceId); i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


            ArrayList<Student> singleStudent = new ArrayList<>();
            singleStudent.add(student);
            sendQuestionID(resourceId, singleStudent);
        } catch (NumberFormatException e) {
            System.err.println("Received request but resource id not in number format");
        }
    }

    public void sendMediaFile(File mediaFile, Student student) {
        try {
            byte[] fileData = Files.readAllBytes(mediaFile.toPath());

            //build info prefix
            String mediaName = mediaFile.getName();

            List<String> extensions = Arrays.asList(FilesHandler.supportedMediaExtensions);
            String[] extension = mediaName.split("\\.");
            if (extension.length > 1 && extensions.contains("*." + extension[1]) && mediaName.length() > 14) {
                mediaName = mediaName.substring(mediaName.length() - 14, mediaName.length());
            }

            //add the file to the sent objects (for sync check)
            networkStateSingleton.getQuestionIdsToSend().add(mediaName);
            sendOnlyId(student, mediaName);

            TransferableObject fileTransferable = new TransferableObject(TransferPrefix.file);
            fileTransferable.setObjectId(mediaName);
            fileTransferable.setFileBytes(fileData);
            sendTransferableObjectWithId(fileTransferable, student, fileTransferable.getObjectId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEvaluation(double evaluation, String questionID, Student student) throws IOException {
        Evaluation eval = new Evaluation();
        eval.setEvaluation(evaluation);
        eval.setIdentifier(questionID);
        sendTransferableObjectWithoutId(eval, student);

        /**
         * CODE USED FOR TESTING
         */
        if (functionalTesting.testMode) {
            functionalTesting.studentsNbEvalSent.put(student.getName(), functionalTesting.studentsNbEvalSent.get(student.getName()) + 1);
        }
    }

    public void sendGameScore(Student student, Double teamOneScore, Double teamTwoScore) throws IOException {
        ShortCommand shortCommand = new ShortCommand();
        shortCommand.setCommand(ShortCommands.gameScore);
        shortCommand.setOptionalArgument1(teamOneScore.toString());
        shortCommand.setOptionalArgument2(teamTwoScore.toString());
        sendTransferableObjectWithoutId(shortCommand, student);
    }

    public void updateEvaluation(double evaluation, String questionID, String studentID) throws IOException {
        Student student = aClass.getStudentWithID(studentID);
        Evaluation eval = new Evaluation();
        eval.setEvaluation(evaluation);
        eval.setIdentifier(questionID);
        eval.setEvalUpdate(true);
        sendTransferableObjectWithoutId(eval, student);
    }

    public void sendCorrection(String questionID) throws IOException {
        ShortCommand shortCommand = new ShortCommand();
        shortCommand.setCommand(ShortCommands.correction);
        shortCommand.setOptionalArgument1(questionID);
        shortCommand.setOptionalArgument2(UUID.randomUUID().toString().substring(0, 4));
        for (Student student : aClass.getStudents()) {
            sendTransferableObjectWithoutId(shortCommand, student);
        }
    }

    public Classroom getClassroom() {
        return aClass;
    }

    public void removeQuestion(int index) {
        learningTrackerController.removeQuestion(index);
    }

    public void addQuestion(String question, String ID, Integer group) {
        learningTrackerController.addQuestion(question, ID, group);
    }

    public Boolean checkIfQuestionsOnDevices() {
        Boolean questionsOnDevices = true;
        for (Map.Entry<String, CopyOnWriteArrayList<String>> entry : networkStateSingleton.getStudentsToSyncedIdsMap().entrySet()) {
            CopyOnWriteArrayList<String> questions = entry.getValue();
            if (!questions.containsAll(networkStateSingleton.getQuestionIdsToSend())) {
                questionsOnDevices = false;
                break;
            }

            //for debug
            System.out.println("Student device: " + entry.getKey());
            for (String question : questions) {
                System.out.println("question: " + question);
            }
        }

        return questionsOnDevices;
    }

    public void activateTestForGroup(ArrayList<String> questionIds, ArrayList<String> students, String testID) {

        //one reinitialize if groups array are same size as number of groups (meaning we are in a new groups session)
        if (questionIdsForGroups.size() == Koeko.studentGroupsAndClass.size() - 1) {
            questionIdsForGroups.clear();
            studentNamesForGroups.clear();
        }
        //add ids(clone it because we want to remove its content later without affecting the source array) and students to group arrays
        questionIdsForGroups.add(new ArrayList<>());
        for (String id : questionIds) {
            questionIdsForGroups.get(questionIdsForGroups.size() - 1).add(id);
        }
        studentNamesForGroups.add(students);

        for (String studentName : students) {
            Student student = aClass.getStudentWithName(studentName);
            if (questionIds.size() > 0) {
                //get the one question ID which doesn't correspond to a test
                int i = 0;
                for (; i < questionIds.size() && Long.valueOf(questionIds.get(i)) < 0; i++) {
                }
                ArrayList<Student> singleStudent = new ArrayList<>();
                singleStudent.add(student);
                sendQuestionID(questionIds.get(i), singleStudent);
            }
            student.setTestQuestions((ArrayList<String>) questionIds.clone());

            //following code not used for now
            if (Long.valueOf(testID) != 0) {
                Test studentTest = new Test();
                studentTest.setIdTest(testID);
                studentTest.setIdsQuestions((ArrayList<String>) questionIds.clone());
                for (String ignored : questionIds) {
                    studentTest.getQuestionsEvaluations().add(-1.0);
                }
                student.setActiveTest(studentTest);
            }
        }
    }

    public void sendActiveIds(Student student) {
        //send a list of the ids to sync
        if (network_solution == 1) {
            SyncedIds syncedIds = new SyncedIds();
            syncedIds.setPrefix(TransferPrefix.stateUpdate);
            for (String id : networkStateSingleton.getQuestionIdsToSend()) {
                syncedIds.getIds().add(id);
            }

            try {
                sendTransferableObjectWithoutId(syncedIds, student);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //send the active questions
        ArrayList<String> activeIDs = (ArrayList<String>) Koeko.studentGroupsAndClass.get(0).getActiveIDs().clone();
        if (activeIDs.size() > 0) {
            try {
                for (String activeID : activeIDs) {
                    if (Long.valueOf(activeID) > 0) {
                        sendMultipleChoiceWithID(activeID, student);
                        sendShortAnswerQuestionWithID(activeID, student);
                    } else {
                        //send test object
                        sendTestWithID(QuestionGeneric.changeIdSign(activeID), student);

                        //send media file linked to test
                        String mediaFileName = DbTableTest.getMediaFileName(activeID);
                        if (mediaFileName.length() > 0) {
                            File mediaFile = FilesHandler.getMediaFile(mediaFileName);
                            sendMediaFile(mediaFile, student);
                        }
                    }
                }
                System.out.println("address: " + student.getInetAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //activate the present question/test if it's not already done
        if (networkStateSingleton.getStudentsToActiveIdMap().get(student.getUniqueDeviceID()) != null) {
            if (!networkStateSingleton.getStudentsToActiveIdMap().get(student.getUniqueDeviceID())
                    .contentEquals(networkStateSingleton.getActiveID())) {
                ArrayList<Student> singleStudent = new ArrayList<>();
                singleStudent.add(student);
                sendQuestionID(networkStateSingleton.getActiveID(), singleStudent);
            }
        } else {
            System.err.println("student with id: " + student.getUniqueDeviceID() + " not in studentsToActiveIdMap");
        }
    }

    private void sendOnlyId(Student student, String id) {
        if (network_solution == 1) {
            SyncedIds syncedIds = new SyncedIds();
            syncedIds.getIds().add(id);
            try {
                sendTransferableObjectWithoutId(syncedIds, student);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendString(Student student, String prefixMessage) {
        byte[] prefix = new byte[prefixSize];
        for (int i = 0; i < prefixMessage.getBytes().length && i < prefixSize; i++) {
            prefix[i] = prefixMessage.getBytes()[i];
        }
        writeToOutputStream(student, prefix);
    }

    public void activateGame(Integer gameType, Student student) {
        try {
            for (Game game : Koeko.activeGames) {
                for (StudentCellView studentCellView : game.getTeamOne().getStudentCellViews()) {
                    if (student  == null || studentCellView.getStudent().getUniqueDeviceID().contentEquals(student.getUniqueDeviceID())) {
                        GameView gameView = new GameView(gameType, game.getEndScore(), 0, 1);
                        sendTransferableObjectWithoutId(gameView, student);
                    }
                }
            }

            for (Game game : Koeko.activeGames) {
                for (StudentCellView studentCellView : game.getTeamTwo().getStudentCellViews()) {
                    if (student  == null || studentCellView.getStudent().getUniqueDeviceID().contentEquals(student.getUniqueDeviceID())) {
                        GameView gameView = new GameView(gameType, game.getEndScore(), 0, 2);
                        sendTransferableObjectWithoutId(gameView, student);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] buildPrefixBytes(String prefix) {
        byte[] wholePrefix = new byte[NetworkCommunication.prefixSize];
        byte[] stringPrefix = prefix.getBytes();
        for (int i = 0; i < stringPrefix.length; i++) {
            wholePrefix[i] = stringPrefix[i];
        }
        return wholePrefix;
    }

    private byte[] appendContentToPrefix(byte[] prefixByte, byte[] contentByte) {
        byte[] wholeBytesArray = Arrays.copyOf(prefixByte, prefixByte.length + contentByte.length);
        System.arraycopy(contentByte, 0, wholeBytesArray, prefixByte.length, contentByte.length);
        return wholeBytesArray;
    }

    private void launchWritingLoop() {
        Thread writingThread = new Thread(() -> {
            queueIsFinished.set(false);
            while (sendingQueue.size() >= 0) {
                try {
                    PayloadForSending pl = sendingQueue.take();
                    System.out.println("Sending: " + pl.getPayloadID());
                    synchronized (pl.getOutputStream()) {
                        pl.getOutputStream().write(pl.getPayload(), 0, pl.getPayload().length);
                        pl.getOutputStream().flush();
                    }
                    System.out.println("Finished sending:" + System.nanoTime() / 1000000000.0 + ": " + pl.getPayloadID());
                } catch (InterruptedException ex1) {
                    ex1.printStackTrace();
                } catch (SocketException sockex) {
                    System.out.println("SocketException (socket closed by client?)");
                } catch (IOException ex2) {
                    if (ex2.toString().contains("Broken pipe")) {
                        System.out.println("Broken pipe with a student (student was null)");
                    } else {
                        System.out.println("Other IOException occured");
                        ex2.printStackTrace();
                    }
                } catch (NullPointerException nulex) {
                    System.out.println("NullPointerException in a thread with null output stream (closed by another thread)");
                }
                if (sendingQueue.size() == 0) {
                    queueIsFinished.set(true);
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!NetworkCommunication.networkCommunicationSingleton.checkIfQuestionsOnDevices()) {
                System.err.println("Everything data was sent but some probably didn't reach!!!");
            }
        });
        writingThread.start();
    }

    private void writeToOutputStream(Student student, byte[] bytearray) {
        writeToOutputStream(student, null, bytearray);
    }

    private void writeToOutputStream(Student student, String sentID, byte[] bytearray) {
        try {
            if (student == null) {
                //change if necessary sync status of student
                if (sentID != null) {
                    networkStateSingleton.toggleSyncStateForStudent(aClass.getStudents(), NetworkState.STUDENT_NOT_SYNCED);
                }

                ArrayList<OutputStream> handledOutputStreams = new ArrayList<>();

                for (Student singleStudent : aClass.getStudents()) {
                    if (singleStudent.getOutputStream() != null && (sentID == null || SettingsController.forceSync == 1
                            || !networkStateSingleton.getStudentsToSyncedIdsMap().get(singleStudent.getUniqueDeviceID()).contains(sentID))
                            && !handledOutputStreams.contains(singleStudent.getOutputStream())) {
                        try {
                            sendingQueue.put(new PayloadForSending(singleStudent.getOutputStream(), bytearray, sentID));
                            if (queueIsFinished.get() == true) {
                                launchWritingLoop();
                            }
                        } catch (InterruptedException intex) {
                            intex.printStackTrace();
                        }

                        //if Nearby activated (layers), try to prevent sending twice to same outputStream
                        if (NetworkCommunication.network_solution == 1 && !handledOutputStreams.contains(singleStudent.getOutputStream())) {
                            handledOutputStreams.add(singleStudent.getOutputStream());
                        }
                    } else {
                        if (networkStateSingleton.getStudentsToSyncedIdsMap().get(singleStudent.getUniqueDeviceID()).containsAll(networkStateSingleton.getQuestionIdsToSend())) {
                            networkStateSingleton.toggleSyncStateForStudent(singleStudent, NetworkState.STUDENT_SYNCED);
                        }
                    }
                }
            } else {
                //change if necessary sync status of student
                if (sentID != null) {
                    networkStateSingleton.toggleSyncStateForStudent(student, NetworkState.STUDENT_NOT_SYNCED);
                }
                if (sentID == null || SettingsController.forceSync == 1
                        || !networkStateSingleton.getStudentsToSyncedIdsMap().get(student.getUniqueDeviceID()).contains(sentID)) {
                    try {
                        sendingQueue.put(new PayloadForSending(student.getOutputStream(), bytearray, sentID));
                        if (queueIsFinished.get() == true) {
                            launchWritingLoop();
                        }
                    } catch (InterruptedException intex) {
                        intex.printStackTrace();
                    }
                } else {
                    if (networkStateSingleton.getStudentsToSyncedIdsMap().get(student.getUniqueDeviceID()).containsAll(networkStateSingleton.getQuestionIdsToSend())) {
                        networkStateSingleton.toggleSyncStateForStudent(student, NetworkState.STUDENT_SYNCED);
                    }
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void popUpIfStudentIdentifierCollision(String studentName) {
        Platform.runLater(() -> {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Koeko.studentsVsQuestionsTableControllerSingleton);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text(studentName + " is trying to connect but has a different " +
                    "device identifier \n than the student with the same name already registered."));
            Scene dialogScene = new Scene(dialogVbox, 450, 40);
            dialog.setScene(dialogScene);
            dialog.show();
        });
    }

    private void sendIpAddress() throws IOException {
        if (Koeko.leftBarController != null &&
                Koeko.leftBarController.ipAddresses != null &&
                Koeko.leftBarController.ipAddresses.size() > 0) {
            for (int i = 0; i < Koeko.leftBarController.ipAddresses.size(); i++) {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);

                String stringAddress = Koeko.leftBarController.ipAddresses.get(i);
                String stringBroadcastAddress = stringAddress.substring(0, stringAddress.lastIndexOf(".") + 1);
                stringBroadcastAddress = stringBroadcastAddress + "255";
                InetAddress address = InetAddress.getByName(stringBroadcastAddress);

                String message = "IPADDRESS///" + stringAddress + "///";
                byte[] buffer = message.getBytes();

                DatagramPacket packet
                        = new DatagramPacket(buffer, buffer.length, address, 9346);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    if (!e.toString().contains("is unreachable")) {
                        if (e.toString().contains("Network is down")) {
                            System.out.println("Trying to send ip through udp: Network is down");
                        } else if (e.toString().contains("Host is down")) {
                            System.out.println("Trying to send ip through udp: Host is down");
                        } else if (e.toString().contains("Can't assign requested address")) {
                            System.out.println("Can't assign requested address");
                        } else if (e.toString().contains("No route to host")) {
                            System.out.println("No route to host");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
                socket.close();
            }
        }

        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void activateSubnet(DeviceInfo advertiser, DeviceInfo discoverer) throws IOException {
        SubNet subNet = networkStateSingleton.activateAndGetNextSubnet(advertiser, discoverer);
        ShortCommand shortCommandAdver = new ShortCommand();
        shortCommandAdver.setCommand(ShortCommands.advertiser);
        Student advertiserStudent = aClass.getStudentWithUniqueID(subNet.getAdvertiser().getUniqueId());
        sendTransferableObjectWithoutId(shortCommandAdver, advertiserStudent);
        System.out.println("activating: " + advertiserStudent.getName() + " as advertiser");

        ShortCommand shortCommandDiscov = new ShortCommand();
        shortCommandDiscov.setCommand(ShortCommands.discoverer);
        shortCommandDiscov.setOptionalArgument1(subNet.getName());
        shortCommandDiscov.setOptionalArgument2(subNet.getPassword());
        Student discovererStudent = aClass.getStudentWithUniqueID(subNet.getDiscoverer().getUniqueId());
        sendTransferableObjectWithoutId(shortCommandDiscov, discovererStudent);
        System.out.println("activating: " + discovererStudent.getName() + " as discoverer");
    }
}
