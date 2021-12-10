package hu.bme.aut.onlab.poker.network

import android.util.Log
import com.google.gson.Gson
import hu.bme.aut.onlab.poker.MainFragment
import hu.bme.aut.onlab.poker.model.Action
import hu.bme.aut.onlab.poker.model.Statistics
import hu.bme.aut.onlab.poker.model.TableRules
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Represents the client that communicates with the server
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
object PokerClient {
    lateinit var session: DefaultClientWebSocketSession
    lateinit var userName: String
    var isGuest: Boolean = true
        set(value) {
            field = value
            MainFragment._this.interactionEnabled = true
        }
    private var tables = mutableListOf<Int>()
    private val chain = NetworkChain()
    lateinit var tableJoinedListener: TableJoinedListener
    var statsListener: StatisticsListener? = null
    var subListener: SubscriptionAcceptanceListener? = null
    var receiver: GamePlayReceiver? = null
    var spectatingReceiver: SpectatorGamePlayReceiver? = null

    /**
     * Receives text from the server
     * @author Bognar, Gabor Bela
     */
    fun receiveText(text: String) {
        val message: NetworkMessage
        try {
            message = Gson().fromJson(text, NetworkMessage::class.java)
        } catch (e: Exception) {
            Log.d("pokerWeb", "Couldn't decode text from server:\n$text")
            return
        }
        chain.process(message)
    }

    /**
     * Send text to the server
     * @author Bognar, Gabor Bela
     */
    private fun sendToServer(data: String, code: Int) {
        val message = NetworkMessage(code, data)
        GlobalScope.launch {
            session.send(Frame.Text(Gson().toJson(message)))
        }
    }

    /**
     * Receives state message from the server.
     * @author Bognar, Gabor Bela
     */
    fun setGameState(networkMessage: NetworkMessage) {
        if (networkMessage.messageCode == GameStateMessage.MESSAGE_CODE) {
            val stateMessage = Gson().fromJson(networkMessage.data, GameStateMessage::class.java)
            receiver?.onNewGameState(stateMessage)
        }
        else if (networkMessage.messageCode == SpectatorGameStateMessage.MESSAGE_CODE) {
            val stateMessage = Gson().fromJson(networkMessage.data, SpectatorGameStateMessage::class.java)
            spectatingReceiver?.onNewGameState(stateMessage)
        }
    }

    /**
     * Set our username
     * @author Bognar, Gabor Bela
     */
    fun setConnectionInfo(name: String, guest: Boolean) {
        userName = name
        isGuest = guest
    }

    /**
     * Receives [TurnEndMessage]
     * @author Bognar, Gabor Bela
     */
    fun turnEnded(turnEndMessage: TurnEndMessage) {
        Log.d("pokerWeb", "Turn ended: $turnEndMessage")
        spectatingReceiver?.onTurnEnd(turnEndMessage)
        receiver?.onTurnEnd(turnEndMessage)
    }

    /**
     * Handles being eliminated from the table
     * @author Bognar, Gabor Bela
     */
    fun eliminatedFromTable(table: Int) {
        receiver?.onGetEliminated(table)
    }

    /**
     * Sends [CreateTableMessage] to the server
     * @author Bognar, Gabor Bela
     */
    fun startTable(rules: TableRules) {
        val createMessage = CreateTableMessage(userName, rules)
        sendToServer(Gson().toJson(createMessage), CreateTableMessage.MESSAGE_CODE)
    }

    /**
     * Handles joining a table
     * @author Bognar, Gabor Bela
     */
    fun tableJoined(joinedMessage: TableJoinedMessage) {
        if (joinedMessage.tableId != 0 && !tables.contains(joinedMessage.tableId)) {
            tables.add(joinedMessage.tableId)
            tableJoinedListener.tableJoined(joinedMessage)
        }
    }

    /**
     * Sends table join request to the server
     * @author Bognar, Gabor Bela
     */
    fun tryJoin(tableId: Int?) {
        val joinMessage = JoinTableMessage(userName, tableId)
        sendToServer(Gson().toJson(joinMessage), JoinTableMessage.MESSAGE_CODE)
    }

