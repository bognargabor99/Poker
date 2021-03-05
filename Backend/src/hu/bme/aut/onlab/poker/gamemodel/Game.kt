package hu.bme.aut.onlab.poker.gamemodel

object Game {
    private val tables = mutableListOf<Table>()

    fun startTable(settings: TableSettings): Int {
        val newTable = Table(settings)
        tables.add(newTable)
        return newTable.Id
    }

    fun joinTable(tableId: Int, userId: Int) {
        tables.find { t -> t.Id == tableId }
            //?.addPlayer(userId)
        TODO("Implement collection of users for attaching player objects for them")
    }
}