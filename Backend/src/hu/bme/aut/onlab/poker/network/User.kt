package hu.bme.aut.onlab.poker.network

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

class User(private val session: DefaultWebSocketSession) {
    val name = "user${lastId.getAndIncrement()}"
    private val chain = NetworkChain()

    init {
        UserCollection += this
    }

    fun receiveFromClient(receivedText: String) {
        val request = Json.decodeFromString<NetworkRequest>(receivedText)
        println("decoded JSON request")
        chain.process(request)
    }

    fun sendToClient(textToSend: String) = GlobalScope.launch { session.send(textToSend) }

    fun notifyGameStarted(tableId: Int) {
        GlobalScope.launch {
            session.send("Table $tableId started")
        }
    }

    companion object {
        var lastId = AtomicInteger(1)
    }
}