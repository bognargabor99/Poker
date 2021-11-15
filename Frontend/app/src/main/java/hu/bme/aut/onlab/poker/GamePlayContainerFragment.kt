package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.muddzdev.styleabletoast.StyleableToast
import hu.bme.aut.onlab.poker.databinding.FragmentGamePlayContainerBinding
import hu.bme.aut.onlab.poker.model.GamePlayFrameInfo
import hu.bme.aut.onlab.poker.network.GameStateMessage
import hu.bme.aut.onlab.poker.network.PokerClient
import hu.bme.aut.onlab.poker.network.TableJoinedMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class GamePlayContainerFragment : Fragment(), PokerClient.TableJoinedListener, PokerClient.GamePlayReceiver {
    private lateinit var binding: FragmentGamePlayContainerBinding

    private var gamePlayInfos: MutableList<GamePlayFrameInfo> = mutableListOf()
    private val amIPlaying: Boolean
        get() = gamePlayInfos.any { !it.isFree }

    private val args: GamePlayContainerFragmentArgs by navArgs()
    private var activeTable: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGamePlayContainerBinding.inflate(layoutInflater, container, false)
        setOnClickListeners()

        PokerClient.tableJoinedListener = this
        PokerClient.receiver = this

        gamePlayInfos.add(GamePlayFrameInfo(binding.frameLayoutTable1.id, null, 0, binding.btnTable1, true))
        gamePlayInfos.add(GamePlayFrameInfo(binding.frameLayoutTable2.id, null, 0, binding.btnTable2, true))
        gamePlayInfos.add(GamePlayFrameInfo(binding.frameLayoutTable3.id, null, 0, binding.btnTable3, true))

        gamePlayInfos.firstOrNull()?.let {
            it.fragment = GamePlayFragment.newInstance(args.firstTableId, args.firstTableRules)
            it.currentTableId = args.firstTableId
            it.isFree = false
        }

        val tr = requireActivity().supportFragmentManager.beginTransaction()
        tr.replace(gamePlayInfos.first().containerId, gamePlayInfos.first().fragment!!)
        tr.commit()
        activeTable = 0
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.btnBack.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.leave_table_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    PokerClient.leaveTable(gamePlayInfos[activeTable].currentTableId)
                    gamePlayInfos[activeTable].isFree = true
                    gamePlayInfos[activeTable].currentTableId = 0
                    activity?.runOnUiThread {
                        gamePlayInfos[activeTable].button.isEnabled = false
                    }
                    if (!this.amIPlaying)
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
            findNavController().navigate(GamePlayContainerFragmentDirections.actionGamePlayContainerFragmentToChoosePlayOrJoinFragment())
        }
    }

    private fun findFreeContainerIndex(): Int =
        gamePlayInfos.indexOfFirst { it.isFree }

    override fun tableJoined(joinedMessage: TableJoinedMessage) {
        val containerIndex = findFreeContainerIndex()
        if (containerIndex == -1) {
            PokerClient.leaveTable(joinedMessage.tableId)
            return
        }

        gamePlayInfos[containerIndex].let {
            it.isFree = false
            it.fragment = GamePlayFragment.newInstance(joinedMessage.tableId, joinedMessage.rules)
            it.currentTableId = joinedMessage.tableId
        }

        val tr = requireActivity().supportFragmentManager.beginTransaction()
        tr.replace(gamePlayInfos[containerIndex].containerId, gamePlayInfos[containerIndex].fragment!!)
        tr.commit()

        requireActivity().runOnUiThread {
            gamePlayInfos[containerIndex].button.isEnabled = true
        }
    }

    override fun onTableStarted(tableId: Int) {
        gamePlayInfos.find { it.currentTableId == tableId }?.fragment?.onTableStarted(tableId)
    }

    override fun onNewGameState(stateMessage: GameStateMessage) {
        gamePlayInfos.find { it.currentTableId == stateMessage.tableId }?.fragment?.onNewGameState(stateMessage)
    }

    override fun onTurnEnd(turnEndMessage: TurnEndMessage) {
        gamePlayInfos.find { it.currentTableId == turnEndMessage.tableId }?.fragment?.onTurnEnd(turnEndMessage)
    }

    override fun onGetEliminated(tableId: Int) {
        gamePlayInfos.find { it.currentTableId == tableId }?.fragment?.onGetEliminated(tableId)
        activity?.runOnUiThread {
            StyleableToast.makeText(
                requireContext(),
                getString(R.string.eliminated_alert, tableId),
                Toast.LENGTH_LONG,
                R.style.custom_toast
            ).show()
        }

        gamePlayInfos.find { it.currentTableId == tableId }?.let {
            it.fragment = null
            it.isFree = true
            activity?.runOnUiThread {
                it.button.isEnabled = false
            }
        }
        if (tableId == gamePlayInfos[activeTable].currentTableId) {
            if (!this.amIPlaying)
                findNavController().popBackStack()
            else
                switchTable()
        }
        gamePlayInfos.find { it.currentTableId == tableId }?.currentTableId = 0
    }

    private fun switchTable() {
        val newlyActiveTable = gamePlayInfos.indexOfFirst { !it.isFree }
        if (newlyActiveTable != -1)
            requireActivity().runOnUiThread {
                activeTable = newlyActiveTable
                gamePlayInfos[activeTable].button.callOnClick()
                Log.d("pokerWeb", "manually perform button click")
            }
    }

    override fun onPlayerDisconnection(tableId: Int, name: String) {
        gamePlayInfos.find { it.currentTableId == tableId }?.fragment?.onPlayerDisconnection(tableId, name)
    }

    override fun onTableWin(tableId: Int) {
        gamePlayInfos.find { it.currentTableId == tableId }?.fragment?.onTableWin(tableId)
        activity?.runOnUiThread {
            StyleableToast.makeText(
                requireContext(),
                getString(R.string.congratulation, tableId),
                Toast.LENGTH_LONG,
                R.style.custom_toast
            ).show()
        }
        gamePlayInfos.find { it.currentTableId == tableId }?.let {
            it.fragment = null
            it.isFree = true
            activity?.runOnUiThread {
                it.button.isEnabled = false
            }
        }
        if (tableId == gamePlayInfos[activeTable].currentTableId) {
            if (!this.amIPlaying)
                findNavController().popBackStack()
            else
                switchTable()
        }
        gamePlayInfos.find { it.currentTableId == tableId }?.currentTableId = 0
    }
}