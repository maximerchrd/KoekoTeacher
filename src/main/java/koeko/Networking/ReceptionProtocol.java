package koeko.Networking;

import koeko.database_management.DbTableIndividualQuestionForStudentResult;
import koeko.database_management.DbTableStudents;
import koeko.students_management.Classroom;
import koeko.students_management.Student;

import java.util.ArrayList;
import java.util.Vector;

public class ReceptionProtocol {

    static public void receivedCONN(Student arg_student, String answerString, Classroom aClass) {
        Student student = aClass.getStudentWithIP(arg_student.getInetAddress().toString());
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
            aClass.updateStudentButNotStreams(student);
        }
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
    }
}
