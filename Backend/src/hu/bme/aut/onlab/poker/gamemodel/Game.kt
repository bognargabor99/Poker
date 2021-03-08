package hu.bme.aut.onlab.poker.gamemodel

import hu.bme.aut.onlab.poker.network.UserCollection

object Game {
    private val tables = mutableListOf<Table>()

    fun startTable(settings: TableSettings): Int {
        val newTable = Table(settings)
        tables.add(newTable)
        println("new Table created")
        return newTable.id
    }

    fun joinTable(tableId: Int, userName: String) {
        val success = tables.find { t -> t.id == tableId }
            ?.addPlayer()
        if (success?.first == true) {
            UserCollection.setPlayerForUser(userName, success.second!!)
            println("$userName added to table")
        }
    }

    fun getOpenTables(): List<Int> =
        tables.filter { table -> table.settings.isOpen && !table.isStarted }
            .map { it.id }
}