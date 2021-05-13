package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
enum class TurnState {
    PREFLOP,
    AFTER_FLOP,
    AFTER_TURN,
    AFTER_RIVER;
}