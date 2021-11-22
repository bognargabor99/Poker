package hu.bme.aut.onlab.poker.model

import hu.bme.aut.onlab.poker.view.PokerCardView

data class CardAnimation(
    val card1: PokerCardView,
    val card2: PokerCardView,
    val animId: Int,
    val reverseAnimId: Int,
)