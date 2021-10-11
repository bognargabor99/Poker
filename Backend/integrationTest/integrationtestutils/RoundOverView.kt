package hu.bme.aut.onlab.poker.integrationtestutils

import hu.bme.aut.onlab.poker.gamemodel.Card

data class RoundOverView(
    val inHandCards: List<Card>,
    val tableCards: List<Card>,
    val nextPlayer: String
)