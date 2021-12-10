package hu.bme.aut.thesis.poker.gamemodel

/**
 * Represents rules of the [Table]
 * @property isOpen Is the table open for anyone?
 * @property playerCount Number of [Player]s at the table
 * @property playerStartingStack Starting chipstack of the players
 * @property doubleBlindsAfterTurnCount Number of turns the blinds are doubled after
 * @property bigBlindStartingAmount Starting amount of the big blind
 * @property isRoyal Is the table played with a [RoyalDeck]
 * @author Bognar, Gabor Bela
 */
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
