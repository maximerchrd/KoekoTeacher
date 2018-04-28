package com.wideworld.learningtrackerteacher.Networking;

import com.wideworld.learningtrackerteacher.database_management.DbTableStudents;
import com.wideworld.learningtrackerteacher.students_management.Classroom;
import com.wideworld.learningtrackerteacher.students_management.Student;

public class ReceptionProtocol {
    static public void receivedCONN(Student arg_student, String answerString, Classroom aClass) {
        Student student = arg_student;
        student.setAddress(answerString.split("///")[1]);
        student.setName(answerString.split("///")[2]);
        Integer studentID = DbTableStudents.addStudent(answerString.split("///")[1], answerString.split("///")[2]);
        if (studentID == -2) {
            NetworkCommunication.networkCommunicationSingleton.popUpIfStudentIdentifierCollision(student.getName());
        }
        student.setStudentID(studentID);

        //update the tracking of questions on device



        NetworkCommunication.networkCommunicationSingleton.getLearningTrackerController().addUser(student, true);
        aClass.updateStudent(student);
        if (aClass.studentAlreadyInClass(student) && answerString.contains("Android")) {
            aClass.setNbAndroidDevices(aClass.getNbAndroidDevices() + 1);
            System.out.println("Increasing the number of connected android devices");
        }
        aClass.getStudentsPath().put(student.getAddress(),student.getOutputStream());
    }
}
