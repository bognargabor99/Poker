package hu.bme.aut.thesis.poker.gamemodel

import hu.bme.aut.thesis.poker.network.ActionIncomingMessage
import hu.bme.aut.thesis.poker.network.SpectatorSubscriptionMessage
import hu.bme.aut.thesis.poker.network.UserCollection
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
object Game {
    private val tables = mutableListOf<Table>()

    fun startTable(rules: TableRules): Int {
        val newTable = Table(rules)
        tables.add(newTable)
        return newTable.id
    }

    fun joinTable(tableId: Int?, userName: String) {
        val toJoin = tableId ?: getOpenTables().first()
        val success = tables.firstOrNull { it.id == toJoin }
            ?.addInGamePlayer(userName)
        if (success == true) {
            println("Player: $userName added to table")
        }
    }

    fun addSpectator(subMessage: SpectatorSubscriptionMessage) : Int {
        println("New subscription received for ${subMessage.tableId}")
        val toSpectate = if (subMessage.tableId != null && tables.any { it.id == subMessage.tableId }) subMessage.tableId else tables.first().id
        return if (tables.single { it.id == toSpectate  }.run { isStarted && addSpectator(subMessage.userName)}) {
            println("Spectator: ${subMessage.userName} added to table$toSpectate")
            toSpectate
        }
        else
            0
    }

    fun removeSpectator(tableId: Int, userName: String) {
        val indexOfTable = tables.indexOfFirst { it.id == tableId }
        if (indexOfTable != -1 && tables[indexOfTable].removeSpectator(userName))
            println("Spectator: $userName removed from table$tableId")
    }

    fun performAction(actionMessage: ActionIncomingMessage) {
        tables.find { it.id == actionMessage.tableId }
            ?.onAction(actionMessage)
    }

    fun getOpenTables(): List<Int> =
        tables.filter { table -> table.isOpen() && !table.isStarted }
            .map { it.id }

    fun removePlayerFromTable(user: String, tableId: Int) {
        tables.find { it.id == tableId }
            ?.let {
                it.playerDisconnected(user)
                it.removeSpectator(user)
            }
    }

    fun removePlayerFromTables(user: String, tablePlayingIds: List<Int>, tableSpectatingIds: List<Int>) {
        tables.filter { (tablePlayingIds + tableSpectatingIds).contains(it.id) }
            .forEach { table ->
                table.playerDisconnected(user)
                table.removeSpectator(user)
            }
    }

    fun closeTable(tableId: Int) {
        println("closing table $tableId")
        tables.removeIf { it.id == tableId }
        UserCollection.removeTableFromPLayers(tableId)
    }
}