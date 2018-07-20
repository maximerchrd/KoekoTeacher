package koeko.students_management;

import koeko.Koeko;
import koeko.questions_management.QuestionMultipleChoice;
import koeko.questions_management.QuestionShortAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by maximerichard on 28/03/17.
 */
public class Classroom {
    private Vector<Student> students_vector = null;
    private ArrayList<String> students_addresses = null;
    private ArrayList<QuestionMultipleChoice> current_set_quest_mult_choice = null;
    private ArrayList<QuestionShortAnswer> current_set_quest_short_answer = null;
    private ArrayList<String> activeIDs;
    private ArrayList<String> activeQuestions;
    private ArrayList<ArrayList<Double>> activeEvaluations;
    private ArrayList<Double> averageEvaluations;
    private Integer tableIndex = -1;
    private String className = "";
    private Integer nbAndroidDevices = 0;
    private ArrayList<String> IDsToStoreOnDevices;
    private HashMap<String, Vector<String>> ongoingQuestionsForStudent;

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
        ongoingQuestionsForStudent = new HashMap<>();
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
    public ArrayList<String> getActiveIDs() {
        return activeIDs;
    }
    public ArrayList<String> getIDsToStoreOnDevices() {
        return IDsToStoreOnDevices;
    }
    public HashMap<String, Vector<String>> getOngoingQuestionsForStudent() {
        return ongoingQuestionsForStudent;
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
    public void setActiveIDs(ArrayList<String> activeIDs) {
        this.activeIDs = activeIDs;
    }
    public void setNbAndroidDevices(Integer nbAndroidDevices) {
        this.nbAndroidDevices = nbAndroidDevices;
    }
    public void setTableIndex(Integer tableIndex) {
        this.tableIndex = tableIndex;
    }
    public void setIDsToStoreOnDevices(ArrayList<String> IDsToStoreOnDevices) {
        this.IDsToStoreOnDevices = IDsToStoreOnDevices;
    }
    public void setOngoingQuestionsForStudent(HashMap<String, Vector<String>> ongoingQuestionsForStudent) {
        this.ongoingQuestionsForStudent = ongoingQuestionsForStudent;
    }



    //other get methods
    public Student getStudentWithID(Integer studentID) {
        Student student = new Student();
        for (int i = 0; i < students_vector.size(); i++) {
            if (String.valueOf(studentID).contentEquals(String.valueOf(students_vector.get(i).getStudentID()))) {
                student = students_vector.get(i);
            }
        }
        return student;
    }

    public Student getStudentWithIP(String ip) {
        Student student = new Student();
        for (int i = 0; i < students_vector.size(); i++) {
            if (ip.contentEquals(students_vector.get(i).getInetAddress().toString())) {
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
            if (students_vector.get(i).getInetAddress() != null) {
                if (students_vector.get(i).getInetAddress().toString().equals(address)) {
                    index = i;
                }
            }
        }
        return index;
    }

    private int readStudents_addresses() {
        students_addresses.clear();
        for (int i = 0; i < students_vector.size(); i++) {
            if (students_vector.get(i).getInetAddress() != null) {
                students_addresses.add(students_vector.get(i).getInetAddress().toString());
            } else {
                students_addresses.add("");
            }
        }
        return students_addresses.size();
    }

    //returns only the question IDs, without the test IDs
    public ArrayList<String> getActiveQuestionIDs() {
        ArrayList<String> questionIDs = new ArrayList<>();
        for (String id : activeIDs) {
            if (Long.valueOf(id) > 0) {
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
            if (!students_addresses.contains(student.getInetAddress().toString())) {
                System.out.println("studentGroupsAndClass addresses content: " + students_addresses.get(0) + " student address: " + student.getUniqueID());
                students_vector.add(student);
            } else {
                int index = students_addresses.indexOf(student.getInetAddress().toString());
                if (index > -1) {
                    students_vector.remove(index);
                    students_vector.add(student);
                } else {
                    System.out.println("ERROR in addStudentIfNotInClass: index of student address not found even though should be in array");
                }
            }
        } else {
            students_vector.add(student);
        }
    }


    //update methods
    public Student updateStudentStreams(Student student) {
        int index = indexOfStudentWithAddress(student.getInetAddress().toString());
        if (index >= 0) {
            students_vector.get(index).setInputStream(student.getInputStream());
            students_vector.get(index).setOutputStream(student.getOutputStream());
        } else {
            System.out.println("A problem occured: student not in class when trying to update infos");
        }

        return students_vector.get(index);
    }

    public void updateStudentButNotStreams(Student student) {
        int index = indexOfStudentWithAddress(student.getInetAddress().toString());
        if (index >= 0) {
            students_vector.get(index).setName(student.getName());
            students_vector.get(index).setUniqueID(student.getUniqueID());
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
            if (student.getConnected()) {
                ArrayList<String> deviceQuestions = new ArrayList<>();

                //convert question ids string to int
                for (String id : student.getDeviceQuestions()) {
                    deviceQuestions.add(id);
                }

                //check if the questions on the device contain the active questions
                if (!deviceQuestions.containsAll(activeIDs)) {
                    questionsReached = false;
                    System.out.println("check if all questions on device: " + questionsReached);
                    System.out.println("activeIDs: " + activeIDs);
                    System.out.println("device IDs: " + deviceQuestions);
                    break;
                }
                System.out.println("check if all questions on device: " + questionsReached);
                System.out.println("activeIDs: " + activeIDs);
                System.out.println("device IDs: " + deviceQuestions);
            }
        }
        return  questionsReached;
    }

    public void mergeStudentsOnNameOrIP(Student studentToMerge) {
        ArrayList<Student> studentsToMerge = new ArrayList<>();
        for (int i = 0; i < students_vector.size(); i++) {
            if (!students_vector.get(i).getName().contentEquals("no name") && studentToMerge.getName().contentEquals(students_vector.get(i).getName())) {
                studentsToMerge.add(students_vector.get(i));
            } else if (students_vector.get(i).getInetAddress() != null && studentToMerge.getInetAddress().equals(students_vector.get(i).getInetAddress())) {
                studentsToMerge.add(students_vector.get(i));
            }
        }

        if (studentsToMerge.size() > 0) {
            if (studentToMerge.getInetAddress() != null && studentToMerge.getInetAddress().toString().length() > 0 && !studentToMerge.getName().contentEquals("no name")
                    && !studentToMerge.getUniqueID().contentEquals("no identifier")) {
                for (Student student : studentsToMerge) {
                    students_vector.remove(student);
                }
                students_vector.add(studentToMerge);
            } else if (studentsToMerge.get(0).getInetAddress() != null
                    && !studentsToMerge.get(0).getUniqueID().contentEquals("no identifier")
                    && !studentsToMerge.get(0).getName().contentEquals("no name")) {
                System.out.println("mergeStudentsOnNameOrIP: the present student is probably more complete. we do nothing.");
            } else if (!studentToMerge.getName().contentEquals("no name") && !studentsToMerge.get(0).getName().contentEquals("no name")
                    && studentsToMerge.get(0).getInetAddress() != null && studentsToMerge.get(0).getInetAddress().toString().length() > 0) {
                students_vector.remove(studentsToMerge.get(0));
                students_vector.add(studentToMerge);
            } else if  (studentsToMerge.get(0).getInetAddress() == null && studentToMerge.getInetAddress() == null &&
                    studentsToMerge.get(0).getName().contentEquals(studentToMerge.getName()) &&
                    studentsToMerge.get(0).getUniqueID().contentEquals(studentToMerge.getUniqueID()) &&
                    studentsToMerge.get(0).getStudentID().equals(studentToMerge.getStudentID())) {
                System.out.println("mergeStudentsOnNameOrIP: merging probably equal objects. we do nothing");
            } else {
                System.out.println("mergeStudentsOnNameOrIP: implementation not complete.");
            }
        }
    }
}
