package hu.bme.aut.onlab.poker.gamemodel

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
}