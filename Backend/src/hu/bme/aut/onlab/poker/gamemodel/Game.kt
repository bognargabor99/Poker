package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.network.ActionIncomingMessage
import hu.bme.aut.onlab.poker.network.UserCollection
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

    fun addSpectator(tableId: Int, userName: String) : Boolean {
        val indexOfTable = tables.indexOfFirst { it.id == tableId }
        return if (indexOfTable != -1 && tables[indexOfTable].addSpectator(userName)) {
            println("Spectator: $userName added to table$tableId")
            true
        }
        else
            false
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

    fun getRulesByTableId(tableId: Int): TableRules =
        this.tables.find { it.id == tableId }?.rules!!
}