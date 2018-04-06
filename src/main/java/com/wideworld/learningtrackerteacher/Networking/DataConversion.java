package com.wideworld.learningtrackerteacher.Networking;

import com.wideworld.learningtrackerteacher.questions_management.Test;

public class DataConversion {
    public byte[] testToBytesArray(Test test) {
        String testString = "";
        testString += Integer.valueOf(test.getIdTest()) + "///";
        testString += test.getTestName() + "///";

        for (int i = 0; i < test.getObjectives().size() && i < test.getObjectivesIDs().size(); i++) {
            testString += test.getObjectivesIDs().get(i) + "/|/";
            testString += test.getObjectives().get(i) + "|||";
        }
        testString += "///";

        byte[] bytearraytest = testString.getBytes();
        return  bytearraytest;
    }
}
