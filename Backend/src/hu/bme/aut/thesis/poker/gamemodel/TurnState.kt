package hu.bme.aut.thesis.poker.gamemodel

enum class TurnState {
    PREFLOP,
    AFTER_FLOP,
    AFTER_TURN,
    AFTER_RIVER;

    operator fun inc(): TurnState =
        when (this) {
            PREFLOP -> AFTER_FLOP
            AFTER_FLOP -> AFTER_TURN
            AFTER_TURN -> AFTER_RIVER
            else -> PREFLOP
        }

    companion object {
        fun fromCardCount(cardCount: Int): TurnState {
            return when (cardCount) {
                0 -> PREFLOP
                3 -> AFTER_FLOP
                4 -> AFTER_TURN
                5 -> AFTER_RIVER
                else -> throw IllegalArgumentException("The count of table cards must be either: 0, 3, 4, 5")
            }
        }
    }
}