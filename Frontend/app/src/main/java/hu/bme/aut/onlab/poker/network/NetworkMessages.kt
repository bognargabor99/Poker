package hu.bme.aut.onlab.poker.network

import android.os.Parcelable
import hu.bme.aut.onlab.poker.model.*
import kotlinx.parcelize.Parcelize

data class NetworkMessage(
        val messageCode: Int,
        val data: String
)

data class CreateTableMessage(
        val userName: String,
        val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 1
    }
}

data class JoinTableMessage(
        val userName: String,
        val tableId: Int?
) {
    companion object {
        const val MESSAGE_CODE = 2
    }
}

data class GetOpenTablesMessage(
        val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 3
    }
}

data class ActionMessage(
        val tableId: Int,
        val name: String,
        val action: Action
) {
    companion object {
        const val MESSAGE_CODE = 4
    }
}

data class GameStateMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: MutableList<Player>, // players with name, chip stack, and this round's size of bet
    val maxRaiseThisRound: Int,
    val receiverCards: MutableList<Card>, // cards in hand of the receiver
    val nextPlayer: String, // username of next player
    val turnState: TurnState,
    val bigBlind: Int,
    val pot: Int,
    val lastAction: ActionMessage?
) {
    companion object {
        const val MESSAGE_CODE = 5
    }
}

@Parcelize
data class TurnEndMessage(
        val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
        val tableCards: List<Card>, // cards on the table
        val playerOrder: List<WinningPlayer>, // players' hands + winnings in the last turn
) : Parcelable {
    companion object {
        const val MESSAGE_CODE = 6
    }
}

data class EliminationMessage(
        val tableId: Int // id of table the client is eliminated from
) {
    companion object {
        const val MESSAGE_CODE = 7
    }
}

data class ConnectionInfoMessage(
        val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 8
    }
}

data class DisconnectedPlayerMessage(
        val tableId: Int,
        val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 9
    }
}

data class WinnerAnnouncerMessage(
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 10
    }
}

data class SendOpenTablesMessage(
        val tableIds: List<Int>
) {
    companion object {
        const val MESSAGE_CODE = 11
    }
}

data class TableCreatedMessage(
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 12
    }
}

data class TableJoinedMessage(
        val tableId: Int,
        val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 13
    }
}

data class GameStartedMessage(
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 14
    }
}

data class LeaveTableMessage(
        val userName: String,
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 15
    }
}