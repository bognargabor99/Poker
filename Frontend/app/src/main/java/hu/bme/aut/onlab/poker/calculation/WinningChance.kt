package hu.bme.aut.onlab.poker.calculation

import kotlin.math.round

data class WinningChance(var allHands: Int = 0, var handsWon: Int = 0, var tieHandsWon: Int = 0) {
    val winChance: Double
        get() = round(handsWon.toDouble()*10000/allHands)/100
    val tieChance: Double
        get() = round(tieHandsWon.toDouble()*10000/allHands)/100

    override fun toString(): String {
        return "Win: $winChance%\nTie: $tieChance%"
    }
}