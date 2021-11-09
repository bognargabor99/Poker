package hu.bme.aut.onlab.poker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
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
    var playersBusted: Int = 0
) : Parcelable