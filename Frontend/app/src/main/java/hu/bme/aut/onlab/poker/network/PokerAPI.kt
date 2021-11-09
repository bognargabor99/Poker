package hu.bme.aut.onlab.poker.network

import android.util.Log
import hu.bme.aut.onlab.poker.model.UserAuthInfo
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*

@OptIn(InternalAPI::class)
@DelicateCoroutinesApi
object PokerAPI {
    private val client = HttpClient {
        install(WebSockets)
    }
    lateinit var messageReceivingRoutine: Job

    val isConnected: Boolean
        get() {
            val ret = PokerAPI::messageReceivingRoutine.isInitialized && messageReceivingRoutine.isActive
            Log.d("pokerWeb", "isConnected: ${ if (ret) "YES" else "NO" }")
            return ret
        }

    fun connect(domain: String, authInfo: UserAuthInfo, onSuccess: () -> Unit, onError: () -> Unit) {
        GlobalScope.launch {
            try {
                client.wss(method = HttpMethod.Get, host = "$domain.ngrok.io", path = "/", request = {
                    headers {
                        remove(HttpHeaders.Authorization)
                        append(HttpHeaders.Authorization, authInfo.toAuthHeaderValue())
                    }
                }) {
                    onSuccess()
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