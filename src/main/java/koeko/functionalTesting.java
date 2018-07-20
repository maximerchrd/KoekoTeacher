package koeko;

import javafx.application.Platform;
import koeko.Networking.NetworkCommunication;
import koeko.database_management.DbTableQuestionMultipleChoice;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.students_management.Student;
import koeko.view.Utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;

public class functionalTesting {
    static PrintStream originalStream;
    static PrintStream dummyStream;
    static int idOffset = 1000;
    static int numberStudents = 1;
    static int numberOfQuestions = 20;
    static ArrayList<String> questionPack = new ArrayList<>();

    static public Map<String, Integer> studentsNbEvalSent = new LinkedHashMap<>();
    static public Boolean testMode = false;

    static public void mainTesting() {
        testMode = true;
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                while (NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector().size() < functionalTesting.numberStudents) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (Student student : NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector()) {
                    studentsNbEvalSent.put(student.getName(), 0);
                }

                originalStream = System.out;
                dummyStream = new PrintStream(new OutputStream(){
                    public void write(int b) {
                        // NO-OP
                    }
                });

                System.out.println("***** START FUNCTIONAL TESTING *****");
                System.setOut(dummyStream);

                createQuestionsPack(0,functionalTesting.numberOfQuestions, "subject 1", "objective 1");

                System.setOut(originalStream);
                System.out.println("** Sending questions");
                System.setOut(dummyStream);

                sendQuestionsPack();

                System.setOut(originalStream);
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
            }
        });
    }

    private static void activateQuestionsPack() {
        Vector<Student> students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector();
        for (String id : questionPack) {
            NetworkCommunication.networkCommunicationSingleton.SendQuestionID(id, students);
            //NetworkCommunication.networkCommunicationSingleton.sendShortAnswerQuestionWithID(1000 + j, students.get(i));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.setOut(originalStream);
            System.out.println("Sent question with ID: " + id);
            System.setOut(dummyStream);
        }
    }

    private static void sendQuestionsPack() {
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

    private static void createQuestionsPack(int startingIndex, int endingIndex, String subject, String objective) {
        for (int i = startingIndex; i < endingIndex; i++) {
            QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
            questionMultipleChoice.setQUESTION("question " + (i + 1) + "^^.-_$£, +\"*ç%&/()=?'^");
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
            questionMultipleChoice.setIMAGE("pictures/image_" + i % 4 + ".jpg");
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
