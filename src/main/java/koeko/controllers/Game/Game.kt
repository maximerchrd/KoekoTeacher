package koeko.controllers.Game

class Game {
    var endScore = 0
    var teamOne = Team()
    var teamTwo = Team()
    var scoreTeamOne = 0
    var scoreTeamTwo = 0
    var gameType = 0

    constructor(endScore:Int = 30) {
        this.endScore = endScore
    }
}