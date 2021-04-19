package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.network.ActionIncomingMessage

object Game {
    private val tables = mutableListOf<Table>()

    fun startTable(rules: TableRules): Int {
        val newTable = Table(rules)
        tables.add(newTable)
        return newTable.id
    }

    fun joinTable(tableId: Int?, userName: String) {
        val toJoin = tableId ?: getOpenTables().first()
        val success = tables.single { it.id == toJoin }
            .addPlayer(userName)
        if (success) {
            println("$userName added to table")
        }
    }

    fun performAction(actionMessage: ActionIncomingMessage) {
        tables.find { it.id == actionMessage.tableId }
            ?.onAction(actionMessage)
    }

    fun getOpenTables(): List<Int> =
        tables.filter { table -> table.isOpen() && !table.isStarted }
            .map { it.id }

    fun removePlayerFromTables(user: String, tableIds: MutableList<Int>) {
        tables.filter { tableIds.contains(it.id) }
            .forEach { table ->
                table.playerDisconnected(user)
            }
    }

    fun closeTable(tableId: Int) {
        println("closing table $tableId")
        tables.removeIf { it.id == tableId }
    }
}