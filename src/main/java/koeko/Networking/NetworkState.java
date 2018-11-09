package koeko.Networking;

import koeko.Koeko;
import koeko.students_management.Student;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkState {
    private Map<String, CopyOnWriteArrayList<String>> studentsToSyncedIdsMap;
    private List<String> questionIdsToSend;

    //0: not ready; 1: ready
    static public int STUDENT_NOT_SYNCED = 0;
    static public int STUDENT_SYNCED = 1;
    private Map<String, Integer> studentsToReadyMap;

    private String activeID = "";
    private Map<String, String> studentsToActiveIdMap;
    private Map<String, DeviceInfo> studentsToDeviceInfos;
    private List<String> studentsToConnectionStatus; //0: not connected; 1: connected


    public NetworkState() {
        this.studentsToSyncedIdsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.questionIdsToSend = new CopyOnWriteArrayList<>();
        this.studentsToReadyMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.studentsToActiveIdMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.studentsToDeviceInfos = Collections.synchronizedMap(new LinkedHashMap<>());
        this.studentsToConnectionStatus = new CopyOnWriteArrayList<>();
    }

    public void toggleSyncStateForStudent(Student student, Integer state) {
        if (studentsToReadyMap.get(student.getUniqueDeviceID()) == 0 && state == 1) {
            Koeko.studentsVsQuestionsTableControllerSingleton.setStatusQuestionsReceived(student, STUDENT_SYNCED);
            studentsToReadyMap.put(student.getUniqueDeviceID(), state);
        } else if (studentsToReadyMap.get(student.getUniqueDeviceID()) == 1 && state == 0) {
            Koeko.studentsVsQuestionsTableControllerSingleton.setStatusQuestionsReceived(student, STUDENT_NOT_SYNCED);
            studentsToReadyMap.put(student.getUniqueDeviceID(), state);
        }
    }

    public void toggleSyncStateForStudent(Vector<Student> students, Integer state) {
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


    public Map<String, CopyOnWriteArrayList<String>> getStudentsToSyncedIdsMap() {
        return studentsToSyncedIdsMap;
    }

    public void setStudentsToSyncedIdsMap(Map<String, CopyOnWriteArrayList<String>> studentsToSyncedIdsMap) {
        this.studentsToSyncedIdsMap = studentsToSyncedIdsMap;
    }

    public List<String> getQuestionIdsToSend() {
        return questionIdsToSend;
    }

    public void setQuestionIdsToSend(List<String> questionIdsToSend) {
        this.questionIdsToSend = questionIdsToSend;
    }

    public Map<String, Integer> getStudentsToReadyMap() {
        return studentsToReadyMap;
    }

    public void setStudentsToReadyMap(Map<String, Integer> studentsToReadyMap) {
        this.studentsToReadyMap = studentsToReadyMap;
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

    public void setStudentsToDeviceInfos(Map<String, DeviceInfo> studentsToDeviceInfos) {
        this.studentsToDeviceInfos = studentsToDeviceInfos;
    }

    public List<String> getStudentsToConnectionStatus() {
        return studentsToConnectionStatus;
    }

    public void setStudentsToConnectionStatus(List<String> studentsToConnectionStatus) {
        this.studentsToConnectionStatus = studentsToConnectionStatus;
    }
}
