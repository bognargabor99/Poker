package hu.bme.aut.onlab.poker.network

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.dto.InGamePlayerDto
import hu.bme.aut.onlab.poker.dto.PlayerToSpectateDto
import hu.bme.aut.onlab.poker.dto.TurnEndMsgPlayerDto
import hu.bme.aut.onlab.poker.gamemodel.Action
import hu.bme.aut.onlab.poker.gamemodel.Card
import hu.bme.aut.onlab.poker.gamemodel.TableRules
import hu.bme.aut.onlab.poker.gamemodel.TurnState

interface Message {
    fun toJsonString(): String = Gson().toJson(this)
}

data class NetworkMessage(
    val messageCode: Int,
    val data: String
) : Message

data class CreateTableMessage(
    val userName: String,
    val rules: TableRules = TableRules.defaultRules
) : Message {
    companion object {
        const val MESSAGE_CODE = 1
    }
}

data class JoinTableMessage(
    val userName: String,
    val tableId: Int?
) : Message {
    companion object {
        const val MESSAGE_CODE = 2
    }
}

data class GetOpenTablesMessage(
    val userName: String
) : Message {
    companion object {
        const val MESSAGE_CODE = 3
    }
}

data class ActionIncomingMessage(
    val tableId: Int,
    val name: String,
    val action: Action
) : Message {
    companion object {
        const val MESSAGE_CODE = 4
    }
}

data class GameStateMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: List<InGamePlayerDto>, // players with name, chip stack, and this rounds betsize
    val maxRaiseThisRound: Int,
    val receiverCards: MutableList<Card>, // cards in hand of the receiver
    val nextPlayer: String, // username of next player
    val turnState: TurnState,
    val bigBlind: Int,
    val pot: Int,
    val lastAction: ActionIncomingMessage?
) : Message {
    companion object {
        const val MESSAGE_CODE = 5
    }
}

data class TurnEndMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val playerOrder: List<TurnEndMsgPlayerDto>, // players' hands + winnings in the last turn
) : Message {
    companion object {
        const val MESSAGE_CODE = 6
    }
}

data class EliminationMessage(
    val tableId: Int // id of table the client is eliminated from
) : Message {
    companion object {
        const val MESSAGE_CODE = 7
    }
}

data class ConnectionInfoMessage(
    val userName: String
) : Message {
    companion object {
        const val MESSAGE_CODE = 8
    }
}

data class DisconnectedPlayerMessage(
    val tableId: Int,
    val userName: String
) : Message {
    companion object {
        const val MESSAGE_CODE = 9
    }
}

data class WinnerAnnouncerMessage(
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 10
    }
}

data class SendOpenTablesMessage(
    val tableIds: List<Int>
) : Message {
    companion object {
        const val MESSAGE_CODE = 11
    }
}

data class TableCreatedMessage(
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 12
    }
}

data class TableJoinedMessage(
    val tableId: Int,
    val rules: TableRules = TableRules.defaultRules
) : Message {
    companion object {
        const val MESSAGE_CODE = 13
    }
}

data class GameStartedMessage(
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 14
    }
}

data class LeaveTableMessage(
    val userName: String,
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 15
    }
}

data class SpectatorSubscriptionMessage(
    val userName: String,
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 16
    }
}

data class SpectatorUnsubscriptionMessage(
    val userName: String,
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 17
    }
}

data class SubscriptionAcceptanceMessage(
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 18
    }
}

class UnsubscriptionAcceptanceMessage(
    val tableId: Int
) : Message {
    companion object {
        const val MESSAGE_CODE = 19
    }
}

data class SpectatorGameStateMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: List<PlayerToSpectateDto>, // players with name, chip stack, and this rounds betsize
    val maxRaiseThisRound: Int,
    val nextPlayer: String, // username of next player
    val turnState: TurnState,
    val bigBlind: Int,
    val pot: Int,
    val lastAction: ActionIncomingMessage?
) : Message {
    companion object {
        const val MESSAGE_CODE = 20
    }
}