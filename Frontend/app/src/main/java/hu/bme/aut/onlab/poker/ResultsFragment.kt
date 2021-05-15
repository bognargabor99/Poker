package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import hu.bme.aut.onlab.poker.adapter.ResultsAdapter
import hu.bme.aut.onlab.poker.databinding.FragmentResultsBinding
import hu.bme.aut.onlab.poker.view.PokerCardView

class ResultsFragment : DialogFragment() {
    private lateinit var binding: FragmentResultsBinding
    private lateinit var resultsAdapter: ResultsAdapter
    private val args: ResultsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultsBinding.inflate(layoutInflater, container, false)
        resultsAdapter = ResultsAdapter()
        binding.resultList.adapter = resultsAdapter
        resultsAdapter.submitList(args.winners.playerOrder)
        args.winners.tableCards.forEachIndexed { index, card ->
            val cardOnTable = binding.tableCards.getChildAt(index) as PokerCardView
            cardOnTable.isUpside = true
            cardOnTable.value = card.value
            cardOnTable.symbol = card.suit.ordinal
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val params = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }
}