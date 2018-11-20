package koeko.Networking;

import koeko.Koeko;
import koeko.controllers.SettingsController;
import koeko.database_management.DbTableQuestionMultipleChoice;
import koeko.database_management.DbTableStudents;
import koeko.students_management.Classroom;
import koeko.students_management.Student;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceptionProtocol {
    static private AtomicBoolean startingNearby = new AtomicBoolean(false);

    static public Student receivedCONN(Student arg_student, String answerString, Classroom aClass) {
        Student student = aClass.getStudentWithIPAndUUID(arg_student.getInetAddress(), answerString.split("///")[1]);
        if (student == null) {
            student = arg_student;
        } else {
            student.setInputStream(arg_student.getInputStream());
            student.setOutputStream(arg_student.getOutputStream());
        }
        student.setConnected(true);
        student.setUniqueDeviceID(answerString.split("///")[1]);
        student.setName(answerString.split("///")[2]);
        String studentID = DbTableStudents.addStudent(answerString.split("///")[1], answerString.split("///")[2]);
        if (studentID.contentEquals("-2")) {
            NetworkCommunication.networkCommunicationSingleton.popUpIfStudentIdentifierCollision(student.getName());
        }
        student.setStudentID(studentID);

        //get the device infos if android
        extractInfos(answerString, student);

        NetworkState networkState = NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton();
        networkState.getStudentsToConnectionStatus().add(student.getUniqueDeviceID());

        //update the tracking of questions on device
        if (networkState.getStudentsToActiveIdMap().get(student.getUniqueDeviceID()) == null) {
            networkState.getStudentsToSyncedIdsMap().put(student.getUniqueDeviceID(), new CopyOnWriteArrayList<>());
            networkState.getStudentsToReadyMap().put(student.getUniqueDeviceID(), 0);
            networkState.getStudentsToActiveIdMap().put(student.getUniqueDeviceID(), "");
        }

        NetworkCommunication.networkCommunicationSingleton.getLearningTrackerController().addUser(student, true);

        NetworkCommunication.networkCommunicationSingleton.sendString(arg_student, "CONNECTED///");
        activateNearbyIfNecessary(0);

        return student;
    }

    private static void extractInfos(String answerString, Student student) {
        if (answerString.split("///").length >= 4) {
            String[] infos = answerString.split("///")[3].split(":");
            if (infos.length >= 4) {
                Integer sdklevel = 0;
                Long googleServicesVersion = 0L;
                Boolean ble = false;
                Integer hotspotAvailable = 0;
                String deviceModel = "";
                try {
                    sdklevel = Integer.valueOf(infos[1]);

                    if (infos[2].contentEquals("BLE")) {
                        ble = true;
                    }
                    googleServicesVersion = Long.valueOf(infos[3]);
                    hotspotAvailable = Integer.valueOf(infos[4]);
                    deviceModel = infos[5];
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                DeviceInfo deviceInfo = new DeviceInfo(student.getUniqueDeviceID(), infos[0], sdklevel, ble, googleServicesVersion,
                        hotspotAvailable, deviceModel);

                //don't classify device if he is reconnecting after verticalizing
                for (SubNet subNet : NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getSubNets()) {
                    if (subNet.getAdvertiser().getUniqueId().contentEquals(deviceInfo.getUniqueId())) return;
                    if (subNet.getDiscoverer().getUniqueId().contentEquals(deviceInfo.getUniqueId())) return;
                    for (DeviceInfo clientsInfos : subNet.getClients()) {
                        if (clientsInfos.getUniqueId().contentEquals(deviceInfo.getUniqueId())) return;
                    }
                }
                NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().classifiyNewDevice(deviceInfo);
            }
        } else {
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton()
                    .classifiyNewDevice(new DeviceInfo(student.getUniqueDeviceID(), "IOS", 0, false, 0L, 0, "IOS"));
        }
    }

    public static void receivedRESIDS(String answerString, NetworkState networkState, Student student) {
        if (SettingsController.forceSync == 0 && answerString.split("///").length >= 3) {
            String[] resourceIds = answerString.split("///")[2].split("\\|");
            for (int i = 0; i < resourceIds.length; i++) {
                if (resourceIds[i].split(";").length > 1) {
                    String teachersHash = DbTableQuestionMultipleChoice.getResourceHashCode(resourceIds[i].split(";")[0]);
                    String studentsHash = resourceIds[i].split(";")[1];
                    if (teachersHash != null && studentsHash != null && teachersHash.contentEquals(studentsHash)) {
                        networkState.getStudentsToSyncedIdsMap().get(answerString.split("///")[1])
                                .add(resourceIds[i].split(";")[0]);
                    }
                } else {
                    networkState.getStudentsToSyncedIdsMap().get(answerString.split("///")[1])
                            .add(resourceIds[i].split(";")[0]);
                }
            }
        }

        if (answerString.contains("ENDTRSM")) {
            NetworkCommunication.networkCommunicationSingleton.sendActiveIds(student);
        }
    }

    private static void activateNearbyIfNecessary(int trials) {
        if (!startingNearby.get()) {
            startingNearby.set(true);
            NetworkState networkState = NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton();

            if (NetworkCommunication.network_solution == 1 && networkState.nextSubNet <= networkState.numberDesiredHotspots) {
                System.out.println("Checking verticalizing possibilities");

                //activate new Subnet
                DeviceInfo advertiser = networkState.popAdvertiser();
                DeviceInfo discoverer = networkState.popDiscoverer();
                if (advertiser != null && discoverer != null) {
                    NetworkCommunication.networkCommunicationSingleton.activateSubnet(advertiser, discoverer);
                    if (trials < 4) {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        for (SubNet subNet : NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getSubNets()) {
                            if (subNet.getAdvertiser().getUniqueId().contentEquals(advertiser.getUniqueId())) {
                                if (!subNet.getOnline()) {
                                    System.out.println("Verticalizing failed");
                                    startingNearby.set(false);
                                    NetworkState.subnetResult(subNet, 0);
                                    activateNearbyIfNecessary(++trials);
                                }
                            }
                        }
                    }
                } else {
                    if (advertiser != null) {
                        networkState.classifiyNewDevice(advertiser);
                    }
                    if (discoverer != null) {
                        networkState.classifiyNewDevice(discoverer);
                    }
                    System.out.println("Can't start subnet yet");
                }
            }
            startingNearby.set(false);
        }
    }
}
