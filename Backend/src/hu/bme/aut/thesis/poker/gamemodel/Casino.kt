package hu.bme.aut.thesis.poker.gamemodel

import hu.bme.aut.thesis.poker.network.ActionIncomingMessage
import hu.bme.aut.thesis.poker.network.SpectatorSubscriptionMessage
import hu.bme.aut.thesis.poker.network.UserCollection
import hu.bme.aut.thesis.poker.network.User
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Represents the casino in the game
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
object Casino {
    private val tables = mutableListOf<Table>()

    /**
     * Start a [Table] with the given [TableRules]
     * @param rules Rules of the new table
     * @return Id of the new table
     * @author Bognar, Gabor Bela
     */
    fun startTable(rules: TableRules): Int {
        val newTable = Table(rules)
        tables.add(newTable)
        return newTable.id
    }

    /**
     * Adds a [User] to a [Table] as a player, if it can
     * @param tableId Id of the table to join. If null, the player will join a random table
     * @param userName Name of the user
     * @author Bognar, Gabor Bela
     */
    fun joinTable(tableId: Int?, userName: String) {
        val toJoin = tableId ?: getOpenTables().firstOrNull()
        val success = tables.firstOrNull { it.id == toJoin }
            ?.addInGamePlayer(userName)
        if (success == true) {
            println("Player: $userName added to table")
        }
    }

    /**
     * Adds a [User] to a [Table] as a spectator, if it can
     * @param subMessage Information of the table to spectate and the user to add as spectator
     * @author Bognar, Gabor Bela
     */
    fun addSpectator(subMessage: SpectatorSubscriptionMessage) : Int {
        println("New subscription received for ${subMessage.tableId}")
        val toSpectate = if (subMessage.tableId != null && tables.any { it.id == subMessage.tableId }) subMessage.tableId else tables.last().id
        return if (tables.single { it.id == toSpectate  }.run { isStarted && addSpectator(subMessage.userName)}) {
            println("Spectator: ${subMessage.userName} added to table$toSpectate")
            toSpectate
        }
        else
            0
    }

    /**
     * Removes a spectator from a [Table]
     * @param tableId Ids of the table to unsubscribe from
     * @param userName Name of the [User]
     * @author Bognar, Gabor Bela
     */
    fun removeSpectator(tableId: Int, userName: String) {
        val indexOfTable = tables.indexOfFirst { it.id == tableId }
        if (indexOfTable != -1 && tables[indexOfTable].removeSpectator(userName))
            println("Spectator: $userName removed from table$tableId")
    }

    /**
     * Delegates an [Action] to the corrensponding [Table]
     * @param actionMessage Information about the action
     * @author Bognar, Gabor Bela
     */
    fun performAction(actionMessage: ActionIncomingMessage) {
        tables.find { it.id == actionMessage.tableId && it.isStarted }
            ?.onAction(actionMessage)
    }

    /**
     * Fetches open [Table]s, which have not started yet
     * @return Ids of open tables
     * @author Bognar, Gabor Bela
     */
    fun getOpenTables(): List<Int> =
        tables.filter { table -> table.isOpen() && !table.isStarted }
            .map { it.id }

    /**
     * Removes a [User] from a [Table]
     * @param tableId Id of the table
     * @param user Name of the user
     * @author Bognar, Gabor Bela
     */
    fun removePlayerFromTable(user: String, tableId: Int) {
        tables.find { it.id == tableId }
            ?.let {
                it.playerDisconnected(user)
                it.removeSpectator(user)
            }
    }

    /**
     * Removes a [User] from the specified [Table]s
     * @param tablePlayingIds Ids of the tables the user is playing at
     * @param tableSpectatingIds Ids of the tables the user is spectating
     * @param user Name of the user
     * @author Bognar, Gabor Bela
     */
    fun removePlayerFromTables(user: String, tablePlayingIds: List<Int>, tableSpectatingIds: List<Int>) {
        tables.filter { (tablePlayingIds + tableSpectatingIds).contains(it.id) }
            .forEach { table ->
                table.playerDisconnected(user)
                table.removeSpectator(user)
            }
    }

    /**
     * Drops a [Table] from the managed tables
     * @param tableId Id of the table to remove
     * @author Bognar, Gabor Bela
     */
    fun closeTable(tableId: Int) {
        println("closing table $tableId")
        tables.removeIf { it.id == tableId }
        UserCollection.removeTableFromPLayers(tableId)
    }
}