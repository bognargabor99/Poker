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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.muddzdev.styleabletoast.StyleableToast
import hu.bme.aut.onlab.poker.databinding.FragmentSpectatorContainerBinding
import hu.bme.aut.onlab.poker.model.SpectatorFrameInfo
import hu.bme.aut.onlab.poker.network.PokerClient
import hu.bme.aut.onlab.poker.network.SpectatorGameStateMessage
import hu.bme.aut.onlab.poker.network.SubscriptionAcceptanceMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SpectatorContainerFragment : Fragment(), PokerClient.SubscriptionAcceptanceListener, PokerClient.SpectatorGamePlayReceiver {
    private lateinit var binding: FragmentSpectatorContainerBinding
    private val args: SpectatorContainerFragmentArgs by navArgs()
    private var activeTable: Int = 0
    private val spectatorInfos: MutableList<SpectatorFrameInfo> = mutableListOf()
    private val amISpectating: Boolean
        get() = spectatorInfos.any { !it.isFree }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpectatorContainerBinding.inflate(layoutInflater, container, false)
        setOnClickListeners()
        PokerClient.subListener = this
        PokerClient.spectatingReceiver = this

        spectatorInfos.add(SpectatorFrameInfo(binding.frameLayoutTable1.id, null, 0, binding.btnTable1, true))
        spectatorInfos.add(SpectatorFrameInfo(binding.frameLayoutTable2.id, null, 0, binding.btnTable2, true))
        spectatorInfos.add(SpectatorFrameInfo(binding.frameLayoutTable3.id, null, 0, binding.btnTable3, true))

        spectatorInfos.firstOrNull()?.let {
            it.fragment = SpectatorFragment.newInstance(args.firstTableId, args.firstTableRules)
            it.currentTableId = args.firstTableId
            it.isFree = false
        }

        val tr = requireActivity().supportFragmentManager.beginTransaction()
        tr.replace(spectatorInfos.first().containerId, spectatorInfos.first().fragment!!)
        tr.commit()
        activeTable = 0
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.btnBack.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.leave_table_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    PokerClient.unsubscribe(spectatorInfos[activeTable].currentTableId)
                    spectatorInfos[activeTable].isFree = true
                    spectatorInfos[activeTable].currentTableId = 0
                    activity?.runOnUiThread {
                        spectatorInfos[activeTable].button.isEnabled = false
                    }
                    if (!this.amISpectating)
                        view?.findNavController()?.popBackStack()
                    else
                        switchTable()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }

        binding.btnTable1.setOnClickListener {
            binding.frameLayoutTable1.visibility = View.VISIBLE
            binding.frameLayoutTable2.visibility = View.INVISIBLE
            binding.frameLayoutTable3.visibility = View.INVISIBLE
            activeTable = 0
        }

        binding.btnTable2.setOnClickListener {
            binding.frameLayoutTable1.visibility = View.INVISIBLE
            binding.frameLayoutTable2.visibility = View.VISIBLE
            binding.frameLayoutTable3.visibility = View.INVISIBLE
            activeTable = 1
        }

        binding.btnTable3.setOnClickListener {
            binding.frameLayoutTable1.visibility = View.INVISIBLE
            binding.frameLayoutTable2.visibility = View.INVISIBLE
            binding.frameLayoutTable3.visibility = View.VISIBLE
            activeTable = 2
        }

        binding.btnPlus.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.alert_join_table)
                .setNegativeButton(R.string.random) { _, _ ->
                    PokerClient.trySpectate(null)
                }
                .setPositiveButton(R.string.specific) { _, _ ->
                    spectateSpecificTable()
                }
                .show()
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
                if (table >= 100)
                    PokerClient.trySpectate(table)
                else
                    toast("Please enter a valid ID")
            }
            .show()
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun tableSpectated(subMessage: SubscriptionAcceptanceMessage) {
        val containerIndex = findFreeContainerIndex()
        if (containerIndex == -1) {
            PokerClient.unsubscribe(subMessage.tableId)
            return
        }

        spectatorInfos[containerIndex].let {
            it.isFree = false
            it.fragment = SpectatorFragment.newInstance(subMessage.tableId, subMessage.rules)
            it.currentTableId = subMessage.tableId
        }

        val tr = requireActivity().supportFragmentManager.beginTransaction()
        tr.replace(spectatorInfos[containerIndex].containerId, spectatorInfos[containerIndex].fragment!!)
        tr.commit()

        requireActivity().runOnUiThread {
            spectatorInfos[containerIndex].button.isEnabled = true
        }
    }

    private fun findFreeContainerIndex(): Int =
        spectatorInfos.indexOfFirst { it.isFree }

    override fun onNewGameState(stateMessage: SpectatorGameStateMessage) {
        Log.d("pokerWeb", "Container: New spectator state message received for table: ${stateMessage.tableId}")
        spectatorInfos.find { it.currentTableId == stateMessage.tableId }?.fragment?.onNewGameState(stateMessage)
    }

    override fun onTurnEnd(turnEndMessage: TurnEndMessage) {
        spectatorInfos.find { it.currentTableId == turnEndMessage.tableId }?.fragment?.onTurnEnd(turnEndMessage)
    }

    override fun onPlayerDisconnection(tableId: Int, name: String) {
        spectatorInfos.find { it.currentTableId == tableId }?.fragment?.onPlayerDisconnection(tableId, name)
    }

    override fun onTableWin(tableId: Int, name: String) {
        spectatorInfos.find { it.currentTableId == tableId }?.fragment?.onTableWin(tableId, name)
        activity?.runOnUiThread {
            StyleableToast.makeText(
                requireContext(),
                getString(R.string.winner_announcement, name, tableId),
                Toast.LENGTH_LONG,
                R.style.custom_toast
            ).show()
        }
        spectatorInfos.find { it.currentTableId == tableId }?.let {
            it.fragment = null
            it.isFree = true
            activity?.runOnUiThread {
                it.button.isEnabled = false
            }
        }
        if (tableId == spectatorInfos[activeTable].currentTableId) {
            if (!this.amISpectating)
                findNavController().popBackStack()
            else
                switchTable()
        }
        spectatorInfos.find { it.currentTableId == tableId }?.currentTableId = 0
    }

    private fun switchTable() {
        val newlyActiveTable = spectatorInfos.indexOfFirst { !it.isFree }
        if (newlyActiveTable != -1)
            requireActivity().runOnUiThread {
                activeTable = newlyActiveTable
                spectatorInfos[activeTable].button.callOnClick()
            }
    }
}