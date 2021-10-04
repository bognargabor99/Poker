package hu.bme.aut.onlab.poker.websocket

import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

@ExperimentalSerializationApi
class WebSocketTest {
    @Test
    fun connectionTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/") { incoming, outgoing ->
                val firstReceivedText = (incoming.receive() as Frame.Text).readText()

                val expectedData = Json.encodeToString(ConnectionInfoMessage("user1"))
                val expectedText = Json.encodeToString(NetworkMessage(8, expectedData))

                assertEquals(expectedText, firstReceivedText)
            }
        }
    }
}