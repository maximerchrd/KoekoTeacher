package koeko.controllers.Game

import koeko.Koeko
import koeko.Networking.NetworkCommunication
import koeko.database_management.DbTableRelationQuestionQuestion
import koeko.questions_management.QuestionGeneric
import koeko.students_management.Student
import kotlin.random.Random

class Game {
    var endScore = 0
    var teamOne = Team()
    var teamTwo = Team()
    var gameType = 0
    var usedQuestions = ArrayList<QuestionGeneric>()

    constructor(endScore:Int = 30) {
        this.endScore = endScore
    }

    fun getAllStudents (): ArrayList<StudentCellView> {
        var studentCellViews: ArrayList<StudentCellView> = ArrayList()
        studentCellViews.addAll(teamOne.studentCellViews)
        studentCellViews.addAll(teamTwo.studentCellViews)
        return studentCellViews
    }

    fun sendNextQuestion(randomiseQuestionOrder: Boolean, questionsSet: ArrayList<QuestionGeneric>) {
        val rawQuestionSet = ArrayList<QuestionGeneric>()
        rawQuestionSet.addAll(questionsSet)
        questionsSet.removeAll(usedQuestions)
        if (questionsSet.size == 0) {
            usedQuestions.clear()
            questionsSet.addAll(rawQuestionSet)
        }

        var questionIndex = 0
        if (randomiseQuestionOrder) {
            questionIndex = Random.nextInt(0, questionsSet.size - 1)
        }

        var questionIds = DbTableRelationQuestionQuestion.getQuestionsLinkedToTestId(questionsSet.get(questionIndex).globalID)
        if (questionIds.size > 0) {
            for (i in 0..(teamOne.studentCellViews.size - 1)) {
                var studentSoloArray = ArrayList<Student>()
                studentSoloArray.add(teamOne.studentCellViews.get(i).student)
                NetworkCommunication.networkCommunicationSingleton.sendQuestionID(questionIds.get(i % questionIds.size),
                        studentSoloArray)
            }
            for (i in 0..(teamTwo.studentCellViews.size - 1)) {
                var studentSoloArray = ArrayList<Student>()
                studentSoloArray.add(teamOne.studentCellViews.get(i).student)
                NetworkCommunication.networkCommunicationSingleton.sendQuestionID(questionIds.get(i % questionIds.size),
                        studentSoloArray)
            }
        } else {
            println("sendNextQuestion: gameSet is empty")
        }
        usedQuestions.add(questionsSet.get(questionIndex))
    }
}