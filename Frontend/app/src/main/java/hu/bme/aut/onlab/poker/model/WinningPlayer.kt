package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class WinningPlayer(
        val userName: String,
        val inHandCards: List<Card>?,
        val hand: Hand?,
        val winAmount: Int
) : Parcelable

//@Parcelize
//class Winners : ArrayList<WinningPlayer>(), Parcelable