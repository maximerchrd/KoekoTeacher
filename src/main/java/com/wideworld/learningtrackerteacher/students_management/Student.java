package com.wideworld.learningtrackerteacher.students_management;


import com.wideworld.learningtrackerteacher.questions_management.QuestionGeneric;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by maximerichard on 14/02/17.
 */
public class Student {
    private String mMacAddress = "no address";
    private InetAddress mInetAddress = null;
    private String mName = "no name";
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private double overallPercentage = -1;
    private Boolean mConnectedByBT = false;
    private Integer numberOfAnswers;
    private Integer studentID = -1;
    private ArrayList<Integer> testQuestionsIDs;
    private Boolean connected;


    //constructors
    public Student() {
        numberOfAnswers = 0;
        testQuestionsIDs = new ArrayList<>();
    }
    public Student(String arg_MacAddress, String arg_name) {
        mMacAddress = arg_MacAddress;
        mName = arg_name;
        numberOfAnswers = 0;
        testQuestionsIDs = new ArrayList<>();
    }
    public Student(String arg_address, String arg_name, Boolean connectedByBT) {
        mMacAddress = arg_address;
        mName = arg_name;
        mConnectedByBT = connectedByBT;
        numberOfAnswers = 0;
        testQuestionsIDs = new ArrayList<>();
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
    public void setAddress(String arg_MacAddress) {
        mMacAddress = arg_MacAddress;
    }
    public void setName(String arg_name) {
        mName = arg_name;
    }
    public void setmConnectedByBT(Boolean connectedByBT) {
        mConnectedByBT = connectedByBT;
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
    public void setTestQuestions(ArrayList<Integer> testQuestions) {
        this.testQuestionsIDs = testQuestions;
    }

    //getters
    public Boolean getConnected() {
        return connected;
    }
    public ArrayList<Integer> getTestQuestions() {
        return testQuestionsIDs;
    }
    public Integer getNumberOfAnswers() {
        return numberOfAnswers;
    }
    public InetAddress getInetAddress() {
        return mInetAddress;
    }
    public String getAddress() {
        return mMacAddress;
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
    public Boolean getmConnectedByBT() {
        return mConnectedByBT;
    }
    public Integer getStudentID() {
        return studentID;
    }

    public Integer getNextQuestionID(Integer formerID) {
        Integer nextQuestion = -1;
        if (testQuestionsIDs != null) {
            for (int i = 0; i < testQuestionsIDs.size() - 1; i++) {
                Integer id = testQuestionsIDs.get(i);
                if (id.compareTo(formerID) == 0) {
                    nextQuestion = testQuestionsIDs.get(i + 1);
                    testQuestionsIDs.set(i, -1);
                }
            }
        }
        return  nextQuestion;
    }
}