package hu.bme.aut.onlab.poker.network

import io.ktor.http.cio.websocket.*
import hu.bme.aut.onlab.poker.gamemodel.Player
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

class User(private val session: DefaultWebSocketSession) {
    val name = "user${lastId.getAndIncrement()}"
    private val chain = NetworkChain()
    lateinit var player: Player

    init {
        //TODO("User Collection")
    }

    suspend fun receiveFromClient(receivedText: String) {
        val request = Json.decodeFromString<NetworkRequest>(receivedText)
        chain.process(request)
    }

    suspend fun sendToClient(textToSend: String) {
        session.send(textToSend)
    }

    companion object {
        var lastId = AtomicInteger(1)
    }
}