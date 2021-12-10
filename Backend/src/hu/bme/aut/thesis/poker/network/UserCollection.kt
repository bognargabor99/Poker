package hu.bme.aut.thesis.poker.network

import hu.bme.aut.thesis.poker.data.DatabaseHelper
import hu.bme.aut.thesis.poker.gamemodel.Statistics
import hu.bme.aut.thesis.poker.gamemodel.TableRules
import hu.bme.aut.thesis.poker.gamemodel.Player
import hu.bme.aut.thesis.poker.gamemodel.Table
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*

/**
 * Manages the connected [User]s
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
object UserCollection {
    private val users = Collections.synchronizedSet<User?>(LinkedHashSet())

    /**
     * Adds a new [User] to the collection
     * @param newUser The [User] to add
     * @author Bognar, Gabor Bela
     */
    operator fun plusAssign(newUser: User) {
        users.add(newUser)
    }

    /**
     * Removes a [User] from the collection
     * @param user The [User] to remove
     * @author Bognar, Gabor Bela
     */
    operator fun minusAssign(user: User) {
        users.remove(user)
    }

    /**
     * Sends text to the [User] with the given [userName]
     * @param userName Name of the [User] to the the [NetworkMessage] to
     * @param data Text to send to the [User]
     * @param code The message code of the [data]
     * @author Bognar, Gabor Bela
     */
    fun sendToClient(userName: String, data: String, code: Int) {
        val networkMsg = NetworkMessage(code, data)
        users.find { it.name == userName }
            ?.sendToClient(networkMsg.toJsonString())
    }

    /**
     * Notifies the given [User]s that a [Table] has started
     * @param tableId Id of the started [Table]
     * @param usersInTable List of the [User]s to notify
     * @author Bognar, Gabor Bela
     */
    fun notifyGameStarted(tableId: Int, usersInTable: List<String>) {
        users.filter { usersInTable.contains(it.name) }
            .forEach { user ->
                val msg = GameStartedMessage(tableId)
                sendToClient(user.name, msg.toJsonString(), GameStartedMessage.MESSAGE_CODE)
            }
    }

    /**
     * Eliminates [User]s from a [Table] and informs other [Player]s at the table
     * @param tableId Id of the to eliminate from
     * @param toEliminate The [User]s to eliminate
     * @param toInform The [User]s to inform about elimination
     * @author Bognar, Gabor Bela
     */
    fun eliminateFromTable(tableId: Int, toEliminate: List<String>, toInform: List<String>) {
        toEliminate.forEach {
            sendToClient(it, EliminationMessage(tableId).toJsonString(), EliminationMessage.MESSAGE_CODE)
        }
        toInform.forEach { informed ->
            toEliminate.forEach { eliminated ->
                sendToClient(informed, DisconnectedPlayerMessage(tableId, eliminated).toJsonString(), DisconnectedPlayerMessage.MESSAGE_CODE)
            }
        }
    }

    /**
     * Informs a [User] about [Table] creation
     * @param user The [User] to inform
     * @param tableId Id of the started table
     * @author Bognar, Gabor Bela
     */
    fun tableCreated(user: String, tableId: Int) {
        val answer = TableCreatedMessage(tableId)
        sendToClient(user, answer.toJsonString(), TableCreatedMessage.MESSAGE_CODE)
    }

    /**
     * Informs a [User] about joining a [Table]
     * @param userName The [User] to inform
     * @param tableId Id of the joined [Table]
     * @param rules Rules of the joined [Table]
     * @author Bognar, Gabor Bela
     */
    fun tableJoined(userName: String, tableId: Int, rules: TableRules) {
        val answer = TableJoinedMessage(tableId, rules)
        sendToClient(userName, answer.toJsonString(), TableJoinedMessage.MESSAGE_CODE)
        users.find { it.name == userName }?.tablePlayingIds?.add(tableId)
    }

    /**
     * Removes a player from the specified tables
     * @param userName The [User] to remove
     * @param tables Ids of the [Table] to remove the player from
     * @author Bognar, Gabor Bela
     */
    fun removePlayerFromTables(userName: String, tables: MutableList<Int>) {
        users.find { it.name == userName }?.tablePlayingIds?.removeAll(tables)
    }

    /**
     * Informs a [User] about spectating a [Table]
     * @param userName The [User] to inform
     * @param tableId Id of the spectated [Table]
     * @param rules Rules of the spectated [Table]
     * @author Bognar, Gabor Bela
     */
    fun tableSpectated(userName: String, tableId: Int, rules: TableRules) {
        val answer = SubscriptionAcceptanceMessage(tableId, rules)
        sendToClient(userName, answer.toJsonString(), SubscriptionAcceptanceMessage.MESSAGE_CODE)
        users.find { it.name == userName }?.tableSpectatingIds?.add(tableId)
    }

    /**
     * Removes the [Table] from the handled tables of [User] instances
     * @param tableId Id of the [Table]
     * @author Bognar, Gabor Bela
     */
    fun removeTableFromPLayers(tableId: Int) {
        users.forEach {
            it.tablePlayingIds.remove(tableId)
            it.tableSpectatingIds.remove(tableId)
        }
    }

    /**
     * Determines if there is a client authorized with the given username
     * @param user Username
     * @return True, if the is someone already authorized with the username
     * @author Bognar, Gabor Bela
     */
    fun isAlreadyAuthenticated(user: String) =
        users.any { it.name == user }

    /**
     * Updates the stats for a [User]
     * @param userName Name of the [User]
     * @param stats The statistics to update with
     * @author Bognar, Gabor Bela
     */
    fun updateStats(userName: String, stats: Statistics) {
        if (users.find { it.name == userName }?.isGuest == false)
            DatabaseHelper.updateStatsForUser(userName, stats)
    }

    /**
     * Fetches the statistics for a [User]
     * @param userName The name of the [User]
     * @return [Statistics] of the given [User]
     * @author Bognar, Gabor Bela
     */
    fun getStatsForPlayer(userName: String): Statistics {
        return DatabaseHelper.getStatisticsByName(userName)
    }
}
