package hu.bme.aut.thesis.poker.gamemodel

import java.time.LocalDate

data class Statistics (
    var registerDate: String = LocalDate.now().toString(),
    var tablesPlayed: Int = 0,
    var tablesWon: Int = 0,
    var allHands: Int = 0,
    var handsWon: Int = 0,
    var totalChipsWon: Int = 0,
    var biggestPotWon: Int = 0,
    var raiseCount: Int = 0,
    var showDownCount: Int = 0,
    var flopsSeen: Int = 0,
    var turnsSeen: Int = 0,
    var riversSeen: Int = 0,
    var playersBusted: Int = 0 // TODO
)