package koeko.controllers.Game

import koeko.students_management.Student

class Team {
    var studentCellView: ArrayList<StudentCellView> = ArrayList()
    var teamScore = 0.0

    fun increaseScore (student: StudentCellView?, scoreIncrease: Double) {
        var index = studentCellView.indexOf(student)
        if (index >= 0) {
            studentCellView.get(index).score += scoreIncrease
        }
        teamScore += scoreIncrease
    }
}