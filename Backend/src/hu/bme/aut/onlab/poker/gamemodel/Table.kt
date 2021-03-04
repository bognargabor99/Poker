package hu.bme.aut.onlab.poker.gamemodel

class Table(private val settings: TableSettings) {

    private val players: MutableList<Player> = mutableListOf()
    var isStarted: Boolean = false
    var bigBlindAmount = settings.bigBlindStartingAmount
    var turnCount = 0

    fun addPlayer(player: Player) : Boolean = if (!isStarted && players.size < settings.playerCount) {
        players.add(player)
        true
    } else
        false

    fun startGame() {
        isStarted = true
    }

    fun gameLoop() {

    }

    fun newTurn() {
        TODO("Start new turn")
    }

    fun endTurn() {
        TODO("End turn")
    }

    fun evaluateHand(fromCards: List<Card>) : Hand {
        TODO("Hand evaluation algorithm needed")
    }
}