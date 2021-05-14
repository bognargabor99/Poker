package hu.bme.aut.onlab.poker.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*

object PokerAPI {
    private val client = HttpClient {
        install(WebSockets)
    }
    private lateinit var messageReceivingRoutine: Job

    fun connect(domain: String, onError: () -> Unit) {
        GlobalScope.launch {
            try {
                client.webSocket(method = HttpMethod.Get, host = "$domain.ngrok.io", path = "/") {
                    PokerClient.session = this
                    messageReceivingRoutine = launch { receiveMessages() }
                    messageReceivingRoutine.join()
                }
            } catch (cre: ClientRequestException) {
                onError()
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.receiveMessages() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val receivedBytes = message.readBytes()
                val text = String(receivedBytes)
                delay(600)
                Log.d("pokerWeb", text)
                PokerClient.receiveText(text)
            }
        } catch (e: Exception) {
            Log.d("pokerWeb","Error while receiving: " + e.localizedMessage)
        }
    }

    fun disConnect() {
        GlobalScope.launch {
            if (PokerAPI::messageReceivingRoutine.isInitialized)
                messageReceivingRoutine.cancelAndJoin()
        }
    }
}