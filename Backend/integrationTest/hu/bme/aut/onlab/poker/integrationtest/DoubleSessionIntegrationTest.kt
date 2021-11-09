package hu.bme.aut.onlab.poker.integrationtest

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.integrationtestutils.AggressiveBot
import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
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
                    assertTrue(connInfo.userName.startsWith("guest"))
                    assertTrue(connInfo.userName.substring(5).toIntOrNull() != null)
                    delay(2000)
                }
            }

            val user2Thread = thread(start = false, isDaemon = true, name = "multiConnUser2") {
                handleWebSocketConversation("/") { incoming, _ ->
                    val receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("guest"))
                    assertTrue(connInfo.userName.substring(5).toIntOrNull() != null)
                    delay(2000)
                }
            }
            user1Thread.start()
            user2Thread.start()
            user1Thread.join()
            user2Thread.join()
            this.stop(10, 20)
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
                    assertTrue(connInfo.userName.startsWith("guest"))
                    assertTrue(connInfo.userName.substring(5).toIntOrNull() != null)
                    val ourName = connInfo.userName


                }
            }

            val user2Thread = thread(start = false, isDaemon = true, name = "multiConnUser2") {
                handleWebSocketConversation("/") { incoming, _ ->
                    val receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("guest"))
                    assertTrue(connInfo.userName.substring(5).toIntOrNull() != null)
                    val ourName = connInfo.userName

                    val bot = AggressiveBot(ourName)
                }
            }
            user1Thread.start()
            user2Thread.start()
            user1Thread.join()
            user2Thread.join()
            this.stop(10, 20)
        }
    }
}