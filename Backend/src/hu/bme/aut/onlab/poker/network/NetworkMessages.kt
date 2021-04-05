package hu.bme.aut.onlab.poker.network

import hu.bme.aut.onlab.poker.dto.PlayerDto
import hu.bme.aut.onlab.poker.dto.TurnEndMsgPlayerDto
import hu.bme.aut.onlab.poker.gamemodel.Action
import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.TableRules
import kotlinx.serialization.Serializable

@Serializable
data class NetworkMessage(
    val messageCode: Int,
    val data: String
)

@Serializable
data class StartTableMessage( // 1
    val userName: String,
    val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 1
    }
}

@Serializable
data class JoinTableMessage( // 2
    val userName: String,
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 2
    }
}

@Serializable
data class GetOpenTablesMessage( // 3
    val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 3
    }
}

@Serializable
data class ActionIncomingMessage( // 4
    val tableId: Int,
    val playerId: Int,
    val action: Action
) {
    companion object {
        const val MESSAGE_CODE = 4
    }
}

@Serializable
data class GameStateMessage( // 5
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: List<PlayerDto>, // players with name, chip stack, and this rounds betsize
    var receiverPID: Int, // receiver's playerId in the Game
    val receiverCards: MutableList<Card>, // cards in hand of the receiver
    val nextPlayer: String, // username of next player
    val lastAction: ActionIncomingMessage?
) {
    companion object {
        const val MESSAGE_CODE = 5
    }
}

@Serializable
data class TurnEndMessage( // 6
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val playerOrder: List<TurnEndMsgPlayerDto>, // players' hands + winnings in the last turn
) {
    companion object {
        const val MESSAGE_CODE = 6
    }
}

@Serializable
data class EliminationMessage( // 7
    val tableId: Int // id of table the client id eliminated from
) {
    companion object {
        const val MESSAGE_CODE = 7
    }
}

@Serializable
data class ConnectionInfoMessage( // 8
    val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 8
    }
}

@Serializable
data class DisconnectedPlayerMessage( // 9
    val tableId: Int,
    val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 9
    }
}

@Serializable
data class WinnerAnnouncerMessage( // 10
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 10
    }
}

@Serializable
data class SendOpenTablesMessage( // 11
    val tableIds: List<Int>
) {
    companion object {
        const val MESSAGE_CODE = 11
    }
}