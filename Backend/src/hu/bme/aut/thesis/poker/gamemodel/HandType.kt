package hu.bme.aut.thesis.poker.gamemodel

/**
 * Enum class for the types of a [Hand]
 * @author Bognar, Gabor Bela
 */
enum class HandType {
    ROYAL_FLUSH,
    STRAIGHT_FLUSH,
    FOUR_OF_A_KIND,
    FULL_HOUSE,
    FLUSH,
    STRAIGHT,
    THREE_OF_A_KIND,
    TWO_PAIR,
    PAIR,
    HIGH_CARD;
}