package hu.bme.aut.onlab.poker.network

import android.os.Parcelable
import hu.bme.aut.onlab.poker.dto.PlayerToSpectate
import hu.bme.aut.onlab.poker.model.*
import kotlinx.parcelize.Parcelize

/**
 * This class is used to communicate with client on the network
 * @author Bognar, Gabor Bela
 */
data class NetworkMessage(
        val messageCode: Int,
        val data: String
)

/**
 * A message class for asking for the creation of a table
 * @author Bognar, Gabor Bela
 */
data class CreateTableMessage(
        val userName: String,
        val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 1
    }
}

/**
 * A message class for asking to join a table
 * @author Bognar, Gabor Bela
 */
data class JoinTableMessage(
        val userName: String,
        val tableId: Int?
) {
    companion object {
        const val MESSAGE_CODE = 2
    }
}

/**
 * A message class for fetching publicly available table
 * @author Bognar, Gabor Bela
 */
data class GetOpenTablesMessage(
        val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 3
    }
}

/**
 * A message class for interacting in the Game
 * @author Bognar, Gabor Bela
 */
data class ActionMessage(
        val tableId: Int,
        val name: String,
        val action: Action
) {
    companion object {
        const val MESSAGE_CODE = 4
    }
}

/**
 * A message class for sending out state of a Game
 * @param tableId The ID of the table
 * @param tableCards Cards of the table
 * @param bigBlind The amount of the big blind
 * @param lastAction The last valid action that happened on the table
 * @param maxRaiseThisRound Maximum amount a chips that [Player] has put in the pot
 * @param nextPlayer Name of the next [Player]
 * @param players Informations about the [Player]s at the Table
 * @param pot The amount of the chips in the pot
 * @param turnState Current state of the table
 * @param receiverCards Cards of the [Player] that the this message is sent to
 * @author Bognar, Gabor Bela
 */
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

/**
 * A message class for informing players and spectators about the end of a turn
 * @param tableCards Cards on the table
 * @param playerOrder Order of the [Player]s that were in the showdown
 * @author Bognar, Gabor Bela
 */
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

/**
 * A message class for informing a [Player] that he/she has been eliminated
 * @author Bognar, Gabor Bela
 */
data class EliminationMessage(
        val tableId: Int // id of table the client is eliminated from
) {
    companion object {
        const val MESSAGE_CODE = 7
    }
}

/**
 * A message class for sending out the name of a [User]
 * @author Bognar, Gabor Bela
 */
data class ConnectionInfoMessage(
        val userName: String,
        val isGuest: Boolean
) {
    companion object {
        const val MESSAGE_CODE = 8
    }
}

/**
 * A message class for informing [Player] that another player has been disconnected or eliminated from a table
 * @author Bognar, Gabor Bela
 */
data class DisconnectedPlayerMessage(
        val tableId: Int,
        val userName: String
) {
    companion object {
        const val MESSAGE_CODE = 9
    }
}

/**
 * A message class for announcing the winner of a game
 * @author Bognar, Gabor Bela
 */
data class WinnerAnnouncerMessage(
    val tableId: Int,
    val nameOfWinner: String
) {
    companion object {
        const val MESSAGE_CODE = 10
    }
}

/**
 * A message class for answering a [GetOpenTablesMessage]
 * @author Bognar, Gabor Bela
 */
data class SendOpenTablesMessage(
        val tableIds: List<Int>
) {
    companion object {
        const val MESSAGE_CODE = 11
    }
}

/**
 * A message class for informing a user that a Table has been created
 * @author Bognar, Gabor Bela
 */
data class TableCreatedMessage(
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 12
    }
}

/**
 * A message class for informing a user that he/she joined a table
 * @author Bognar, Gabor Bela
 */
data class TableJoinedMessage(
        val tableId: Int,
        val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 13
    }
}

/**
 * A message class for informing a user that a table started playing
 * @author Bognar, Gabor Bela
 */
data class GameStartedMessage(
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 14
    }
}

/**
 * A message class for informing that a user will no longer play at a table
 * @author Bognar, Gabor Bela
 */
data class LeaveTableMessage(
        val userName: String,
        val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 15
    }
}

/**
 * A message class for subscribing to a table as a spectator
 * @author Bognar, Gabor Bela
 */
data class SpectatorSubscriptionMessage(
    val userName: String,
    val tableId: Int?
) {
    companion object {
        const val MESSAGE_CODE = 16
    }
}

/**
 * A message class for unsubscribing from a table as a spectator
 * @author Bognar, Gabor Bela
 */
data class SpectatorUnsubscriptionMessage(
    val userName: String,
    val tableId: Int
) {
    companion object {
        const val MESSAGE_CODE = 17
    }
}

/**
 * A message class for informing a user that he/she has succesfully started spectating a table
 * @author Bognar, Gabor Bela
 */
data class SubscriptionAcceptanceMessage(
    val tableId: Int,
    val rules: TableRules
) {
    companion object {
        const val MESSAGE_CODE = 18
    }
}

/**
 * Specific GameStateMessage for spectators. Contains the cards of each player
 * @author Bognar, Gabor Bela
 */
data class SpectatorGameStateMessage(
    val tableId: Int, // id of Table (if multiple playable Tables will be implemented in the future)
    val tableCards: List<Card>, // cards on the table
    val players: List<PlayerToSpectate>, // players with name, chip stack, and this rounds betsize
    val maxRaiseThisRound: Int,
    val nextPlayer: String, // username of next player
    val turnState: TurnState,
    val bigBlind: Int,
    val pot: Int,
    val lastAction: ActionMessage?
) {
    companion object {
        const val MESSAGE_CODE = 19
    }
}

/**
 * A message class for asking for and sending out [Statistics] of a user
 * @author Bognar, Gabor Bela
 */
data class StatisticsMessage(
    val userName: String,
    var statistics: Statistics? = null
) {
    companion object {
        const val MESSAGE_CODE = 20
    }
}