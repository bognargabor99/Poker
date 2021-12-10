package hu.bme.aut.onlab.poker.model

/**
 * Enum class representing the state of a turn
 * @author Bognar, Gabor Bela
 */
enum class TurnState {
    PREFLOP,
    AFTER_FLOP,
    AFTER_TURN,
    AFTER_RIVER;
}