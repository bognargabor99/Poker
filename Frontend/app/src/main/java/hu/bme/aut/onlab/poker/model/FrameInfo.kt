package hu.bme.aut.onlab.poker.model

import android.widget.Button
import hu.bme.aut.onlab.poker.GamePlayFragment
import hu.bme.aut.onlab.poker.SpectatorFragment
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Model class about state of a frame about [GamePlayFragment]
 * @author Bognar, Gabor Bela
 */
data class GamePlayFrameInfo(
    val containerId: Int,
    var fragment: GamePlayFragment?,
    var currentTableId: Int = 0,
    val button: Button,
    var isFree: Boolean
)

/**
 * Model class about state of a frame about [SpectatorFragment]
 * @author Bognar, Gabor Bela
 */
@DelicateCoroutinesApi
data class SpectatorFrameInfo(
    val containerId: Int,
    var fragment: SpectatorFragment?,
    var currentTableId: Int = 0,
    val button: Button,
    var isFree: Boolean
)