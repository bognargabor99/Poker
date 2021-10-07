package hu.bme.aut.onlab.poker.gamemodel

data class TableRules(
    val isOpen: Boolean,
    val playerCount: Int,
    val bigBlindStartingAmount: Int,
    val doubleBlindsAfterTurnCount: Int,
    val playerStartingStack: Int,
    val isRoyal: Boolean = false
) {
    init {
        require(playerCount >= 2)
    }
    companion object {
        val defaultRules = TableRules(
            isOpen = true,
            playerCount = 2,
            bigBlindStartingAmount = 80,
            doubleBlindsAfterTurnCount = 6,
            playerStartingStack = 3000
        )
    }
}
