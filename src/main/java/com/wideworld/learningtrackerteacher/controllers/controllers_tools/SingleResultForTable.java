package com.wideworld.learningtrackerteacher.controllers.controllers_tools;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by maximerichard on 08.03.18.
 */
public class SingleResultForTable {
    private final SimpleStringProperty name = new SimpleStringProperty("");
    private final SimpleStringProperty date = new SimpleStringProperty("");
    private final SimpleStringProperty question = new SimpleStringProperty("");
    private final SimpleStringProperty evaluation = new SimpleStringProperty("");
    private final SimpleStringProperty studentsAnswer = new SimpleStringProperty("");
    private final SimpleStringProperty correctAnswer = new SimpleStringProperty("");
    private final SimpleStringProperty incorrectAnswer = new SimpleStringProperty("");
    private final SimpleStringProperty subjects = new SimpleStringProperty("");
    private final SimpleStringProperty objectives = new SimpleStringProperty("");

    public SingleResultForTable() {

    }
    public SingleResultForTable(String name, String date, String question, String evaluation, String studentsAnswer,
                                String correctAnswer, String incorrectAnswer, String subjects, String objectives) {
        setName(name);
        setDate(date);
        setQuestion(question);
        setEvaluation(evaluation);
        setStudentsAnswer(studentsAnswer);
        setCorrectAnswer(correctAnswer);
        setIncorrectAnswer(incorrectAnswer);
        setSubjects(subjects);
        setObjectives(objectives);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDate() {
        return date.get();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getQuestion() {
        return question.get();
    }

    public SimpleStringProperty questionProperty() {
        return question;
    }

    public void setQuestion(String question) {
        this.question.set(question);
    }

    public String getEvaluation() {
        return evaluation.get();
    }

    public SimpleStringProperty evaluationProperty() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation.set(evaluation);
    }

    public String getStudentsAnswer() {
        return studentsAnswer.get();
    }

    public SimpleStringProperty studentsAnswerProperty() {
        return studentsAnswer;
    }

    public void setStudentsAnswer(String studentsAnswer) {
        this.studentsAnswer.set(studentsAnswer);
    }

    public String getCorrectAnswer() {
        return correctAnswer.get();
    }

    public SimpleStringProperty correctAnswerProperty() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer.set(correctAnswer);
    }

    public String getIncorrectAnswer() {
        return incorrectAnswer.get();
    }

    public SimpleStringProperty incorrectAnswerProperty() {
        return incorrectAnswer;
    }

    public void setIncorrectAnswer(String incorrectAnswer) {
        this.incorrectAnswer.set(incorrectAnswer);
    }

    public String getSubjects() {
        return subjects.get();
    }

    public SimpleStringProperty subjectsProperty() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects.set(subjects);
    }

    public String getObjectives() {
        return objectives.get();
    }

    public SimpleStringProperty objectivesProperty() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives.set(objectives);
    }
}
