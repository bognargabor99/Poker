package hu.bme.aut.onlab.poker.websocket

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.test.*

@DelicateCoroutinesApi
class WebSocketTest {
    @Test
    fun connectionTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/") { incoming, _ ->
                val firstReceivedText = (incoming.receive() as Frame.Text).readText()

                assertEquals(MessageHelper.getConnectionInfoMessage("user1"), firstReceivedText)
            }
        }
    }

    @Test
    fun startTableTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/") { incoming, outgoing ->
                var receivedText = (incoming.receive() as Frame.Text).readText()

                val ourName: String = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java).userName

                outgoing.send(Frame.Text(MessageHelper.getStartTableMessage(ourName)))

                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getTableCreatedMessage(100), receivedText)

                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getTableJoinedMessage(100), receivedText)

                outgoing.send(Frame.Text(MessageHelper.getGetOpenTablesMessage(ourName)))

                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getSendOpenTablesMessage(listOf(100)), receivedText)
            }
        }
    }
}