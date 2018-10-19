package koeko;

import javafx.application.Platform;
import koeko.KoekoSyncCollect.SyncOperations;
import koeko.Networking.NetworkCommunication;
import koeko.database_management.*;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.Test;
import koeko.students_management.Student;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.*;

/**
 * TestCode
 * 0:
 * 1:
 * 2:
 * 3: create questions with 1 picture and send them
 * 4: insert all question in a test
 * 5: make copies of questions when creating a question
 */
public class functionalTesting {
    static public Boolean transferRateTesting = false;
    static PrintStream originalStream;
    static PrintStream dummyStream;
    static int idOffset = 1000;
    static public int numberStudents = 2;
    static public int numberOfQuestions = 10;
    static public Long msDelay = 3000L;
    static ArrayList<String> questionPack = new ArrayList<>();

    static public Map<String, Integer> studentsNbEvalSent = new  LinkedHashMap<>();
    static public Boolean testMode = false;
    static public Long startTimeQuestionSending = 0L;
    static public Long endTimeQuestionSending = 0L;
    static public Integer nbAccuseReception = 0;
    static public int testCodeGlobal = -1;
    static public int nbCopiesGlobal = 0;

    static public void mainTesting(int testCode, int nbStudents, int nbQuestions, Long msDelay, int nbCopies) {
        functionalTesting.testCodeGlobal = testCode;
        functionalTesting.nbCopiesGlobal = nbCopies;
        testMode = true;
        numberStudents = nbStudents;
        numberOfQuestions = nbQuestions;
        functionalTesting.msDelay = msDelay;
        startTimeQuestionSending = 0L;
        endTimeQuestionSending = 0L;
        nbAccuseReception = 0;
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                if (testCode == 0 || testCode == 2 || testCode == 3) {
                    while (NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector().size() < functionalTesting.numberStudents) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    for (Student student : NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector()) {
                        studentsNbEvalSent.put(student.getName(), 0);
                    }

                    originalStream = System.out;
                    dummyStream = new PrintStream(new OutputStream() {
                        public void write(int b) {
                            // NO-OP
                        }
                    });

                    System.out.println("***** START FUNCTIONAL TESTING *****");
                    System.setOut(dummyStream);

                    createQuestionsPack(0, functionalTesting.numberOfQuestions, "subject 1", "objective 1", testCode);

                    System.setOut(originalStream);
                    System.out.println("** Sending questions");
                    System.out.println(NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector().size());
                    System.setOut(dummyStream);

                    sendQuestionsPack();

                    while (functionalTesting.endTimeQuestionSending == 0L) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.setOut(originalStream);
                    Long sendingTime = functionalTesting.endTimeQuestionSending - functionalTesting.startTimeQuestionSending;
                    sendingTime = sendingTime / 1000;
                    System.out.println("Questions sent in: " + sendingTime + " seconds");
                    System.out.println("** Activating questions");
                    System.setOut(dummyStream);

                    activateQuestionsPack();

                    deleteQuestionsPack();

                    System.setOut(originalStream);
                    System.out.println("*** RESULTS ***");

                    for (Student student : NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector()) {
                        System.out.println(student.getName() + " was sent \t \t " + studentsNbEvalSent.get(student.getName()) + " evaluations.");
                    }

                    System.out.println("***** END FUNCTIONAL TESTING *****");
                } else if (testCode == 1) {
                    DbTableProfessor.addProfessor("","","test teacher");
                    DbTableProfessor.setProfessorLanguage("test teacher", "eng");
                    insertQuestion("test QMC 1 version 1","test subject 1");
                    insertQuestion("test QMC 2 version 1", "test subject 2");
                    insertQuestion("test QMC 3 version 1", "test subject 3");
                    //Sync
                    try {
                        SyncOperations.SyncAll(InetAddress.getByName("127.0.0.1"), 50507, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //clean up the db
                    //deleteQuestion("1000", "test subject 1");
                    //deleteQuestion("1001","test subject 2");
                } else if (testCode == 4) {
                    createTest(nbCopies);
                }
            }
        });
    }

    private static void createTest(int nbOfCopies) {
        Vector<String> questionids = DbTableQuestionGeneric.getAllGenericQuestionsIds();
        Test test = new Test();
        test.setTestMode(1);
        test.setTestName("fctTesting test");
        DbTableTest.addTest(test);
        for (int i = 0; i < questionids.size(); i++) {
            if (i > 0) {
                DbTableRelationQuestionQuestion.addRelationQuestionQuestion(questionids.get(i - 1), questionids.get(i),test.getTestName(), "");
            }
        }
    }

    private static void deleteQuestion(String id, String subject) {
        try {
            DbTableQuestionMultipleChoice.removeMultipleChoiceQuestionWithID(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DbTableSubject.deleteSubject(subject);
        DbTableRelationQuestionSubject.removeRelationSubjectQuestion(subject, id);
    }

    private static void insertQuestion(String question, String subject) {
        //Setup the db before syncing
        try {
            QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
            questionMultipleChoice.setQUESTION(question);
            questionMultipleChoice.setLEVEL("0");
            String id = DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(questionMultipleChoice);
            DbTableSubject.addSubject(subject + "a");
            DbTableSubject.addSubject(subject + "b");
            DbTableSubject.addSubject(subject + "c");
            DbTableRelationQuestionSubject.addRelationQuestionSubject(id,subject+ "a");
            DbTableRelationQuestionSubject.addRelationQuestionSubject(id,subject+ "b");
            DbTableRelationQuestionSubject.addRelationQuestionSubject(id,subject+ "c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void activateQuestionsPack() {
        Vector<Student> students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector();
        for (String id : questionPack) {
            NetworkCommunication.networkCommunicationSingleton.SendQuestionID(id, students);
            //NetworkCommunication.networkCommunicationSingleton.sendShortAnswerQuestionWithID(1000 + j, students.get(i));
            try {
                Thread.sleep(msDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.setOut(originalStream);
            System.out.println("Sent question with ID: " + id);
            System.setOut(dummyStream);
        }
    }

    private static void sendQuestionsPack() {
        functionalTesting.startTimeQuestionSending = System.currentTimeMillis();
        Vector<Student> students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector();
        for (int i = 0; i < students.size(); i++) {
            for (String id : questionPack) {
                try {
                    NetworkCommunication.networkCommunicationSingleton.sendMultipleChoiceWithID(id, students.get(i));
                    //NetworkCommunication.networkCommunicationSingleton.sendShortAnswerQuestionWithID(1000 + j, students.get(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void createQuestionsPack(int startingIndex, int endingIndex, String subject, String objective, int testCode) {
        questionPack.removeAll(questionPack);
        for (int i = startingIndex; i < endingIndex; i++) {
            QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
            questionMultipleChoice.setQUESTION("question " + (i + 1) + "^^.-_$£, +\"*ç%&/()=?'^" + "7492qJfzdDSB");
            questionMultipleChoice.setOPT0((i + 1) + "A OK not so harsh for the special characters, but still, this needs to be solved somehow later, but still, this needs to be solved somehow later, but still, this needs to be solved somehow later, but still, this needs to be solved somehow later.");
            questionMultipleChoice.setOPT1((i + 1) + "B \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT2((i + 1) + "C \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT3((i + 1) + "D \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT4((i + 1) + "E \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT5((i + 1) + "F \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT6((i + 1) + "G \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT7((i + 1) + "H \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT8((i + 1) + "I \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setOPT9((i + 1) + "J \"^^.-_$£, +\\\"*ç%&/()=?'^");
            questionMultipleChoice.setNB_CORRECT_ANS(1);
            if (testCode == 0) {
                questionMultipleChoice.setIMAGE("pictures/image_" + i % 4 + ".jpg");
            } else if (testCode == 2){
                questionMultipleChoice.setIMAGE("none");
            } else if (testCode == 3) {
                questionMultipleChoice.setIMAGE("pictures/cell.jpg");
            }
            try {
                questionPack.add(DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(questionMultipleChoice));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteQuestionsPack() {
        for (String id : questionPack) {
            try {
                DbTableQuestionMultipleChoice.removeMultipleChoiceQuestionWithID(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
