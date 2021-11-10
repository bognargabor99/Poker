package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import hu.bme.aut.onlab.poker.databinding.FragmentMainBinding
import hu.bme.aut.onlab.poker.model.Statistics
import hu.bme.aut.onlab.poker.network.PokerAPI
import hu.bme.aut.onlab.poker.network.PokerClient
import hu.bme.aut.onlab.poker.network.TableJoinedMessage
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class MainFragment : Fragment(), PokerClient.TableJoinedListener, PokerClient.StatisticsListener {
    private lateinit var binding: FragmentMainBinding

    var interactionEnabled: Boolean = PokerAPI.isConnected
        set(value) {
            activity?.runOnUiThread {
                binding.btnConnect.isEnabled = !value
                binding.btnPlay.isEnabled = value
                binding.btnStatistics.visibility = if (value && !PokerClient.isGuest) View.VISIBLE else View.GONE
            }
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MainActivity.backPressDisabled = false
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        PokerClient.tableJoinedListener = this

        _this = this
        setOnClickListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        interactionEnabled = PokerAPI.isConnected
    }

    private fun setOnClickListeners() {
        binding.btnConnect.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToAuthFragment())
        }

        binding.btnPlay.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToChoosePlayOrJoinFragment())
        }

        binding.btnStatistics.setOnClickListener {
            PokerClient.statsListener = this
            PokerClient.fetchStatistics()
        }
    }

    override fun tableJoined(joinedMessage: TableJoinedMessage) {
        Log.d("pokerWeb", "Got join message for table ${joinedMessage.tableId}")
        requireActivity().runOnUiThread {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToGamePlayContainerFragment(joinedMessage.tableId, joinedMessage.rules))
        }
    }

    override fun statisticsReceived(statistics: Statistics) {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToStatisticsFragment(statistics))
    }

    companion object {
        lateinit var _this: MainFragment
        const val POKER_DOMAIN = "75eb-152-66-182-115"
    }
}