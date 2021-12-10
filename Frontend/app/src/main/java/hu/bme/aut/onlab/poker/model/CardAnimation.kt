package hu.bme.aut.onlab.poker.model

import android.widget.TextView
import hu.bme.aut.onlab.poker.view.PokerCardView

/**
 * Model class for mapping of cards and animations
 * @author Bognar, Gabor Bela
 */
data class CardAnimation(
    val card1: PokerCardView,
    val card2: PokerCardView,
    val animId: Int,
    val reverseAnimId: Int,
    val tvChance: TextView
)