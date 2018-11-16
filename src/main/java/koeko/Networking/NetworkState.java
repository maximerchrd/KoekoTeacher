package koeko.Networking;

import koeko.Koeko;
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
    private String hotspotName = "koeko";
    private int nextSubNet = 1;
    private ArrayList<SubNet> subNets;

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
        this.subNets = new ArrayList<>();
        this.potentialAdvertisers = new ArrayList<>();
        this.potentialDiscoverers = new ArrayList<>();
        this.potentialThirdLayerDevices = new ArrayList<>();
        this.onlyFirstLayerDevices = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            SubNet subNet = new SubNet();
            subNet.setName(hotspotName + (i+1));
            subNet.setPassword(String.valueOf(System.nanoTime() + i));
            subNets.add(subNet);
        }
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
        DeviceInfo mostRecentAndroid = null;
        for (DeviceInfo deviceInfo : potentialAdvertisers) {
            if (mostRecentAndroid == null || mostRecentAndroid.getSdkLevel() < deviceInfo.getSdkLevel()) {
                mostRecentAndroid = deviceInfo;
            }
        }
        if (mostRecentAndroid != null) {
            potentialAdvertisers.remove(mostRecentAndroid);
        }
        return mostRecentAndroid;
    }

    public DeviceInfo popDiscoverer() {
        DeviceInfo discoverer = null;
        if (potentialAdvertisers.size() > 5) {
            discoverer = potentialAdvertisers.get(0);
            potentialAdvertisers.remove(discoverer);
        } else if (potentialDiscoverers.size() > 0) {
            discoverer = potentialDiscoverers.get(0);
            potentialDiscoverers.remove(discoverer);
        } else if (potentialAdvertisers.size() > 0) {
            discoverer = potentialAdvertisers.get(0);
            potentialAdvertisers.remove(discoverer);
        } else {
            System.out.println("PROBLEM: Wifi saturated without possibility of starting new nearby node (less than 2 suitable androids)");
        }
        return discoverer;
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
        subNets.get(nextSubNet).setAdvertiser(advertiser);
        subNets.get(nextSubNet).setDiscoverer(discoverer);
        subNets.get(nextSubNet).setOnline(true);
        nextSubNet++;
        return subNets.get(nextSubNet - 1);
    }

    public Integer getNumberOfFirstLayerDevices() {
        Integer devices = 0;
        devices += potentialAdvertisers.size();
        devices += potentialDiscoverers.size();
        devices += potentialThirdLayerDevices.size();
        devices += onlyFirstLayerDevices.size();
        for (SubNet subNet : subNets) {
            if (subNet.getOnline()) devices++;
        }
        return  devices;
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

    public ArrayList<SubNet> getSubNets() {
        return subNets;
    }

    public int getNextSubNet() {
        return nextSubNet;
    }
}
