package koeko.Networking;

import koeko.Koeko;
import koeko.database_management.DbTableSubnetResult;
import koeko.students_management.Student;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkState {
    private Map<String, CopyOnWriteArrayList<String>> studentsToSyncedIdsMap;
    private List<String> questionIdsToSend;

    static public int STUDENT_NOT_SYNCED = 0;
    static public int STUDENT_SYNCED = 1;
    private Map<String, Integer> studentsToReadyMap;

    private String activeID = "";
    private Map<String, String> studentsToActiveIdMap;
    private Map<String, DeviceInfo> studentsToDeviceInfos;

    //remove studentsToConnectionStatus ?
    private List<String> studentsToConnectionStatus;

    //hotspots
    public Integer numberDesiredHotspots = 2;
    private String hotspotName = "koeko";
    private String hotspotPassword = "12345678";
    public int nextSubNet = 1;
    private List<SubNet> subNets;

    static private Long minimumGoogleServiceVersion = 12451000L;
    private ArrayList<DeviceInfo> potentialAdvertisers;         //Android with BTE
    private ArrayList<DeviceInfo> potentialDiscoverers;         //Android without BTE and failed BTE or Hotspot
    private ArrayList<DeviceInfo> potentialThirdLayerDevices;   //From IOS 11 and up and Android without BT
    private ArrayList<DeviceInfo> onlyFirstLayerDevices;        //From IOS 10 and down

    public NetworkState() {
        this.studentsToSyncedIdsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.questionIdsToSend = new CopyOnWriteArrayList<>();
        this.studentsToReadyMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.studentsToActiveIdMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.studentsToDeviceInfos = Collections.synchronizedMap(new LinkedHashMap<>());
        this.studentsToConnectionStatus = new CopyOnWriteArrayList<>();
        this.subNets = new CopyOnWriteArrayList<>();
        this.potentialAdvertisers = new ArrayList<>();
        this.potentialDiscoverers = new ArrayList<>();
        this.potentialThirdLayerDevices = new ArrayList<>();
        this.onlyFirstLayerDevices = new ArrayList<>();
    }

    public void toggleSyncStateForStudent(Student student, Integer state) {
        if (studentsToReadyMap.get(student.getUniqueDeviceID()) != null) {
            if (studentsToReadyMap.get(student.getUniqueDeviceID()) == 0 && state == 1) {
                Koeko.studentsVsQuestionsTableControllerSingleton.setStatusQuestionsReceived(student, STUDENT_SYNCED);
                studentsToReadyMap.put(student.getUniqueDeviceID(), state);
            } else if (studentsToReadyMap.get(student.getUniqueDeviceID()) == 1 && state == 0) {
                Koeko.studentsVsQuestionsTableControllerSingleton.setStatusQuestionsReceived(student, STUDENT_NOT_SYNCED);
                studentsToReadyMap.put(student.getUniqueDeviceID(), state);
            }
        }
    }

    public void toggleSyncStateForStudent(ArrayList<Student> students, Integer state) {
        if (state == 0) {
            for (Student student : students) {
                if (studentsToReadyMap.get(student.getUniqueDeviceID()) == 1) {
                    studentsToReadyMap.put(student.getUniqueDeviceID(), 0);
                    Koeko.studentsVsQuestionsTableControllerSingleton.setStatusQuestionsReceived(student, STUDENT_NOT_SYNCED);
                }
            }
        } else {
            System.err.println("Case not accounted for in toggleSyncStateForStudent");
        }
    }

    public void unsyncIdAfterUpdate(String id) {
        for (CopyOnWriteArrayList<String> studentsIds : studentsToSyncedIdsMap.values()) {
            studentsIds.remove(id);
        }
    }

    public void classifiyNewDevice(DeviceInfo deviceInfo) {
        System.out.println("Classifiying new device: " + deviceInfo.getSdkLevel() + "; " + deviceInfo.getBle());
        if (deviceInfo.getOs().contentEquals("android") && deviceInfo.getBle()
                && deviceInfo.getGoogleServicesVersion() >= minimumGoogleServiceVersion) {
            potentialAdvertisers.add(deviceInfo);
        } else if (deviceInfo.getOs().contentEquals("android")
                && deviceInfo.getGoogleServicesVersion() >= minimumGoogleServiceVersion) {
            potentialDiscoverers.add(deviceInfo);
        } else if (deviceInfo.getOs().contentEquals("android")
                || (deviceInfo.getOs().contentEquals("IOS") && deviceInfo.getSdkLevel() >= 11)) {
            potentialThirdLayerDevices.add(deviceInfo);
        } else {
            onlyFirstLayerDevices.add(deviceInfo);
        }
        studentsToDeviceInfos.put(deviceInfo.getUniqueId(), deviceInfo);
    }

    public DeviceInfo popAdvertiser() {
        return getBetterDevice(potentialAdvertisers, false);
    }

    public DeviceInfo popDiscoverer() {
        DeviceInfo discoverer = getBetterDevice(potentialDiscoverers, true);
        if (discoverer == null) {
            discoverer = getBetterDevice(potentialAdvertisers, true);
        }
        if (discoverer == null) {
            System.out.println("PROBLEM: Wifi saturated without possibility of starting new nearby node (less than 2 suitable androids)");
        }
        return discoverer;
    }

    private DeviceInfo getBetterDevice(ArrayList<DeviceInfo> deviceInfosList, Boolean hotspotNeeded) {
        DeviceInfo betterAndroid = null;
        Integer nbSuccess = 0;
        Integer nbFails = 0;
        for (DeviceInfo deviceInfo : deviceInfosList) {
            Integer[] score = getDeviceScore(deviceInfo.getUniqueId(), SubNetConst.ADVERTISER);
            if (betterAndroid == null) {
                if (!hotspotNeeded || deviceInfo.getHotspotAvailable() == 1) {
                    betterAndroid = deviceInfo;
                    nbSuccess = score[0];
                    nbFails = score[1];
                }
            } else {
                // chose device according to 1: rate success/fail; 2: nb of success; 3: higher sdk level
                double presentBest = (double)nbSuccess / ((double)nbSuccess + (double)nbFails);
                if (Double.isNaN(presentBest)) presentBest = 0.0;
                double newResult = (double)score[0] / ((double)score[0] + (double)score[1]);
                if (Double.isNaN(newResult)) newResult = 0.0;
                if (newResult >= presentBest ) {
                    if (newResult == presentBest) {
                        if (score[1] < nbFails) {
                            if (!hotspotNeeded || deviceInfo.getHotspotAvailable() == 1) {
                                betterAndroid = deviceInfo;
                                nbSuccess = score[0];
                                nbFails = score[1];
                            }
                        } else if (betterAndroid.getSdkLevel() < deviceInfo.getSdkLevel()) {
                            if (!hotspotNeeded || deviceInfo.getHotspotAvailable() == 1) {
                                betterAndroid = deviceInfo;
                                nbSuccess = score[0];
                                nbFails = score[1];
                            }
                        }
                    } else {
                        if (!hotspotNeeded || deviceInfo.getHotspotAvailable() == 1) {
                            betterAndroid = deviceInfo;
                            nbSuccess = score[0];
                            nbFails = score[1];
                        }
                    }
                } else {
                    if (betterAndroid.getSdkLevel() < deviceInfo.getSdkLevel()) {
                        if (!hotspotNeeded || deviceInfo.getHotspotAvailable() == 1) {
                            betterAndroid = deviceInfo;
                            nbSuccess = score[0];
                            nbFails = score[1];
                        }
                    }
                }
            }
        }
        if (betterAndroid != null) {
            deviceInfosList.remove(betterAndroid);
        }
        return betterAndroid;
    }

    public DeviceInfo popThirdLayerDevice() {
        DeviceInfo thirdLayerDevice = null;
        if (potentialThirdLayerDevices.size() > 0) {
            thirdLayerDevice = potentialThirdLayerDevices.get(0);
            potentialThirdLayerDevices.remove(thirdLayerDevice);
        } else if (potentialDiscoverers.size() > 0) {
            thirdLayerDevice = potentialDiscoverers.get(0);
            potentialDiscoverers.remove(thirdLayerDevice);
        } else if (potentialAdvertisers.size() > 0) {
            thirdLayerDevice = potentialAdvertisers.get(0);
            potentialAdvertisers.remove(thirdLayerDevice);
        }
        return thirdLayerDevice;
    }

    public SubNet activateAndGetNextSubnet(DeviceInfo advertiser, DeviceInfo discoverer) {
        SubNet subNet = new SubNet();
        subNet.setName(hotspotName + "_" + nextSubNet);
        subNet.setPassword(hotspotPassword);
        subNet.setAdvertiser(advertiser);
        subNet.setDiscoverer(discoverer);
        subNets.add(subNet);
        nextSubNet++;
        return subNet;
    }

    public void subnetSuccess(String deviceIdentifier) {
        for ( SubNet subNet : subNets) {
            if (subNet.getDiscoverer().getUniqueId().contentEquals(deviceIdentifier)) {
                subnetResult(subNet, 1);
            }
        }
    }

    static public void subnetResult(SubNet subNet, Integer success) {
        if (success != 0) {
            subNet.setOnline(true);
        }

        SubnetResult advertiserResult = new SubnetResult();
        advertiserResult.setSuccess(success);
        advertiserResult.setSdkLevel(subNet.getAdvertiser().getSdkLevel());
        advertiserResult.setDeviceRole(SubNetConst.ADVERTISER);
        advertiserResult.setDeviceModel(subNet.getAdvertiser().getDeviceModel());
        advertiserResult.setDeviceId(subNet.getAdvertiser().getUniqueId());
        DbTableSubnetResult.insertSubnetResult(advertiserResult);

        SubnetResult discovererResult = new SubnetResult();
        discovererResult.setSuccess(success);
        discovererResult.setSdkLevel(subNet.getDiscoverer().getSdkLevel());
        discovererResult.setDeviceRole(SubNetConst.DISCOVERER);
        discovererResult.setDeviceModel(subNet.getDiscoverer().getDeviceModel());
        discovererResult.setDeviceId(subNet.getDiscoverer().getUniqueId());
        DbTableSubnetResult.insertSubnetResult(discovererResult);

        if (success == 0) {
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().classifiyNewDevice(subNet.getAdvertiser());
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().classifiyNewDevice(subNet.getDiscoverer());
            NetworkCommunication.networkCommunicationSingleton.getNetworkStateSingleton().getSubNets().remove(subNet);
        }
    }

    static public Integer[] getDeviceScore(String deviceId, Integer deviceRole) {
        Integer[] results = new Integer[2];
        ArrayList<SubnetResult> deviceResults = DbTableSubnetResult.getSubnetResultForId(deviceId, deviceRole);

        Integer nbSuccess = 0;
        Integer nbFails = 0;
        for (SubnetResult subnetResult : deviceResults) {
            if (subnetResult.getSuccess() == 1) {
                nbSuccess++;
            } else {
                nbFails++;
            }
        }
        results[0] = nbSuccess;
        results[1] = nbFails;

        return results;
    }

    public void disconnectDevice(String deviceUID) {
        for (DeviceInfo deviceInfo : potentialAdvertisers) {
            if (deviceInfo.getUniqueId().contentEquals(deviceUID)) {
                potentialAdvertisers.remove(deviceInfo);
                return;
            }
        }
        for (DeviceInfo deviceInfo : potentialDiscoverers) {
            if (deviceInfo.getUniqueId().contentEquals(deviceUID)) {
                potentialDiscoverers.remove(deviceInfo);
                return;
            }
        }
        for (DeviceInfo deviceInfo : potentialThirdLayerDevices) {
            if (deviceInfo.getUniqueId().contentEquals(deviceUID)) {
                potentialThirdLayerDevices.remove(deviceInfo);
                return;
            }
        }
        for (DeviceInfo deviceInfo : onlyFirstLayerDevices) {
            if (deviceInfo.getUniqueId().contentEquals(deviceUID)) {
                onlyFirstLayerDevices.remove(deviceInfo);
                return;
            }
        }
    }

    public void operationFailed(String deviceId) {
        for (SubNet subNet : subNets) {
            if (subNet.getAdvertiser().getUniqueId().contentEquals(deviceId)) {
                potentialDiscoverers.add(subNet.getAdvertiser());
                System.err.println("advertising unexpectedly failed");
                return;
            } else if (subNet.getDiscoverer().getUniqueId().contentEquals(deviceId)) {
                potentialThirdLayerDevices.add(subNet.getDiscoverer());
                System.out.println("discovering or hotspot failed");
                return;
            } else {
                for (DeviceInfo deviceInfo : subNet.getClients()) {
                    if (deviceInfo.getUniqueId().contentEquals(deviceId)) {
                        potentialThirdLayerDevices.add(deviceInfo);
                        subNet.setSaturated(true);
                        System.out.println("Subnet saturated");
                        return;
                    }
                }
            }
        }
        System.err.println("Device not found in operationFailed");
    }

    public Map<String, CopyOnWriteArrayList<String>> getStudentsToSyncedIdsMap() {
        return studentsToSyncedIdsMap;
    }

    public List<String> getQuestionIdsToSend() {
        return questionIdsToSend;
    }

    public Map<String, Integer> getStudentsToReadyMap() {
        return studentsToReadyMap;
    }

    public String getActiveID() {
        return activeID;
    }

    public void setActiveID(String activeID) {
        this.activeID = activeID;
    }

    public Map<String, String> getStudentsToActiveIdMap() {
        return studentsToActiveIdMap;
    }

    public void setStudentsToActiveIdMap(Map<String, String> studentsToActiveIdMap) {
        this.studentsToActiveIdMap = studentsToActiveIdMap;
    }

    public Map<String, DeviceInfo> getStudentsToDeviceInfos() {
        return studentsToDeviceInfos;
    }

    public List<String> getStudentsToConnectionStatus() {
        return studentsToConnectionStatus;
    }

    public String getHotspotName() {
        return hotspotName;
    }

    public List<SubNet> getSubNets() {
        return subNets;
    }

    public int getNextSubNet() {
        return nextSubNet;
    }
}
