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

    private fun sendToServer(data: String, code: Int) {
        val message = NetworkMessage(code, data)
        GlobalScope.launch {
            session.send(Frame.Text(Gson().toJson(message)))
        }
    }

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

    fun setConnectionInfo(name: String, guest: Boolean) {
        userName = name
        isGuest = guest
    }

    fun turnEnded(turnEndMessage: TurnEndMessage) {
        Log.d("pokerWeb", "Turn ended: $turnEndMessage")
        spectatingReceiver?.onTurnEnd(turnEndMessage)
        receiver?.onTurnEnd(turnEndMessage)
        //Thread.sleep(600)
    }

    fun eliminatedFromTable(table: Int) {
        receiver?.onGetEliminated(table)
    }

    fun startTable(rules: TableRules) {
        val createMessage = CreateTableMessage(userName, rules)
        sendToServer(Gson().toJson(createMessage), CreateTableMessage.MESSAGE_CODE)
    }

    fun tableJoined(joinedMessage: TableJoinedMessage) {
        if (joinedMessage.tableId != 0 && !tables.contains(joinedMessage.tableId)) {
            tables.add(joinedMessage.tableId)
            tableJoinedListener.tableJoined(joinedMessage)
        }
    }

    fun tryJoin(tableId: Int?) {
        val joinMessage = JoinTableMessage(userName, tableId)
        sendToServer(Gson().toJson(joinMessage), JoinTableMessage.MESSAGE_CODE)
    }

    fun leaveTable(tableId: Int) {
        val leaveMessage = LeaveTableMessage(userName, tableId)
        sendToServer(Gson().toJson(leaveMessage), LeaveTableMessage.MESSAGE_CODE)
    }

    fun tableStarted(tableId: Int) {
        receiver?.onTableStarted(tableId)
    }

    fun playerDisconnectedFromTable(tableId: Int, userName: String) {
        receiver?.onPlayerDisconnection(tableId, userName)
        spectatingReceiver?.onPlayerDisconnection(tableId, userName)
    }

    fun tableWon(tableId: Int, playerName: String) {
        receiver?.onTableWin(tableId)
        spectatingReceiver?.onTableWin(tableId, playerName)
    }

    fun action(action: Action, tableId: Int) {
        val actionMessage = ActionMessage(tableId, userName, action)
        sendToServer(Gson().toJson(actionMessage), ActionMessage.MESSAGE_CODE)
    }

    fun fetchStatistics() {
        val getStatsMessage = StatisticsMessage(userName)
        sendToServer(Gson().toJson(getStatsMessage), StatisticsMessage.MESSAGE_CODE)
    }

    fun receiveStats(stats: Statistics) {
        statsListener?.statisticsReceived(stats)
    }

    fun trySpectate(tableId: Int?) {
        val subMessage = SpectatorSubscriptionMessage(userName, tableId)
        sendToServer(Gson().toJson(subMessage), SpectatorSubscriptionMessage.MESSAGE_CODE)
    }

    fun unsubscribe(tableId: Int) {
        val unSubMessage = SpectatorUnsubscriptionMessage(userName, tableId)
        sendToServer(Gson().toJson(unSubMessage), SpectatorUnsubscriptionMessage.MESSAGE_CODE)
    }

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