package koeko.Networking;

import koeko.controllers.SettingsController;
import koeko.database_management.DbTableIndividualQuestionForStudentResult;
import koeko.database_management.DbTableStudents;
import koeko.students_management.Classroom;
import koeko.students_management.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReceptionProtocol {

    static public void receivedCONN(Student arg_student, String answerString, Classroom aClass) {
        Student student = aClass.getStudentWithIP(arg_student.getInetAddress().toString());
        student.setConnected(true);
        student.setUniqueDeviceID(answerString.split("///")[1]);
        student.setName(answerString.split("///")[2]);
        String studentID = DbTableStudents.addStudent(answerString.split("///")[1], answerString.split("///")[2]);
        if (studentID.contentEquals("-2")) {
            NetworkCommunication.networkCommunicationSingleton.popUpIfStudentIdentifierCollision(student.getName());
        }
        student.setStudentID(studentID);

        //get the device infos if android
        if (answerString.split("///").length >= 4) {
            String[] infos = answerString.split("///")[3].split(":");
            if (infos.length >= 4) {
                Integer sdklevel = 0;
                try {
                    sdklevel = Integer.valueOf(infos[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                Boolean ble = false;
                if (infos[2].contentEquals("BLE")) {
                    ble = true;
                }
                Long googleServicesVersion = 0L;
                try {
                    googleServicesVersion = Long.valueOf(infos[3]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                DeviceInfo deviceInfo = new DeviceInfo(student.getUniqueDeviceID(), infos[0], sdklevel, ble, googleServicesVersion);
                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getStudentsToDeviceInfos()
                        .put(student.getUniqueDeviceID(), deviceInfo);
            }
        } else {
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getStudentsToDeviceInfos()
                    .put(student.getUniqueDeviceID(), new DeviceInfo(student.getUniqueDeviceID(),"IOS", 0, false, 0L));
        }

        NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getStudentsToConnectionStatus()
                .add(student.getUniqueDeviceID());

        //update the tracking of questions on device
        if (NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton()
                .getStudentsToActiveIdMap().get(student.getUniqueDeviceID()) == null) {
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton()
                    .getStudentsToSyncedIdsMap().put(student.getUniqueDeviceID(), new CopyOnWriteArrayList<>());
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton()
                    .getStudentsToReadyMap().put(student.getUniqueDeviceID(), 0);
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton()
                    .getStudentsToActiveIdMap().put(student.getUniqueDeviceID(), "");
        }

        activateNearbyIfNecessary();

        NetworkCommunication.networkCommunicationSingleton.getLearningTrackerController().addUser(student, true);

        aClass.updateStudentButNotStreams(student);
    }

    static public void receivedANSW(Student arg_student, String answerString, ArrayList<ArrayList<Integer>> questionIdsForGroups,
                                    ArrayList<ArrayList<String>> studentNamesForGroups) {
        double eval = DbTableIndividualQuestionForStudentResult.addIndividualQuestionForStudentResult(answerString.split("///")[5],
                answerString.split("///")[2], answerString.split("///")[3], answerString.split("///")[0]);
        NetworkCommunication.networkCommunicationSingleton.sendEvaluation(eval, answerString.split("///")[5], arg_student);

        //find out to which group the student and answer belong
        Integer groupIndex = 0;
        Integer questID = Integer.valueOf(answerString.split("///")[5]);
        for (int i = 0; i < studentNamesForGroups.size(); i++) {
            if (studentNamesForGroups.get(i).contains(arg_student.getName()) && questionIdsForGroups.get(i).contains(questID)) {
                groupIndex = i;
                questionIdsForGroups.get(i).remove(questID);
            }
        }
        NetworkCommunication.networkCommunicationSingleton.getLearningTrackerController().addAnswerForUser(arg_student,
                answerString.split("///")[3], answerString.split("///")[4], eval,
                answerString.split("///")[5], groupIndex);
        String nextQuestion = arg_student.getNextQuestionID(answerString.split("///")[5]);
        System.out.println("student: " + arg_student.getName() + ";former question: " + questID + "; nextQuestion:" + nextQuestion);
        for (String testid : arg_student.getTestQuestions()) {
            System.out.println(testid);
        }
        if (!nextQuestion.contentEquals("-1")) {
            Vector<Student> singleStudent = new Vector<>();
            singleStudent.add(arg_student);
            NetworkCommunication.networkCommunicationSingleton.sendQuestionID(nextQuestion, singleStudent);
        }

        //set evaluation if question belongs to a test
        if (arg_student.getActiveTest().getIdsQuestions().contains(questID)) {
            System.out.println("inserting question evaluation for test");
            int questionIndex = arg_student.getActiveTest().getIdsQuestions().indexOf(questID);
            if (questionIndex < arg_student.getActiveTest().getQuestionsEvaluations().size() && questionIndex >= 0) {
                arg_student.getActiveTest().getQuestionsEvaluations().set(questionIndex, eval);
            }
            Boolean testCompleted = true;
            for (Double questEval : arg_student.getActiveTest().getQuestionsEvaluations()) {
                if (questEval < 0) {
                    testCompleted = false;
                }
            }
            if (testCompleted) {
                Double testEval = 0.0;
                for (Double questEval : arg_student.getActiveTest().getQuestionsEvaluations()) {
                    testEval += questEval;
                }
                testEval = testEval / arg_student.getActiveTest().getQuestionsEvaluations().size();
                arg_student.getActiveTest().setTestEvaluation(testEval);
                DbTableIndividualQuestionForStudentResult.addIndividualTestEval(arg_student.getActiveTest().getIdTest(), arg_student.getName(), testEval);
            }
        }
    }

    public static void receivedRESIDS(String answerString, NetworkState networkState, Student student) {
        if (SettingsController.forceSync == 0 && answerString.split("///").length >= 3) {
            String[] resourceIds = answerString.split("///")[2].split("\\|");
            networkState.getStudentsToSyncedIdsMap().get(answerString.split("///")[1])
                    .addAll(new ArrayList<>(Arrays.asList(resourceIds)));
        }

        if (answerString.contains("ENDTRSM")) {
            NetworkCommunication.networkCommunicationSingleton.sendActiveIds(student);
        }
    }

    private static void activateNearbyIfNecessary() {
        NetworkState networkState = NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton();
        if (NetworkCommunication.network_solution == 1 && networkState.getStudentsToConnectionStatus().size() >= NetworkCommunication.maximumSupportedDevices - 1) {
            System.out.println("Trying to activate nearby connections");
            ArrayList<DeviceInfo> potentialAdvertisers = new ArrayList<>();
            ArrayList<DeviceInfo> potentialDiscoverers = new ArrayList<>();
            for (Map.Entry<String, DeviceInfo> entry : networkState.getStudentsToDeviceInfos().entrySet()) {
                DeviceInfo deviceInfo = entry.getValue();
                if (deviceInfo.getGoogleServicesVersion() >= 12451000) {
                    if (deviceInfo.getBle()) {
                        potentialAdvertisers.add(deviceInfo);
                    } else if (deviceInfo.getOs().contentEquals("android")) {
                        potentialDiscoverers.add(deviceInfo);
                    }
                }
            }

            if (potentialAdvertisers.size() > 0) {
                DeviceInfo mostRecentAndroid = new DeviceInfo();
                DeviceInfo discoverer = new DeviceInfo();
                for (DeviceInfo deviceInfo : potentialAdvertisers) {
                    if (mostRecentAndroid.getSdkLevel() < deviceInfo.getSdkLevel()) {
                        mostRecentAndroid = deviceInfo;
                    }
                }
                potentialAdvertisers.remove(mostRecentAndroid);

                if (potentialAdvertisers.size() > 5) {
                    discoverer = potentialAdvertisers.get(0);
                } else if (potentialDiscoverers.size() > 0) {
                    discoverer = potentialDiscoverers.get(0);
                } else if (potentialAdvertisers.size() > 0) {
                    discoverer = potentialAdvertisers.get(0);
                } else {
                    System.out.println("PROBLEM: Wifi saturated without possibility of starting new nearby node (less than 2 suitable androids)");
                }

                NetworkCommunication.networkCommunicationSingleton.activateAsAdvertiser(mostRecentAndroid.getUniqueId());
                NetworkCommunication.networkCommunicationSingleton.activateAsDiscoverer(discoverer.getUniqueId());
            } else {
                System.out.println("PROBLEM: Wifi saturated without possibility of starting new nearby node");
            }
        }
    }
}
