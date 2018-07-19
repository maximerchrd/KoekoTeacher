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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class functionalTesting {
    static PrintStream originalStream;
    static PrintStream dummyStream;
    static int idOffset = 1000;
    static int numberStudents = 2;
    static int numberOfQuestions = 10;

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
                sendQuestionsPack(0,functionalTesting.numberOfQuestions);
                System.setOut(originalStream);
                System.out.println("** Activating questions");
                System.setOut(dummyStream);
                activateQuestionsPack(0,functionalTesting.numberOfQuestions);

                deleteQuestionsPack(0, functionalTesting.numberOfQuestions);

                System.setOut(originalStream);
                System.out.println("*** RESULTS ***");

                for (Student student : NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector()) {
                    System.out.println(student.getName() + " was sent \t \t " + studentsNbEvalSent.get(student.getName()) + " evaluations.");
                }

                System.out.println("***** END FUNCTIONAL TESTING *****");
            }
        });
    }

    private static void activateQuestionsPack(int startingIndex, int endingIndex) {
        Vector<Student> students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector();
        for (int j = idOffset + startingIndex; j < idOffset + endingIndex; j++) {
            NetworkCommunication.networkCommunicationSingleton.SendQuestionID(j, students);
            //NetworkCommunication.networkCommunicationSingleton.sendShortAnswerQuestionWithID(1000 + j, students.get(i));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.setOut(originalStream);
            System.out.println("Sent question with ID: " + j);
            System.setOut(dummyStream);
        }
    }

    private static void sendQuestionsPack(int startingIndex, int endingIndex) {
        Vector<Student> students = NetworkCommunication.networkCommunicationSingleton.aClass.getStudents_vector();
        for (int i = 0; i < students.size(); i++) {
            for (int j = idOffset + startingIndex; j < idOffset + endingIndex; j++) {
                try {
                    NetworkCommunication.networkCommunicationSingleton.sendMultipleChoiceWithID(j, students.get(i));
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
                DbTableQuestionMultipleChoice.addMultipleChoiceQuestion(questionMultipleChoice);
                changeMcqID(String.valueOf(idOffset + i), questionMultipleChoice.getQUESTION());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteQuestionsPack(int startingIndex, int endingIndex) {
        for (int i = idOffset + startingIndex ; i < idOffset + endingIndex; i++) {
            try {
                DbTableQuestionMultipleChoice.removeMultipleChoiceQuestionWithID(String.valueOf(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static private void changeMcqID(String id, String question) {
        String sql = 	"UPDATE multiple_choice_questions SET ID_GLOBAL = '" + id +
                "' WHERE QUESTION = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:learning_tracker.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, question);

            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
