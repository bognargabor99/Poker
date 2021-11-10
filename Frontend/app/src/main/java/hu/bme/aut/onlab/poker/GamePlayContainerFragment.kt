package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.muddzdev.styleabletoast.StyleableToast
import hu.bme.aut.onlab.poker.databinding.FragmentGamePlayContainerBinding
import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.PokerClient
import hu.bme.aut.onlab.poker.network.TableJoinedMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class GamePlayContainerFragment : Fragment(), PokerClient.TableJoinedListener, PokerClient.GamePlayReceiver {
    private lateinit var binding: FragmentGamePlayContainerBinding
    private val fragmentMapping: MutableMap<Int, GamePlayFragment> = mutableMapOf()
    private val tableToButtonMapping: MutableMap<Int, Button> = mutableMapOf()
    private val args: GamePlayContainerFragmentArgs by navArgs()
    private val containerAvailability: MutableList<Boolean> = MutableList(3) { true }
    private var currentTableId: Int = 0
    private var activeTable: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGamePlayContainerBinding.inflate(layoutInflater, container, false)
        setOnClickListeners()

        PokerClient.tableJoinedListener = this
        PokerClient.receiver = this

        fragmentMapping[args.firstTableId] = GamePlayFragment.newInstance(args.firstTableId, args.firstTableRules)

        currentTableId = args.firstTableId
        val tr = requireActivity().supportFragmentManager.beginTransaction()
        tr.replace(binding.frameLayoutTable1.id, fragmentMapping[args.firstTableId]!!)
        tr.commit()
        containerAvailability[0] = false
        tableToButtonMapping[currentTableId] = binding.btnTable1
        activeTable = 1
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.btnBack.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.leave_table_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    fragmentMapping.remove(currentTableId)
                    PokerClient.leaveTable(currentTableId)
                    if (fragmentMapping.isEmpty())
                        view?.findNavController()?.popBackStack()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }

        binding.btnTable1.setOnClickListener {
            binding.frameLayoutTable1.visibility = View.VISIBLE
            binding.frameLayoutTable2.visibility = View.INVISIBLE
            binding.frameLayoutTable3.visibility = View.INVISIBLE
            if (activeTable == 1)
                fragmentMapping[currentTableId]?.showActionButtonsIfNecessary()
        }

        binding.btnTable2.setOnClickListener {
            binding.frameLayoutTable1.visibility = View.INVISIBLE
            binding.frameLayoutTable2.visibility = View.VISIBLE
            binding.frameLayoutTable3.visibility = View.INVISIBLE
            if (activeTable == 2)
                fragmentMapping[currentTableId]?.showActionButtonsIfNecessary()
        }

        binding.btnTable3.setOnClickListener {
            binding.frameLayoutTable1.visibility = View.INVISIBLE
            binding.frameLayoutTable2.visibility = View.INVISIBLE
            binding.frameLayoutTable3.visibility = View.VISIBLE
            if (activeTable == 3)
                fragmentMapping[currentTableId]?.showActionButtonsIfNecessary()
        }

        binding.btnPlus.setOnClickListener {
            findNavController().navigate(GamePlayContainerFragmentDirections.actionGamePlayContainerFragmentToChoosePlayOrJoinFragment())
        }
    }

    private fun findFreeContainerId(): Int =
        containerAvailability.indexOfFirst { it }

    override fun tableJoined(joinedMessage: TableJoinedMessage) {
        val containerIndex = findFreeContainerId()
        if (fragmentMapping.size >= 3 || containerIndex == -1) {
            PokerClient.leaveTable(joinedMessage.tableId)
        }

        fragmentMapping[joinedMessage.tableId] = GamePlayFragment.newInstance(joinedMessage.tableId, joinedMessage.rules)

        val (button, containerId) = when (containerIndex) {
            0 -> Pair(binding.btnTable1, binding.frameLayoutTable1.id)
            1 -> Pair(binding.btnTable2, binding.frameLayoutTable2.id)
            2 -> Pair(binding.btnTable3, binding.frameLayoutTable3.id)
            else -> Pair(binding.btnTable3, binding.frameLayoutTable3.id)
        }

        val tr = requireActivity().supportFragmentManager.beginTransaction()
        tr.replace(containerId, fragmentMapping[joinedMessage.tableId]!!)
        tr.commit()

        tableToButtonMapping[joinedMessage.tableId] = button
        requireActivity().runOnUiThread {
            button.isEnabled = true
        }

        containerAvailability[containerIndex] = false
    }

    override fun onTableStarted(tableId: Int) {
        fragmentMapping[tableId]?.onTableStarted(tableId)
    }

    override fun onNewGameState(stateMessage: GameStateMessage) {
        fragmentMapping[stateMessage.tableId]?.onNewGameState(stateMessage)
    }

    override fun onTurnEnd(turnEndMessage: TurnEndMessage) {
        fragmentMapping[turnEndMessage.tableId]?.onTurnEnd(turnEndMessage)
    }

    override fun onGetEliminated(tableId: Int) {
        fragmentMapping[tableId]?.onGetEliminated(tableId)
        activity?.runOnUiThread {
            StyleableToast.makeText(
                requireContext(),
                getString(R.string.eliminated_alert, tableId),
                Toast.LENGTH_LONG,
                R.style.custom_toast
            ).show()
        }
        fragmentMapping.remove(tableId)
        val currentButton = tableToButtonMapping.remove(tableId)!!
        currentButton.isEnabled = false
        if (tableId == currentTableId) {
            if (tableToButtonMapping.isEmpty())
                findNavController().popBackStack()
            else
                switchTable(currentButton.id)
        }
    }

    private fun switchTable(currentButtonId: Int) {
        val destinationButton = tableToButtonMapping.filterValues { it.id != currentButtonId && it.isEnabled }.values.first()
        currentTableId = tableToButtonMapping.filterValues { it.id == destinationButton.id }.keys.maxByOrNull { it }!!
        requireActivity().runOnUiThread {
            Log.d("pokerWeb", "manually perform button click")
            destinationButton.callOnClick()
        }
    }

    override fun onPlayerDisconnection(tableId: Int, name: String) {
        fragmentMapping[tableId]?.onPlayerDisconnection(tableId, name)
    }

    override fun onTableWin(tableId: Int) {
        fragmentMapping[tableId]?.onTableWin(tableId)
        activity?.runOnUiThread {
            StyleableToast.makeText(
                requireContext(),
                getString(R.string.congratulation, tableId),
                Toast.LENGTH_LONG,
                R.style.custom_toast
            ).show()
        }
        fragmentMapping.remove(tableId)
        val currentButtonId = tableToButtonMapping.remove(tableId)!!.id
        tableToButtonMapping.remove(tableId)?.isEnabled = false
        if (tableId == currentTableId) {
            if (tableToButtonMapping.isEmpty())
                findNavController().popBackStack()
            else
                switchTable(currentButtonId)
        }
    }
}