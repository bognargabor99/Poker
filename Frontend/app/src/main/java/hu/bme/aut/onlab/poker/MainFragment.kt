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
import hu.bme.aut.onlab.poker.network.SubscriptionAcceptanceMessage
import hu.bme.aut.onlab.poker.network.TableJoinedMessage
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class MainFragment : Fragment(), PokerClient.TableJoinedListener, PokerClient.StatisticsListener, PokerClient.SubscriptionAcceptanceListener {
    private lateinit var binding: FragmentMainBinding

    var interactionEnabled: Boolean = PokerAPI.isConnected
        set(value) {
            activity?.runOnUiThread {
                binding.btnConnect.isEnabled = !value
                binding.btnPlay.isEnabled = value
                binding.btnSpectate.isEnabled = value
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

        binding.btnSpectate.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.alert_spectate_table)
                .setNegativeButton(R.string.random) { _, _ ->
                    PokerClient.subListener = this
                    PokerClient.trySpectate(null)
                }
                .setPositiveButton(R.string.specific) { _, _ ->
                    spectateSpecificTable()
                }
                .show()
        }

        binding.btnStatistics.setOnClickListener {
            PokerClient.statsListener = this
            PokerClient.fetchStatistics()
        }
    }

    private fun spectateSpecificTable() {
        var table: Int
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.enter_id)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                table = input.text.toString().toInt()
                if (table >= 100) {
                    PokerClient.trySpectate(table)
                    PokerClient.subListener = this
                }
                else
                    toast("Please enter a valid ID")
            }
            .show()
    }

    fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
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

    override fun tableSpectated(subMessage: SubscriptionAcceptanceMessage) {
        Log.d("pokerWeb", "Got subscription acceptance message for table ${subMessage.tableId}")
        requireActivity().runOnUiThread {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToSpectatorContainerFragment(subMessage.tableId, subMessage.rules))
        }
    }

    companion object {
        lateinit var _this: MainFragment
        const val POKER_DOMAIN = "657a-37-220-136-8"
    }
}