package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.dto.PlayerDto
import hu.bme.aut.onlab.poker.dto.TurnEndMsgPlayerDto
import hu.bme.aut.onlab.poker.gamemodel.Action
import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.TableRules
import hu.bme.aut.onlab.poker.gamemodel.TurnState
import kotlinx.serialization.Serializable

@Serializable
data class NetworkMessage(
    val messageCode: Int,
    val data: String
)

@Serializable
data class CreateTableMessage(
    val userName: String,
    val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 1
    }
}

@Serializable
data class JoinTableMessage(
    val userName: String,
    val tableId: Int?
) {
    companion object {
        const val MESSAGE_CODE = 2
    }
}

@Serializable
data class GetOpenTablesMessage(
    val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 3
    }
}

@Serializable
data class ActionIncomingMessage(
    val tableId: Int,
    val name: String,
    val action: Action
) {
    companion object {
        const val MESSAGE_CODE = 4
    }
}

@Serializable
data class GameStateMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: List<PlayerDto>, // players with name, chip stack, and this rounds betsize
    val maxRaiseThisRound: Int,
    val receiverCards: MutableList<Card>, // cards in hand of the receiver
    val nextPlayer: String, // username of next player
    val turnState: TurnState,
    val bigBlind: Int,
    val pot: Int,
    val lastAction: ActionIncomingMessage?
) {
    companion object {
        const val MESSAGE_CODE = 5
    }
}

@Serializable
data class TurnEndMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val playerOrder: List<TurnEndMsgPlayerDto>, // players' hands + winnings in the last turn
) {
    companion object {
        const val MESSAGE_CODE = 6
    }
}

@Serializable
data class EliminationMessage(
    val tableId: Int // id of table the client is eliminated from
) {
    companion object {
        const val MESSAGE_CODE = 7
    }
}

@Serializable
data class ConnectionInfoMessage(
    val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 8
    }
}

@Serializable
data class DisconnectedPlayerMessage(
    val tableId: Int,
    val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 9
    }
}

@Serializable
data class WinnerAnnouncerMessage(
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 10
    }
}

@Serializable
data class SendOpenTablesMessage(
    val tableIds: List<Int>
) {
    companion object {
        const val MESSAGE_CODE = 11
    }
}

@Serializable
data class TableCreatedMessage(
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 12
    }
}

@Serializable
data class TableJoinedMessage(
    val tableId: Int,
    val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 13
    }
}

@Serializable
data class GameStartedMessage(
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 14
    }
}

@Serializable
data class LeaveTableMessage(
    val userName: String,
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 15
    }
}