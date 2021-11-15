package hu.bme.aut.onlab.poker.integrationtest

import com.google.gson.Gson
import integrationtestutils.AggressiveBot
import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.network.SendOpenTablesMessage
import hu.bme.aut.onlab.poker.network.TableCreatedMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import hu.bme.aut.onlab.poker.utils.MessageHelper
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlin.concurrent.thread
import kotlin.test.*

@DelicateCoroutinesApi
class DoubleSession {
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

    @Ignore
    fun allInOnAllHandsTest() {
        withTestApplication {
            application.configureWebSockets()
            val user1Thread = thread(start = false, isDaemon = true, name = "multiConnUser1") {
                handleWebSocketConversation("/") { incoming, outgoing ->
                    var receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("guest"))
                    assertTrue(connInfo.userName.substring(5).toIntOrNull() != null)
                    val ourName = connInfo.userName

                    val bot = AggressiveBot(ourName)

                    outgoing.send(Frame.Text(MessageHelper.getStartTableMessage(bot.userName)))

                    receivedText = (incoming.receive() as Frame.Text).readText()
                    val tableCreatedMessage = Gson().fromJson(receivedText, TableCreatedMessage::class.java)
                    val idOfTable = tableCreatedMessage.tableId

                    receivedText = (incoming.receive() as Frame.Text).readText()
                    assertEquals(MessageHelper.getTableJoinedMessage(idOfTable), receivedText)

                    receivedText = (incoming.receive() as Frame.Text).readText()
                    assertEquals(MessageHelper.getGameStartedMessage(idOfTable), receivedText)
                }
            }

            val user2Thread = thread(start = false, isDaemon = true, name = "multiConnUser2") {
                handleWebSocketConversation("/") { incoming, outgoing ->
                    var receivedText = (incoming.receive() as Frame.Text).readText()

                    val connInfo = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java)
                    assertTrue(connInfo.userName.startsWith("guest"))
                    assertTrue(connInfo.userName.substring(5).toIntOrNull() != null)
                    val ourName = connInfo.userName

                    val bot = AggressiveBot(ourName)

                    val idOfTable: Int

                    val frame = Frame.Text(MessageHelper.getGetOpenTablesMessage(ourName))
                    outgoing.send(frame)
                    receivedText = (incoming.receive() as Frame.Text).readText()
                    var sendOpenTablesMessage = Gson().fromJson(receivedText, SendOpenTablesMessage::class.java)
                    while (sendOpenTablesMessage.tableIds.isEmpty()) {
                        outgoing.send(frame)
                        receivedText = (incoming.receive() as Frame.Text).readText()
                        sendOpenTablesMessage = Gson().fromJson(receivedText, SendOpenTablesMessage::class.java)
                    }
                    idOfTable = sendOpenTablesMessage.tableIds.first()

                    outgoing.send(Frame.Text(MessageHelper.getJoinTableMessage(bot.userName, idOfTable)))

                    receivedText = (incoming.receive() as Frame.Text).readText()
                    assertEquals(MessageHelper.getTableJoinedMessage(idOfTable), receivedText)

                    receivedText = (incoming.receive() as Frame.Text).readText()
                    assertEquals(MessageHelper.getGameStartedMessage(idOfTable), receivedText)
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