    /**
     * Sends table leaving request to the server
     * @author Bognar, Gabor Bela
     */
    fun leaveTable(tableId: Int) {
        val leaveMessage = LeaveTableMessage(userName, tableId)
        sendToServer(Gson().toJson(leaveMessage), LeaveTableMessage.MESSAGE_CODE)
    }

    /**
     * Handles starting of a table
     * @author Bognar, Gabor Bela
     */
    fun tableStarted(tableId: Int) {
        receiver?.onTableStarted(tableId)
    }

    /**
     * Handles disconnection of another player
     * @author Bognar, Gabor Bela
     */
    fun playerDisconnectedFromTable(tableId: Int, userName: String) {
        receiver?.onPlayerDisconnection(tableId, userName)
        spectatingReceiver?.onPlayerDisconnection(tableId, userName)
    }

    /**
     * Handles winning a table
     * @author Bognar, Gabor Bela
     */

    fun tableWon(tableId: Int, playerName: String) {
        receiver?.onTableWin(tableId)
        spectatingReceiver?.onTableWin(tableId, playerName)
    }

    /**
     * Sends [ActionMessage] to the server
     * @author Bognar, Gabor Bela
     */
    fun action(action: Action, tableId: Int) {
        val actionMessage = ActionMessage(tableId, userName, action)
        sendToServer(Gson().toJson(actionMessage), ActionMessage.MESSAGE_CODE)
    }

    /**
     * Fetches our stats from the server
     * @author Bognar, Gabor Bela
     */
    fun fetchStatistics() {
        val getStatsMessage = StatisticsMessage(userName)
        sendToServer(Gson().toJson(getStatsMessage), StatisticsMessage.MESSAGE_CODE)
    }

    /**
     * Receives statistics from the server
     * @author Bognar, Gabor Bela
     */
    fun receiveStats(stats: Statistics) {
        statsListener?.statisticsReceived(stats)
    }

    /**
     * Send [SpectatorSubscriptionMessage] to the server
     * @author Bognar, Gabor Bela
     */
    fun trySpectate(tableId: Int?) {
        val subMessage = SpectatorSubscriptionMessage(userName, tableId)
        sendToServer(Gson().toJson(subMessage), SpectatorSubscriptionMessage.MESSAGE_CODE)
    }

    /**
     * Send [SpectatorUnsubscriptionMessage] to the server
     * @author Bognar, Gabor Bela
     */
    fun unsubscribe(tableId: Int) {
        val unSubMessage = SpectatorUnsubscriptionMessage(userName, tableId)
        sendToServer(Gson().toJson(unSubMessage), SpectatorUnsubscriptionMessage.MESSAGE_CODE)
    }

    /**
     * Handles spectating a table
     */
    fun tableSpectated(acceptanceMessage: SubscriptionAcceptanceMessage) {
        Log.d("pokerWeb", "Got subscription acceptance for table: ${acceptanceMessage.tableId}")
        subListener?.tableSpectated(acceptanceMessage)
    }

    interface TableJoinedListener {
        fun tableJoined(joinedMessage: TableJoinedMessage)
    }

    interface SubscriptionAcceptanceListener {
        fun tableSpectated(subMessage: SubscriptionAcceptanceMessage)
    }

    interface StatisticsListener {
        fun statisticsReceived(statistics: Statistics)
    }

    interface GamePlayReceiver {
        fun onTableStarted(tableId: Int)
        fun onNewGameState(stateMessage: GameStateMessage)
        fun onTurnEnd(turnEndMessage: TurnEndMessage)
        fun onGetEliminated(tableId: Int)
        fun onPlayerDisconnection(tableId: Int, name: String)
        fun onTableWin(tableId: Int)
    }

    interface SpectatorGamePlayReceiver {
        fun onNewGameState(stateMessage: SpectatorGameStateMessage)
        fun onTurnEnd(turnEndMessage: TurnEndMessage)
        fun onPlayerDisconnection(tableId: Int, name: String)
        fun onTableWin(tableId: Int, name: String)
    }
}