package hu.bme.aut.onlab.poker.model

import kotlinx.serialization.Serializable

@Serializable
enum class TurnState(val cardsOnTableCount: Int) {
    PREFLOP(0),
    AFTER_FLOP(2),
    AFTER_TURN(3),
    AFTER_RIVER(4);
}