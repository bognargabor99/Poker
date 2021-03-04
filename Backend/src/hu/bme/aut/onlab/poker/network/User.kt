package hu.bme.aut.onlab.poker.network

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

class User(private val session: DefaultWebSocketSession) {
    val name = "user${lastId.getAndIncrement()}"

    fun receiveFromClient(receivedText: String) {
        Json.decodeFromString<NetworkMessage>(receivedText)
    }

    suspend fun sendToClient(textToSend: String) {
        session.send(textToSend)
    }

    companion object {
        var lastId = AtomicInteger(1)
    }
}