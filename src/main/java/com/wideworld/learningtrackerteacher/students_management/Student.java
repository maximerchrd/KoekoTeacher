package com.wideworld.learningtrackerteacher.students_management;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

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


    //constructors
    public Student() {
        numberOfAnswers = 0;
    }
    public Student(String arg_MacAddress, String arg_name) {
        mMacAddress = arg_MacAddress;
        mName = arg_name;
        numberOfAnswers = 0;
    }
    public Student(String arg_address, String arg_name, Boolean connectedByBT) {
        mMacAddress = arg_address;
        mName = arg_name;
        mConnectedByBT = connectedByBT;
        numberOfAnswers = 0;
    }
    public void increaseNumberOfAnswers () {
        numberOfAnswers++;
    }
    //setters

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
    //getters
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
}
