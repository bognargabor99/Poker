package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.dto.PlayerDto
import hu.bme.aut.onlab.poker.dto.TurnEndMsgPlayerDto
import hu.bme.aut.onlab.poker.gamemodel.Action
import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.TableRules
import kotlinx.serialization.Serializable

@Serializable
data class NetworkRequest(
    val messageCode: Int,
    val data: String
)

@Serializable
data class StartTableMessage( // 1
    val userName: String,
    val rules: TableRules
)

@Serializable
data class JoinTableMessage( // 2
    val userName: String,
    val tableId: Int
)

@Serializable
data class GetOpenTablesMessage( // 3
    val userName: String
)

@Serializable
data class ActionIncomingMessage( // 4
    val tableId: Int,
    val playerId: Int,
    val action: Action
)

@Serializable
data class GameStateMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: List<PlayerDto>, // players with name, chip stack, and this rounds betsize
    val receiverCards: MutableList<Card>, // cards in hand of the receiver
    val nextPlayer: String, // username of next player
    val lastAction: ActionIncomingMessage?
)

@Serializable
data class TurnEndMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val playerOrder: List<TurnEndMsgPlayerDto>, // players' hands + winnings in the last turn
)