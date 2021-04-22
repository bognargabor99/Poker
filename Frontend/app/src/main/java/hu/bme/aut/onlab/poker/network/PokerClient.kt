package hu.bme.aut.onlab.poker.network

import android.util.Log
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

    fun sendToServer(data: String, code: Int) {
        val message = NetworkMessage(code, data)
        GlobalScope.launch {
            session.send(Frame.Text(Json.encodeToString(message)))
        }
    }

    fun setGameState(stateMessage: GameStateMessage) {
        //TODO("Not yet implemented")
        receiver.receiveMessage(stateMessage.toString())
    }

    fun setConnectionInfo(name: String) {
        userName = name
    }

    fun turnEnded(turnEndMessage: TurnEndMessage) {
        //TODO("Not yet implemented")
    }

    fun eliminated(table: Int) {
        //TODO("Not yet implemented")
    }

    fun startTable(rules: TableRules) {
        val createMessage = CreateTableMessage(userName, rules)
        sendToServer(Json.encodeToString(createMessage), CreateTableMessage.MESSAGE_CODE)
    }

    fun tableJoined(tableId: Int) {
        if (tableId != 0 && !tables.contains(tableId)) {
            tables.add(tableId)
            listener.tableJoined(tableId)
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

    interface TableJoinedListener {
        fun tableJoined(tableId: Int)
    }

    interface GamePlayReceiver {
        fun receiveMessage(text: String)
    }
}