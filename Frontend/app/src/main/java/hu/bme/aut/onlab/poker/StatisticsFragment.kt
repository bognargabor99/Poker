package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import hu.bme.aut.onlab.poker.databinding.FragmentStatisticsBinding
import hu.bme.aut.onlab.poker.network.PokerClient
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class StatisticsFragment : DialogFragment() {
    private lateinit var binding: FragmentStatisticsBinding
    private val args: StatisticsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val stat = args.statistics

        binding.tvStatAllHands.text = stat.allHands.toString()
        binding.tvStatBiggestPot.text = stat.biggestPotWon.toString()
        binding.tvStatChipsWon.text = stat.totalChipsWon.toString()
        binding.tvStatFlops.text = stat.flopsSeen.toString()
        binding.tvStatHandsWon.text = stat.handsWon.toString()
        binding.tvStatName.text = PokerClient.userName
        binding.tvStatPlayersBusted.text = stat.playersBusted.toString()
        binding.tvStatRaiseCount.text = stat.raiseCount.toString()
        binding.tvStatRegister.text = stat.registerDate
        binding.tvStatRivers.text = stat.riversSeen.toString()
        binding.tvStatShowdown.text = stat.showDownCount.toString()
        binding.tvStatTablesPlayed.text = stat.tablesPlayed.toString()
        binding.tvStatTablesWon.text = stat.tablesWon.toString()
        binding.tvStatTurns.text = stat.turnsSeen.toString()
    }
}