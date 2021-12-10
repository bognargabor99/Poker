package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents rules of the table
 * @property isOpen Is the table open for anyone?
 * @property playerCount Number of [Player]s at the table
 * @property playerStartingStack Starting chipstack of the players
 * @property doubleBlindsAfterTurnCount Number of turns the blinds are doubled after
 * @property bigBlindStartingAmount Starting amount of the big blind
 * @property isRoyal Is the table played with a royal deck (only 10, J, Q, K, A)
 * @author Bognar, Gabor Bela
 */
@Parcelize
data class TableRules(
    val isOpen: Boolean,
    val playerCount: Int,
    val bigBlindStartingAmount: Int,
    val doubleBlindsAfterTurnCount: Int,
    val playerStartingStack: Int,
    val isRoyal: Boolean
) : Parcelable {
    init {
        require(playerCount >= 2)
        require(playerCount <= 5)
    }
}
