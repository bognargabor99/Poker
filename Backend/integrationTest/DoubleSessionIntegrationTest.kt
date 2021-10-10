package hu.bme.aut.onlab.poker

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.concurrent.thread
import kotlin.test.*

@DelicateCoroutinesApi
class DoubleSessionIntegrationTest {
    @Test
    fun multipleConnectionTest() {
        withTestApplication {
            application.configureWebSockets()
            val user1Thread = thread(start = false, isDaemon = true, name = "multiConnUser1") {
                handleWebSocketConversation("/") { incoming, _ ->
                    val receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("user"))
                    assertTrue(connInfo.userName.substring(4).toIntOrNull() != null)
                    val ourName = connInfo.userName

                    Thread.sleep(2000)
                }
            }

            val user2Thread = thread(start = false, isDaemon = true, name = "multiConnUser2") {
                handleWebSocketConversation("/") { incoming, _ ->
                    val receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("user"))
                    assertTrue(connInfo.userName.substring(4).toIntOrNull() != null)
                    val ourName = connInfo.userName

                    Thread.sleep(2000)
                }
            }
            user1Thread.start()
            user2Thread.start()
            user1Thread.join()
            user2Thread.join()
        }
    }

    @Test
    fun allInOnAllHandsTest() {
        withTestApplication {
            application.configureWebSockets()
            val user1Thread = thread(start = false, isDaemon = true, name = "multiConnUser1") {
                handleWebSocketConversation("/") { incoming, _ ->
                    val receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("user"))
                    assertTrue(connInfo.userName.substring(4).toIntOrNull() != null)
                    val ourName = connInfo.userName


                }
            }

            val user2Thread = thread(start = false, isDaemon = true, name = "multiConnUser2") {
                handleWebSocketConversation("/") { incoming, _ ->
                    val receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("user"))
                    assertTrue(connInfo.userName.substring(4).toIntOrNull() != null)
                    val ourName = connInfo.userName


                }
            }
            user1Thread.start()
            user2Thread.start()
            user1Thread.join()
            user2Thread.join()
        }
    }
}