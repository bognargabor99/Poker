package hu.bme.aut.thesis.poker.gamemodel

/**
 * Enum class representing the state of a turn
 * @author Bognar, Gabor Bela
 */
enum class TurnState {
    PREFLOP,
    AFTER_FLOP,
    AFTER_TURN,
    AFTER_RIVER;

    /**
     * Increases the state of the turn (e.g. FLOP -> TURN, RIVER -> PREFLOP)
     * @author Bognar, Gabor Bela
     */
    operator fun inc(): TurnState =
        when (this) {
            PREFLOP -> AFTER_FLOP
            AFTER_FLOP -> AFTER_TURN
            AFTER_TURN -> AFTER_RIVER
            else -> PREFLOP
        }

    companion object {
        /**
         * Converts number of [Card]s on the [Table] to [TurnState].
         * (e.g. 3 -> FLOP, 4 -> TURN)
         * @param cardCount Number of cards on the table
         * @exception IllegalArgumentException If [cardCount] is not one of (0,3,4,5)
         * @author Bognar, Gabor Bela
         */
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