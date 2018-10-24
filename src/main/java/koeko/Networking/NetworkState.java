package koeko.Networking;

import koeko.Koeko;
import koeko.students_management.Student;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkState {
    private Map<String, CopyOnWriteArrayList<String>> studentsToIdsMap;
    private List<String> sentQuestionIds;

    //0: not ready; 1: ready
    static public int STUDENT_NOT_SYNCED = 0;
    static public int STUDENT_SYNCED = 1;
    private Map<String, Integer> studentsToReadyMap;

    public NetworkState() {
        this.studentsToIdsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.sentQuestionIds = new CopyOnWriteArrayList<>();
        this.studentsToReadyMap = Collections.synchronizedMap(new LinkedHashMap<>());
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


    public Map<String, CopyOnWriteArrayList<String>> getStudentsToIdsMap() {
        return studentsToIdsMap;
    }

    public void setStudentsToIdsMap(Map<String, CopyOnWriteArrayList<String>> studentsToIdsMap) {
        this.studentsToIdsMap = studentsToIdsMap;
    }

    public List<String> getSentQuestionIds() {
        return sentQuestionIds;
    }

    public void setSentQuestionIds(List<String> sentQuestionIds) {
        this.sentQuestionIds = sentQuestionIds;
    }

    public Map<String, Integer> getStudentsToReadyMap() {
        return studentsToReadyMap;
    }

    public void setStudentsToReadyMap(Map<String, Integer> studentsToReadyMap) {
        this.studentsToReadyMap = studentsToReadyMap;
    }
}
