package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import hu.bme.aut.onlab.poker.adapter.ResultsAdapter
import hu.bme.aut.onlab.poker.databinding.FragmentResultsBinding
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import hu.bme.aut.onlab.poker.view.PokerCardView
import kotlinx.coroutines.DelicateCoroutinesApi

private const val WINNERS_PARAM = "winnners"

@DelicateCoroutinesApi
class ResultsFragment : DialogFragment() {
    private lateinit var binding: FragmentResultsBinding
    private lateinit var resultsAdapter: ResultsAdapter

    private lateinit var winners: TurnEndMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            winners = it.getParcelable(WINNERS_PARAM)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultsBinding.inflate(layoutInflater, container, false)
        resultsAdapter = ResultsAdapter()
        binding.resultList.adapter = resultsAdapter
        resultsAdapter.submitList(winners.playerOrder)
        winners.tableCards.forEachIndexed { index, card ->
            val cardOnTable = binding.tableCards.getChildAt(index) as PokerCardView
            cardOnTable.isUpside = true
            cardOnTable.value = card.value
            cardOnTable.symbol = card.suit.ordinal
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        val params = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun onDestroy() {
        isShowing = false
        super.onDestroy()
    }

    companion object {
        var isShowing = false

        @JvmStatic
        fun newInstance(turnEnd: TurnEndMessage) =
            ResultsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(WINNERS_PARAM, turnEnd)
                }
            }
    }
}