package hu.bme.aut.onlab.poker.websocket

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import hu.bme.aut.onlab.poker.utils.MessageHelper
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.Base64
import kotlin.test.*

@DelicateCoroutinesApi
class WebSocketTest {
    @Test
    fun connectionTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/") { incoming, _ ->
                val firstReceivedText = (incoming.receive() as Frame.Text).readText()

                val connInfo = Gson().fromJson(Gson().fromJson(firstReceivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                assertTrue(connInfo.userName.startsWith("user"))
                assertTrue(connInfo.userName.substring(4).toIntOrNull() != null)
            }
        }
    }

    @Test
    fun authenticatedConnectionSuccessTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/", setup = {
                val base64 = Base64.getEncoder().encodeToString("admin:admin".toByteArray())
                addHeader("Authorization", "Basic $base64")
            }) { incoming, _ ->
                val firstReceivedText = (incoming.receive() as Frame.Text).readText()

                assertEquals(MessageHelper.getConnectionInfoMessage("admin"), firstReceivedText)
            }
        }
    }

    @Test
    fun authenticatedConnectionFailureTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/", setup = {
                val base64 = Base64.getEncoder().encodeToString("admin:geza".toByteArray())
                addHeader("Authorization", "Basic $base64")
            }) { incoming, _ ->
                val firstReceivedText = (incoming.receive() as Frame.Text).readText()
                val connInfo = Gson().fromJson(Gson().fromJson(firstReceivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                assertNotEquals("admin", connInfo.userName)
            }
        }
    }
}