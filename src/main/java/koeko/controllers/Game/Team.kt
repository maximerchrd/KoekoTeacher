package koeko.controllers.Game

class Team {
    var studentCellViews: ArrayList<StudentCellView> = ArrayList()
    var teamScore = 0.0

    fun increaseScore (student: StudentCellView?, scoreIncrease: Double) {
        var index = studentCellViews.indexOf(student)
        if (index >= 0) {
            studentCellViews.get(index).score += scoreIncrease
        }
        teamScore += scoreIncrease
    }
}