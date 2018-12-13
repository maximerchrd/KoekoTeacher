package koeko.controllers.Game

class Game {
    var endScore = 0
    var teamOne = Team()
    var teamTwo = Team()
    var gameType = 0

    constructor(endScore:Int = 30) {
        this.endScore = endScore
    }
}