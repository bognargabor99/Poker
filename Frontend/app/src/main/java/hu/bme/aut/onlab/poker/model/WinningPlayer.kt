package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import hu.bme.aut.onlab.poker.network.TurnEndMessage

/**
 * DTO (data transfer object) class for [TurnEndMessage]
 * about necessary data of players at the end of a turn
 * @author Bognar, Gabor Bela
 */
@Parcelize
data class WinningPlayer(
        val userName: String,
        val inHandCards: List<Card>?,
        val hand: Hand?,
        val winAmount: Int
) : Parcelable