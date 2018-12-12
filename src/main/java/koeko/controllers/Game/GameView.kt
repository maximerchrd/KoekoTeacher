package koeko.controllers.Game

class GameView {
    var gameType = -1
    var endScore = 30
    var theme = 0
    var team = 0

    constructor(gameType: Int = -1, endScore: Int = 30, theme: Int = 0, team: Int = 0) {
        this.gameType = gameType
        this.endScore = endScore
        this.theme = theme
        this.team = team
    }
}

object GameType {
    const val manualSending = 0
    const val orderedAutomaticSending = 1
    const val randomAutomaticSending = 2
    const val qrCodeGame = 3
}