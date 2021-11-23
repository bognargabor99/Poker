package hu.bme.aut.onlab.poker.model

import android.widget.TextView
import hu.bme.aut.onlab.poker.view.PokerCardView

data class CardAnimation(
    val card1: PokerCardView,
    val card2: PokerCardView,
    val animId: Int,
    val reverseAnimId: Int,
    val tvChance: TextView
)