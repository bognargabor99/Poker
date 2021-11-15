package hu.bme.aut.onlab.poker.model

import android.widget.Button
import hu.bme.aut.onlab.poker.GamePlayFragment

data class GamePlayFrameInfo(
    val containerId: Int,
    var fragment: GamePlayFragment?,
    var currentTableId: Int = 0,
    val button: Button,
    var isFree: Boolean
)