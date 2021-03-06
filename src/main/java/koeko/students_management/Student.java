package koeko.students_management;


import koeko.questions_management.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by maximerichard on 14/02/17.
 */
public class Student {
    static public String UNITIALIZED_UUID = "no identifier";

    private String uniqueDeviceID = UNITIALIZED_UUID;
    private String studentID = "-1";
    private InetAddress mInetAddress = null;
    private String port = "";
    private String mName = "no name";
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private double overallPercentage = -1;
    private Integer numberOfAnswers;
    private ArrayList<String> testQuestionsIDs;
    private Integer activeTestID;
    private Test activeTest;
    private Boolean connected = false;
    private ArrayList<String> deviceQuestions;
    private Boolean homeworkChecked = false;


    //constructors
    public Student() {
        numberOfAnswers = 0;
        testQuestionsIDs = new ArrayList<>();
        activeTest = new Test();
        deviceQuestions = new ArrayList<>();
        try {
            mInetAddress = InetAddress.getByName("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public Student(String arg_MacAddress, String arg_name) {
        uniqueDeviceID = arg_MacAddress;
        mName = arg_name;
        numberOfAnswers = 0;
        testQuestionsIDs = new ArrayList<>();
        activeTest = new Test();
        deviceQuestions = new ArrayList<>();
        try {
            mInetAddress = InetAddress.getByName("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public Student(String arg_address, String arg_name, Boolean connectedByBT) {
        uniqueDeviceID = arg_address;
        mName = arg_name;
        numberOfAnswers = 0;
        testQuestionsIDs = new ArrayList<>();
        activeTest = new Test();
        deviceQuestions = new ArrayList<>();
        try {
            mInetAddress = InetAddress.getByName("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public void increaseNumberOfAnswers () {
        numberOfAnswers++;
    }
    //setters
    public void setConnected(Boolean connected) {
        this.connected = connected;
    }
    public void setInetAddress(InetAddress arg_inetaddress) {
        mInetAddress = arg_inetaddress;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public void setUniqueDeviceID(String arg_MacAddress) {
        uniqueDeviceID = arg_MacAddress;
    }
    public void setName(String arg_name) {
        mName = arg_name;
    }
    public void setOutputStream(OutputStream arg_outputstream) {
        mOutputStream = arg_outputstream;
    }
    public void setInputStream(InputStream arg_inputstream) {
        mInputStream = arg_inputstream;
    }
    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
    public void setTestQuestions(ArrayList<String> testQuestions) {
        this.testQuestionsIDs = testQuestions;
    }
    public void setActiveTestID(Integer activeTestID) {
        this.activeTestID = activeTestID;
    }
    public void setActiveTest(Test activeTest) {
        this.activeTest = activeTest;
    }
    public void setDeviceQuestions(ArrayList<String> deviceQuestions) {
        this.deviceQuestions = deviceQuestions;
    }
    public void setHomeworkChecked(Boolean homeworkChecked) {
        this.homeworkChecked = homeworkChecked;
    }

    //getters
    public Test getActiveTest() {
        return activeTest;
    }
    public Integer getActiveTestID() {
        return activeTestID;
    }
    public Boolean getConnected() {
        return connected;
    }
    public ArrayList<String> getTestQuestions() {
        return testQuestionsIDs;
    }
    public Integer getNumberOfAnswers() {
        return numberOfAnswers;
    }
    public InetAddress getInetAddress() {
        return mInetAddress;
    }
    public String getPort() {
        return port;
    }
    public String getUniqueDeviceID() {
        return uniqueDeviceID;
    }
    public  String getName() {
        return  mName;
    }
    public OutputStream getOutputStream() {
        return mOutputStream;
    }
    public InputStream getInputStream() {
        return mInputStream;
    }
    public String getStudentID() {
        return studentID;
    }
    public Boolean getHomeworkChecked() {
        return homeworkChecked;
    }

    public void copyStudent(Student student) {
        this.setHomeworkChecked(student.getHomeworkChecked());
        this.setName(student.getName());
        this.setStudentID(student.getStudentID());
        this.setConnected(student.getConnected());
        this.setActiveTest(student.getActiveTest());
        this.setActiveTestID(student.getActiveTestID());
        this.setUniqueDeviceID(student.getUniqueDeviceID());
        this.setInetAddress(student.getInetAddress());
        this.setInputStream(student.getInputStream());
        this.setOutputStream(student.getOutputStream());
        this.setPort(student.getPort());
        this.setTestQuestions(student.getTestQuestions());
        this.setDeviceQuestions(student.getDeviceQuestions());
    }

    public ArrayList<String> getDeviceQuestions() {
        /*if (deviceQuestions.size() == 0) {
            Vector<String> IDs = DbTableRelationQuestionStudent.getQuestionsIdsForStudent(this.mName);
            for (String id : IDs) {
                deviceQuestions.add(id);
            }
        }*/
        return deviceQuestions;
    }

    public String getNextQuestionID(String formerID) {
        String nextQuestion = "-1";
        if (!activeTest.getSynchroneousQuestionsTest()) {
            if (testQuestionsIDs != null) {
                for (int i = 0; i < testQuestionsIDs.size() - 1; i++) {
                    String id = testQuestionsIDs.get(i);
                    if (id.compareTo(formerID) == 0) {
                        nextQuestion = testQuestionsIDs.get(i + 1);
                        testQuestionsIDs.set(i, "-1");
                    }
                }
            }
        }
        return  nextQuestion;
    }

    public void updateQuestionsTracking(String answerString) {
        String IdsArray[] = answerString.split("///")[3].split("|");
        ArrayList<String> IdsList = new ArrayList<>(Arrays.asList(IdsArray));
        for (String id : IdsList) {
            if (id.length() < 2) {
                IdsList.remove(id);
            }
        }
        ArrayList<String> studentsIdsDates = getDeviceQuestions();
        //implement tracking...
    }

    static public void essentialCopyStudent(Student copyFromStudent, Student copyToStudent) {
        copyToStudent.setName(copyFromStudent.getName());
        copyToStudent.setUniqueDeviceID(copyFromStudent.getUniqueDeviceID());
    }
}