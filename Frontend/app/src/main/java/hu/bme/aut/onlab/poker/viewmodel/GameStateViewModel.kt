package hu.bme.aut.onlab.poker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage

class GameStateViewModel : ViewModel() {
    var state: LiveData<GameStateMessage>? = null
    var turnEndInfo: LiveData<TurnEndMessage>? = null
}