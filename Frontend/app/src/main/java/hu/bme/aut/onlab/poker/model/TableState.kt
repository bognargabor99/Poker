package hu.bme.aut.onlab.poker.model

import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage

data class TableState(
    val id: Int,
    var isStarted: Int,
    var oldState: GameStateMessage,
    var newState: GameStateMessage,
    var lastTurnResults: TurnEndMessage,
    var avatarMapping: MutableMap<String, AvatarMap>
)