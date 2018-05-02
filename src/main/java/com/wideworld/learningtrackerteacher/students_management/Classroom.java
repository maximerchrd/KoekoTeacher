package com.wideworld.learningtrackerteacher.students_management;

import com.wideworld.learningtrackerteacher.questions_management.QuestionMultipleChoice;
import com.wideworld.learningtrackerteacher.questions_management.QuestionShortAnswer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by maximerichard on 28/03/17.
 */
public class Classroom {
    private Vector<Student> students_vector = null;
    private ArrayList<String> students_addresses = null;
    private ArrayList<QuestionMultipleChoice> current_set_quest_mult_choice = null;
    private ArrayList<QuestionShortAnswer> current_set_quest_short_answer = null;
    private ArrayList<Integer> activeIDs;
    private ArrayList<String> activeQuestions;
    private ArrayList<ArrayList<Double>> activeEvaluations;
    private ArrayList<Double> averageEvaluations;
    private Integer tableIndex = -1;
    private String className = "";
    private Integer nbAndroidDevices = 0;
    private ArrayList<Integer> IDsToStoreOnDevices;
    private Map<String, OutputStream> studentsPath;

    public Classroom() {
        students_vector = new Vector<>();
        students_addresses = new ArrayList<>();
        current_set_quest_mult_choice = new ArrayList<>();
        current_set_quest_short_answer = new ArrayList<>();
        activeIDs = new ArrayList<>();
        activeQuestions = new ArrayList<>();
        activeEvaluations = new ArrayList<>();
        averageEvaluations = new ArrayList<>();
        IDsToStoreOnDevices = new ArrayList<>();
        studentsPath =  new HashMap<String, OutputStream>();
    }

    //getters
    public ArrayList<ArrayList<Double>> getActiveEvaluations() {
        return activeEvaluations;
    }
    public ArrayList<Double> getAverageEvaluations() {
        return averageEvaluations;
    }
    public ArrayList<String> getActiveQuestions() {
        return activeQuestions;
    }
    public Integer getNbAndroidDevices() {
        return nbAndroidDevices;
    }
    public Integer getTableIndex() {
        return tableIndex;
    }
    public ArrayList<QuestionMultipleChoice> getCurrent_set_quest_mult_choice() {
        return current_set_quest_mult_choice;
    }
    public ArrayList<QuestionShortAnswer> getCurrent_set_quest_short_answer() {
        return current_set_quest_short_answer;
    }
    public String getClassName() {
        return className;
    }
    public int getClassSize() {
        return students_vector.size();
    }
    public Vector<Student> getStudents_vector() {
        return students_vector;
    }
    public ArrayList<Integer> getActiveIDs() {
        return activeIDs;
    }
    public ArrayList<Integer> getIDsToStoreOnDevices() {
        return IDsToStoreOnDevices;
    }
    public Map<String, OutputStream> getStudentsPath() {
        return studentsPath;
    }


