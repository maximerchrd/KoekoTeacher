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
    private String uniqueID = "no identifier";
    private InetAddress mInetAddress = null;
    private String mName = "no name";
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private double overallPercentage = -1;
    private Integer numberOfAnswers;
    private Integer studentID = -1;
    private ArrayList<String> testQuestionsIDs;
    private Integer activeTestID;
    private Test activeTest;
    private Boolean connected = false;
    private ArrayList<String> deviceQuestions;


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
        uniqueID = arg_MacAddress;
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
        uniqueID = arg_address;
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
    public void setUniqueID(String arg_MacAddress) {
        uniqueID = arg_MacAddress;
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
    public void setStudentID(Integer studentID) {
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
    public String getUniqueID() {
        return uniqueID;
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
    public Integer getStudentID() {
        return studentID;
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
        copyToStudent.setUniqueID(copyFromStudent.getUniqueID());
    }
}