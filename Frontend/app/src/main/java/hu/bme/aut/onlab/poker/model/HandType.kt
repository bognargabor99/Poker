package hu.bme.aut.onlab.poker.model

enum class HandType(val asString: String) {
    ROYAL_FLUSH("Royal Flush"),
    STRAIGHT_FLUSH("Straight Flush"),
    FOUR_OF_A_KIND("Four of a Kind"),
    FULL_HOUSE("Full House"),
    FLUSH("Flush"),
    STRAIGHT("Straight"),
    THREE_OF_A_KIND("Three of a kind"),
    TWO_PAIR("Two pair"),
    PAIR("Pair"),
    HIGH_CARD("High Card");
}