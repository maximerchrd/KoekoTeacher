package koeko.Networking;

// import QuestionSendingController;
import koeko.controllers.SettingsController;
import koeko.database_management.DbTableRelationQuestionQuestion;
import koeko.questions_management.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DataConversion {
    static public byte[] testToBytesArray(Test test) {
        String testString = "";
        testString += test.getIdTest() + "///";
        testString += test.getTestName() + "///";
        String testMap = DbTableRelationQuestionQuestion.getFormattedQuestionsLinkedToTest(test.getTestName());
        testString += testMap;

        //insert the question ids into the test
        ArrayList<String> questionIDs = new ArrayList<>();
        String[] questionMapIDs = testMap.split("\\|\\|\\|");
        for (int i = 0; i < questionMapIDs.length; i++) {
            if (!questionMapIDs[i].contentEquals("")) {
                questionIDs.add(questionMapIDs[i].split(";;;")[0]);
            }
        }
        test.setIdsQuestions(questionIDs);

        testString += "///";

        for (int i = 0; i < test.getObjectives().size() && i < test.getObjectivesIDs().size(); i++) {
            testString += test.getObjectivesIDs().get(i) + "/|/";
            testString += test.getObjectives().get(i) + "|||";
        }
        testString += "///";
        testString += test.getTestMode() + "///";
        testString += test.getMedalsInstructions() + "///";

        byte[] bytearraytest = testString.getBytes();
        String textDataSize = String.valueOf(bytearraytest.length);
        String prefix = "TEST:" + textDataSize + "///";
        byte[] byteArrayPrefix = prefix.getBytes();
        byte[] wholeByteArray = new byte[80];
        for (int i = 0; i < byteArrayPrefix.length; i++) {
            wholeByteArray[i] = byteArrayPrefix[i];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            synchronized (outputStream) {
                outputStream.write(wholeByteArray);
                outputStream.write(bytearraytest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        wholeByteArray = outputStream.toByteArray( );

        return  wholeByteArray;
    }

    static public byte[] testEvalToBytesArray(String evalToSend) {
        byte[] bytearraytesteval = evalToSend.getBytes();
        String textDataSize = String.valueOf(bytearraytesteval.length);
        String prefix = "OEVAL:" + textDataSize + "///";
        byte[] byteArrayPrefix = prefix.getBytes();
        byte[] wholeByteArray = new byte[80];
        for (int i = 0; i < byteArrayPrefix.length; i++) {
            wholeByteArray[i] = byteArrayPrefix[i];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            synchronized (outputStream) {
                outputStream.write(wholeByteArray);
                outputStream.write(bytearraytesteval);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        wholeByteArray = outputStream.toByteArray( );

        return  wholeByteArray;
    }

    static public byte[] questionsSetToBytesArray(ArrayList<Integer> questionIDs, Integer testMode) {
        String testString = "";

        for (Integer questionId : questionIDs) {
            testString += String.valueOf(questionId) + "///";
        }

        byte[] bytearraytest = testString.getBytes();
        String textDataSize = String.valueOf(bytearraytest.length);
        String prefix = "TESYN:" + textDataSize + ":" + SettingsController.correctionMode + ":" + testMode +  "///";
        byte[] byteArrayPrefix = prefix.getBytes();
        byte[] wholeByteArray = new byte[80];
        for (int i = 0; i < byteArrayPrefix.length; i++) {
            wholeByteArray[i] = byteArrayPrefix[i];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            synchronized (outputStream) {
                outputStream.write(wholeByteArray);
                outputStream.write(bytearraytest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        wholeByteArray = outputStream.toByteArray( );

        return  wholeByteArray;
    }
}
