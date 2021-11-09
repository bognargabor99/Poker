package hu.bme.aut.onlab.poker.integrationtest

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.gamemodel.*
import hu.bme.aut.onlab.poker.network.ConnectionInfoMessage
import hu.bme.aut.onlab.poker.network.NetworkMessage
import hu.bme.aut.onlab.poker.plugins.configureWebSockets
import hu.bme.aut.onlab.poker.utils.MessageHelper
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.test.*

@DelicateCoroutinesApi
class SingleSessionIntegrationTest {
    /**
     * This test connects as a guest user and creates a [Table] with default [TableRules].
     * Then checks if the table is created by fetching open tables.
     * Then it leaves the table and checks if the table is closed.
     * @author Bognar, Gabor Bela
     */
    @Test
    fun startAndLeaveTableTest() {
        withTestApplication {
            application.configureWebSockets()
            handleWebSocketConversation("/") { incoming, outgoing ->
                // Receive connection info message
                var receivedText = (incoming.receive() as Frame.Text).readText()

                // Getting our guest user name
                val ourName: String = Gson().fromJson(Gson().fromJson(receivedText, NetworkMessage::class.java).data, ConnectionInfoMessage::class.java).userName

                // start table with default rules
                outgoing.send(Frame.Text(MessageHelper.getStartTableMessage(ourName)))

                // receive message and assert that it's about a table creation
                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getTableCreatedMessage(100), receivedText)

                // receive message and assert that it's about a joining a table
                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getTableJoinedMessage(100), receivedText)

                // asking for open tables
                outgoing.send(Frame.Text(MessageHelper.getGetOpenTablesMessage(ourName)))

                // receive open tables and assert that our table is there
                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getSendOpenTablesMessage(listOf(100)), receivedText)

                // leave the table
                outgoing.send(Frame.Text(MessageHelper.getLeaveTableMessage(ourName, 100)))

                // asking for open tables
                outgoing.send(Frame.Text(MessageHelper.getGetOpenTablesMessage(ourName)))

                // as we are the only one on the server and left the only open table
                // we expect that the server closed it
                receivedText = (incoming.receive() as Frame.Text).readText()
                assertEquals(MessageHelper.getSendOpenTablesMessage(listOf()), receivedText)
            }
            this.stop(10, 20)
        }
    }
}