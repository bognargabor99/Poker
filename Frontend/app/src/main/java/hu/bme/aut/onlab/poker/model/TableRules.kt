package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
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
