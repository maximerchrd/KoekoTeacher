package com.wideworld.learningtrackerteacher.Networking;

import com.wideworld.learningtrackerteacher.LearningTracker;
import com.wideworld.learningtrackerteacher.controllers.QuestionSendingController;
import com.wideworld.learningtrackerteacher.database_management.DbTableIndividualQuestionForStudentResult;
import com.wideworld.learningtrackerteacher.database_management.DbTableStudents;
import com.wideworld.learningtrackerteacher.students_management.Classroom;
import com.wideworld.learningtrackerteacher.students_management.Student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class ReceptionProtocol {

    static public void receivedCONN(Student arg_student, String answerString, Classroom aClass) {
        Student student = arg_student;
        student.setUniqueID(answerString.split("///")[1]);
        student.setName(answerString.split("///")[2]);
        Integer studentID = DbTableStudents.addStudent(answerString.split("///")[1], answerString.split("///")[2]);
        if (studentID == -2) {
            NetworkCommunication.networkCommunicationSingleton.popUpIfStudentIdentifierCollision(student.getName());
        }
        student.setStudentID(studentID);

        //update the tracking of questions on device


        NetworkCommunication.networkCommunicationSingleton.getLearningTrackerController().addUser(student, true);

        if (aClass.studentDeviceIDAlreadyInClass(student)) {
            aClass.updateStudentButNotStreams(student);
        } else {
            if (student.getUniqueID().contentEquals(student.getMasterUniqueID())) {
                aClass.updateStudentButNotStreams(student);
            } else {
                aClass.addStudent(student);
                System.out.println("adding student: " + student.getName() + " to class for Network Communication.");
                System.out.println("Students in class: ");
                for (Student printStudent : aClass.getStudents_vector()) {
                    System.out.println(printStudent.getName());
                }
            }
        }
        if (aClass.studentAlreadyInClass(student) && answerString.contains("Android")) {
            aClass.setNbAndroidDevices(aClass.getNbAndroidDevices() + 1);
            System.out.println("Increasing the number of connected android devices");
        }
        aClass.getStudentsPath().put(student.getUniqueID(), student.getOutputStream());
    }

    static public void receivedANSW(Student arg_student, String answerString, ArrayList<ArrayList<Integer>> questionIdsForGroups,
                                    ArrayList<ArrayList<String>> studentNamesForGroups) {
        double eval = DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(Integer.valueOf(answerString.split("///")[5]),
                answerString.split("///")[2], answerString.split("///")[3], answerString.split("///")[0]);
        NetworkCommunication.networkCommunicationSingleton.SendEvaluation(eval, Integer.valueOf(answerString.split("///")[5]), arg_student);

        //find out to which group the student and answer belong
        Integer groupIndex = 0;
        Integer questID = Integer.valueOf(answerString.split("///")[5]);
        for (int i = 0; i < studentNamesForGroups.size(); i++) {
            if (studentNamesForGroups.get(i).contains(arg_student.getName()) && questionIdsForGroups.get(i).contains(questID)) {
                groupIndex = i;
                questionIdsForGroups.get(i).remove(questID);
            }
        }
        NetworkCommunication.networkCommunicationSingleton.getLearningTrackerController().addAnswerForUser(arg_student,
                answerString.split("///")[3], answerString.split("///")[4], eval,
                Integer.valueOf(answerString.split("///")[5]), groupIndex);
        Integer nextQuestion = arg_student.getNextQuestionID(Integer.valueOf(answerString.split("///")[5]));
        System.out.println("student: " + arg_student.getName() + ";former question: " + questID + "; nextQuestion:" + nextQuestion);
        for (Integer testid : arg_student.getTestQuestions()) {
            System.out.println(testid);
        }
        if (nextQuestion != -1) {
            Vector<Student> singleStudent = new Vector<>();
            singleStudent.add(arg_student);
            NetworkCommunication.networkCommunicationSingleton.SendQuestionID(nextQuestion, singleStudent);
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
    }

    static public void receivedGOTIT(String answerString, Student arg_student) {
        if (answerString.split("///").length > 1) {
            if (answerString.split("///")[1].contains(arg_student.getPendingPacketUUID())) {
                arg_student.setPendingPacketUUID("");
                System.out.println("Using some deprecated feature: pending packet UUID");
            } else {
                if (answerString.split("///").length > 2) {
                    String questionID = answerString.split("///")[1];
                    String studentID = answerString.split("///")[2];
                    System.out.println("client received question: " + questionID);
                    if (LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().contains(Integer.valueOf(questionID))) {
                        /*int IDindex = LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().indexOf(Integer.valueOf(questionID));
                        if (LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().size() > IDindex + 1
                                && studentID.contentEquals(arg_student.getUniqueID())) {    //we also need to check if the student who got the question is from the first layer
                            try {
                                System.out.println("active ids: " + LearningTracker.studentGroupsAndClass.get(0).getActiveIDs());
                                System.out.println("IDindex: " + IDindex);
                                NetworkCommunication.networkCommunicationSingleton.sendMultipleChoiceWithID(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(IDindex + 1), arg_student);
                                NetworkCommunication.networkCommunicationSingleton.sendShortAnswerQuestionWithID(LearningTracker.studentGroupsAndClass.get(0).getActiveIDs().get(IDindex + 1), arg_student);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }*/
                        //add the ID to the ID list for the student inside the class
                        LearningTracker.studentGroupsAndClass.get(0).getStudentWithUniqueID(studentID).getDeviceQuestions().add(questionID);
                        System.out.println("transfer finished? " + LearningTracker.studentGroupsAndClass.get(0).allQuestionsOnDevices());
                        if (LearningTracker.studentGroupsAndClass.get(0).allQuestionsOnDevices()) {
                            QuestionSendingController.readyToActivate = true;
                        }
                    }
                }
            }
        } else {
            System.out.println("received GOTIT but array from parsed string too short");
        }
    }
}