    //setters
    public void setActiveEvaluations(ArrayList<ArrayList<Double>> activeEvaluations) {
        this.activeEvaluations = activeEvaluations;
    }
    public void setAverageEvaluations(ArrayList<Double> averageEvaluations) {
        this.averageEvaluations = averageEvaluations;
    }
    public void setActiveQuestions(ArrayList<String> activeQuestions) {
        this.activeQuestions = activeQuestions;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public void setActiveIDs(ArrayList<Integer> activeIDs) {
        this.activeIDs = activeIDs;
    }
    public void setNbAndroidDevices(Integer nbAndroidDevices) {
        this.nbAndroidDevices = nbAndroidDevices;
    }
    public void setTableIndex(Integer tableIndex) {
        this.tableIndex = tableIndex;
    }
    public void setIDsToStoreOnDevices(ArrayList<Integer> IDsToStoreOnDevices) {
        this.IDsToStoreOnDevices = IDsToStoreOnDevices;
    }
    public void setStudentsPath(Map<String, OutputStream> studentsPath) {
        this.studentsPath = studentsPath;
    }


    //other get methods
    public Student getStudentWithID(Integer studentID) {
        Student student = new Student();
        for (int i = 0; i < students_vector.size(); i++) {
            if (String.valueOf(studentID).contentEquals(String.valueOf(students_vector.get(i).getStudentID()))) {
                System.out.println("equal");
                student = students_vector.get(i);
            }
        }
        return student;
    }

    public Student getStudentWithUniqueID(String deviceID) {
        Student student = new Student();
        Boolean found = false;
        for (int i = 0; i < students_vector.size() && !found; i++) {
            if (deviceID.contentEquals(students_vector.get(i).getUniqueID())) {
                student = students_vector.get(i);
                System.out.println("found student: " + student.getName() + " with device ID");
                found = true;
            }
        }
        return student;
    }

    public Student getStudentWithName(String studentName) {
        Student student = new Student();
        for (int i = 0; i < students_vector.size(); i++) {
            if (studentName.contentEquals(String.valueOf(students_vector.get(i).getName()))) {
                System.out.println("equal");
                student = students_vector.get(i);
            }
        }
        return student;
    }

    public String getQuestionWithID(Integer questionID) {
        String question = "";
        if (activeQuestions.size() == activeIDs.size() && activeIDs.size() > 0) {
            question = activeQuestions.get(activeIDs.indexOf(questionID));
        }
        return question;
    }

    public int indexOfStudentWithAddress (String address) {
        int index = -1;
        for (int i = 0; i < students_vector.size(); i++) {
            if (students_vector.get(i).getInetAddress().toString().equals(address)) {
                index = i;
            }
        }
        return index;
    }

    private int readStudents_addresses() {
        students_addresses.clear();
        for (int i = 0; i < students_vector.size(); i++) {
            students_addresses.add(students_vector.get(i).getInetAddress().toString());
        }
        return students_addresses.size();
    }

    //returns only the question IDs, without the test IDs
    public ArrayList<Integer> getActiveQuestionIDs() {
        ArrayList<Integer> questionIDs = new ArrayList<>();
        for (Integer id : activeIDs) {
            if (id > 0) {
                questionIDs.add(id);
            }
        }
        return questionIDs;
    }


    //methods for adding members
    public void addQuestMultChoice(QuestionMultipleChoice questionMultipleChoice) {
        current_set_quest_mult_choice.add(questionMultipleChoice);
    }
    public void addQuestShortAnswer(QuestionShortAnswer questionShortAnswer) {
        current_set_quest_short_answer.add(questionShortAnswer);
    }
    public void addStudent(Student student) {
        students_vector.add(student);
    }
    public void addStudentIfNotInClass(Student student) {
        int students_addresses_size = readStudents_addresses();
        System.out.println("studentGroupsAndClass addresses size: "+ students_addresses_size);
        if (students_addresses_size > 0) {
            if (!students_addresses.contains(student.getUniqueID())) {
                System.out.println("studentGroupsAndClass addresses content: " + students_addresses.get(0) + " student address: " + student.getUniqueID());
                students_vector.add(student);
            } else {
                int index = students_addresses.indexOf(student.getUniqueID());
                students_vector.remove(index);
                students_vector.add(student);
            }
        } else {
            students_vector.add(student);
        }
    }


    //update methods
    public void updateStudent (Student student) {
        int index = indexOfStudentWithAddress(student.getInetAddress().toString());
        if (index >= 0) {
            students_vector.remove(index);
            students_vector.add(student);
        } else {
            System.out.println("A problem occured: student not in class when trying to update infos");
        }
    }

    public Double updateAverageEvaluationForQuestion(Integer question, Integer student, Double evaluation) {
        Double averageEval= 0.0;
        Double numberEval = 0.0;
        if (activeEvaluations.size() > student && activeEvaluations.get(student).size() > 0) {
            if (activeEvaluations.get(student).get(question) != null) {
                activeEvaluations.get(student).set(question, evaluation);
            }
        }
        for (int i = 0; i < activeEvaluations.size(); i++) {
            if (activeEvaluations.get(i).size() > question && activeEvaluations.get(i).get(question) != null
                    && activeEvaluations.get(i).get(question) != -1) {
                averageEval += activeEvaluations.get(i).get(question);
                numberEval += 1.0;
            }
        }
        averageEval = averageEval / numberEval;
        if (averageEvaluations.size() > question) {
            averageEvaluations.set(question, averageEval);
        }
        return averageEval;
    }

    public Double updateAverageEvaluationForClass() {
        Double averageEval= 0.0;
        Double numberEval = 0.0;
        for (int i = 0; i < averageEvaluations.size(); i++) {
            if (averageEvaluations.get(i) != null) {
                averageEval += averageEvaluations.get(i);
                numberEval += 1.0;
            }
        }
        averageEval = averageEval / numberEval;
        return averageEval;
    }


    //other methods
    public void pruneLastStudentIfAlreadyInClass() {
        readStudents_addresses();
        String last_address = students_addresses.get(students_addresses.size() - 1);
        students_addresses.remove(students_addresses.size() - 1);
        if (students_addresses.contains(last_address)) {
            students_vector.remove(students_vector.size() - 1);
        }
    }

    public Boolean studentAlreadyInClass (Student student) {
        int students_addresses_size = readStudents_addresses();
        if (students_addresses_size > 0) {
            if (!students_addresses.contains(student.getInetAddress().toString())) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public Boolean studentDeviceIDAlreadyInClass (Student student) {
        ArrayList<String> deviceIDs = new ArrayList<>();
        for (Student stud : students_vector) {
            deviceIDs.add(stud.getUniqueID());
        }
        if (deviceIDs.size() > 0) {
            if (!deviceIDs.contains(student.getUniqueID())) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public Boolean allQuestionsOnDevices() {
        Boolean questionsReached = true;
        for (Student student : students_vector) {
            ArrayList<Integer> deviceQuestions = new ArrayList<>();
            for (String id : student.getDeviceQuestions()) {
                deviceQuestions.add(Integer.valueOf(id));
            }
            if (!deviceQuestions.containsAll(activeIDs)) {
                questionsReached = false;
                break;
            }
        }
        return  questionsReached;
    }
}
