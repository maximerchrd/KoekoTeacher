package com.wideworld.learningtrackerteacher.Networking;

import com.sun.tools.javac.util.ArrayUtils;
import com.wideworld.learningtrackerteacher.controllers.QuestionSendingController;
import com.wideworld.learningtrackerteacher.questions_management.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class DataConversion {
    static public byte[] testToBytesArray(Test test) {
        String testString = "";
        testString += Integer.valueOf(test.getIdTest()) + "///";
        testString += test.getTestName() + "///";

        for (int i = 0; i < test.getObjectives().size() && i < test.getObjectivesIDs().size(); i++) {
            testString += test.getObjectivesIDs().get(i) + "/|/";
            testString += test.getObjectives().get(i) + "|||";
        }
        testString += "///";

        byte[] bytearraytest = testString.getBytes();
        String textDataSize = String.valueOf(bytearraytest.length);
        String prefix = "TEST:" + textDataSize + "///";
        byte[] byteArrayPrefix = prefix.getBytes();
        byte[] wholeByteArray = new byte[40];
        for (int i = 0; i < byteArrayPrefix.length; i++) {
            wholeByteArray[i] = byteArrayPrefix[i];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(wholeByteArray);
            outputStream.write(bytearraytest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        wholeByteArray = outputStream.toByteArray( );

        return  wholeByteArray;
    }

    static public byte[] questionsSetToBytesArray(ArrayList<Integer> questionIDs) {
        String testString = "";

        for (Integer questionId : questionIDs) {
            testString += String.valueOf(questionId) + "///";
        }

        byte[] bytearraytest = testString.getBytes();
        String textDataSize = String.valueOf(bytearraytest.length);
        String prefix = "TESYN:" + textDataSize + "///";
        byte[] byteArrayPrefix = prefix.getBytes();
        byte[] wholeByteArray = new byte[40];
        for (int i = 0; i < byteArrayPrefix.length; i++) {
            wholeByteArray[i] = byteArrayPrefix[i];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(wholeByteArray);
            outputStream.write(bytearraytest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        wholeByteArray = outputStream.toByteArray( );

        return  wholeByteArray;
    }
}
