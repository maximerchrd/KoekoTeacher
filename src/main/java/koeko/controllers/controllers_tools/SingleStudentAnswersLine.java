package koeko.controllers.controllers_tools;

import javafx.beans.property.SimpleStringProperty;
import koeko.students_management.Student;

import java.util.ArrayList;

/**
 * Created by maximerichard on 12.03.18.
 */
public class SingleStudentAnswersLine {
    private final Student Student = new Student();
    private final SimpleStringProperty Status = new SimpleStringProperty("");
    private final SimpleStringProperty Evaluation = new SimpleStringProperty("");
    private final ArrayList<SimpleStringProperty> Answers;



    public SingleStudentAnswersLine(Student student, String status, String evaluation) {
        setStudent(student);
        setStatus(status);
        setEvaluation(evaluation);
        Answers = new ArrayList<>();
    }

    public String getStudentName() {
        return Student.getName();
    }

    public Student getStudentObject() {
        return Student;
    }

    public SimpleStringProperty studentProperty() {
        SimpleStringProperty simpleStringProperty = new SimpleStringProperty(Student.getName());
        return simpleStringProperty;
    }

    public void setStudent(Student student) {
        this.Student.setName(student.getName());
        this.Student.setStudentID(student.getStudentID());
    }

    public String getStatus() {
        return Status.get();
    }

    public SimpleStringProperty statusProperty() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status.set(status);
    }

    public String getEvaluation() {
        return Evaluation.get();
    }

    public SimpleStringProperty evaluationProperty() {
        return Evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.Evaluation.set(evaluation);
    }

    public ArrayList<SimpleStringProperty> getAnswers() {
        return Answers;
    }

    public void addAnswer() {
        SimpleStringProperty answer = new SimpleStringProperty("");
        Answers.add(answer);
    }
    public int setAnswer(String answer, int index) {
        if (Answers.size() > index) {
            Answers.get(index).set(answer);
            return 0;
        } else {
            return -1;
        }
    }
}
