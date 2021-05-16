package hu.bme.aut.onlab.poker.network

import android.util.Log
import hu.bme.aut.onlab.poker.model.Action
import hu.bme.aut.onlab.poker.model.TableRules
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PokerClient {
    lateinit var session: DefaultClientWebSocketSession
    lateinit var userName: String
    private var tables = mutableListOf<Int>()
    private val chain = NetworkChain()
    lateinit var listener: TableJoinedListener
    lateinit var receiver: GamePlayReceiver

    fun receiveText(text: String) {
        val message: NetworkMessage
        try {
            message = Json.decodeFromString(text)
        } catch (e: Exception) {
            Log.d("receive", "Couldn't decode text from server:\n$text")
            return
        }
        chain.process(message)
    }

    private fun sendToServer(data: String, code: Int) {
        val message = NetworkMessage(code, data)
        GlobalScope.launch {
            session.send(Frame.Text(Json.encodeToString(message)))
        }
    }

    fun setGameState(stateMessage: GameStateMessage) {
        receiver.onNewGameState(stateMessage)
    }

    fun setConnectionInfo(name: String) {
        userName = name
    }

    fun turnEnded(turnEndMessage: TurnEndMessage) {
        Log.d("pokerWeb", "Turn ended: $turnEndMessage")
        receiver.onTurnEnd(turnEndMessage)
        Thread.sleep(1000)
    }

    fun eliminatedFromTable(table: Int) {
        receiver.onGetEliminated(table)
    }

    fun startTable(rules: TableRules) {
        val createMessage = CreateTableMessage(userName, rules)
        sendToServer(Json.encodeToString(createMessage), CreateTableMessage.MESSAGE_CODE)
    }

    fun tableJoined(joinedMessage: TableJoinedMessage) {
        if (joinedMessage.tableId != 0 && !tables.contains(joinedMessage.tableId)) {
            tables.add(joinedMessage.tableId)
            listener.tableJoined(joinedMessage)
        }
    }

    fun tryJoin(tableId: Int?) {
        val joinMessage = JoinTableMessage(userName, tableId)
        sendToServer(Json.encodeToString(joinMessage), JoinTableMessage.MESSAGE_CODE)
    }

    fun leaveTable(tableId: Int) {
        val leaveMessage = LeaveTableMessage(userName, tableId)
        sendToServer(Json.encodeToString(leaveMessage), LeaveTableMessage.MESSAGE_CODE)
    }

    fun tableStarted(tableId: Int) {
        receiver.onTableStarted(tableId)
    }

    fun playerDisconnectedFromTable(userName: String) {
        receiver.onPlayerDisconnection(userName)
    }

    fun tableWon(tableId: Int) {
        receiver.onTableWin(tableId)
    }

    fun action(action: Action) {
        val actionMessage = ActionMessage(tables.last(), userName, action)
        sendToServer(Json.encodeToString(actionMessage), ActionMessage.MESSAGE_CODE)
    }

    interface TableJoinedListener {
        fun tableJoined(joinedMessage: TableJoinedMessage)
    }

    interface GamePlayReceiver {
        fun onTableStarted(tableId: Int)
        fun onNewGameState(stateMessage: GameStateMessage)
        fun onTurnEnd(turnEndMessage: TurnEndMessage)
        fun onGetEliminated(tableId: Int)
        fun onPlayerDisconnection(name: String)
        fun onTableWin(tableId: Int)
    }
}