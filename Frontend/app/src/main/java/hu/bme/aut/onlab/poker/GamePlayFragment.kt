package hu.bme.aut.onlab.poker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.isVisible
import com.muddzdev.styleabletoast.StyleableToast
import hu.bme.aut.onlab.poker.calculation.HandEvaluator
import hu.bme.aut.onlab.poker.databinding.AvatarBinding
import hu.bme.aut.onlab.poker.databinding.FragmentGamePlayBinding
import hu.bme.aut.onlab.poker.model.*
import hu.bme.aut.onlab.poker.network.*
import hu.bme.aut.onlab.poker.view.PokerCardView
import kotlinx.coroutines.DelicateCoroutinesApi

private const val TABLE_ID_PARAM = "tableId"
private const val RULE_PARAM = "rules"

@OptIn(DelicateCoroutinesApi::class)
class GamePlayFragment : Fragment(), PokerClient.GamePlayReceiver {
    private lateinit var binding: FragmentGamePlayBinding
    private lateinit var tableCards: List<PokerCardView>
    private var tableStart = true
    private var newTurn = true
    private lateinit var oldState: GameStateMessage
    private lateinit var newState: GameStateMessage
    private lateinit var lastTurnResults: TurnEndMessage
    private val avatarMap = mutableMapOf<String, AvatarBinding>()
    private var tableId: Int = 0
    private lateinit var tableRules: TableRules

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tableId = it.getInt(TABLE_ID_PARAM)
            tableRules = it.getParcelable(RULE_PARAM)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MainActivity.backPressDisabled = true
        binding = FragmentGamePlayBinding.inflate(layoutInflater, container, false)
        tableCards = listOf(binding.tableCard1, binding.tableCard2, binding.tableCard3, binding.tableCard4, binding.tableCard5)
        binding.tvTableId.text = getString(R.string.game_play_table_id, tableId)
        setOnClickListeners()
        StyleableToast.makeText(requireContext(), getString(R.string.table_join_success), Toast.LENGTH_LONG, R.style.custom_toast).show()
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.btnFold.setOnClickListener {
            PokerClient.action(Action(ActionType.FOLD, 0), tableId)
            foldCards()
        }
        binding.btnCheck.setOnClickListener {
            PokerClient.action(Action(ActionType.CHECK, 0), tableId)
            binding.actionButtons.visibility = View.GONE
        }
        binding.btnCall.setOnClickListener {
            PokerClient.action(Action(ActionType.CALL, newState.maxRaiseThisRound), tableId)
            binding.actionButtons.visibility = View.GONE
        }
        binding.btnRaise.setOnClickListener { askForAmount() }
        binding.btnLastTurn.setOnClickListener { showTurnResults() }
    }

    override fun onTableStarted(tableId: Int) {
        if (tableId != this.tableId)
            return

        activity?.runOnUiThread {
            StyleableToast.makeText(
                requireContext(),
                getString(R.string.table_started),
                Toast.LENGTH_LONG,
                R.style.custom_toast
            ).show()

            binding.progressbar.visibility = View.INVISIBLE
            binding.tvWaitForOthers.visibility = View.INVISIBLE
            displayAvatars()
        }
    }

    /**
     * Handle new game state
     * @author Bognar, Gabor Bela
     */
    override fun onNewGameState(stateMessage: GameStateMessage) {
        if (stateMessage.tableId != tableId)
            return
        if (this::newState.isInitialized)
            oldState = newState
        newState = stateMessage
        if (tableStart) mapAvatars()
        if (newTurn) {
            handCards()
            disableTableCards()
            newTurn = false
        }
        if (newState.lastAction == null) {
            putCardsOnTable()
            Thread.sleep(800)
            setDefaultAvatarThemes()
        } else {
            setAvatarTheme(newState.lastAction!!)
        }
        displayChipCounts()
        displayCurrentHand()
        showActionButtonsIfNecessary()
    }

    /**
     * Sets the theme of an avatar
     * @author Bognar, Gabor Bela
     */
    private fun setAvatarTheme(action: ActionMessage) {
        val resourceId = when (action.action.type) {
            ActionType.CHECK -> R.drawable.avatar_background_check
            ActionType.CALL -> R.drawable.avatar_background_call
            ActionType.RAISE -> R.drawable.avatar_background_raise
            else -> R.drawable.avatar_background_default
        }
        activity?.runOnUiThread {
            avatarMap[action.name].let {
                it?.root?.setBackgroundResource(resourceId)
                it?.tvLastAction?.visibility = View.VISIBLE
                it?.tvLastAction?.text = action.action.type.name
            }
        }
    }

    /**
     * Sets default (grey) theme for all avatars
     * @author Bognar, Gabor Bela
     */
    private fun setDefaultAvatarThemes() {
        activity?.runOnUiThread {
            avatarMap.values.forEach {
                it.tvLastAction.visibility = View.GONE
                it.root.setBackgroundResource(R.drawable.avatar_background_default)
            }
        }
    }

    private fun disableTableCards() {
        tableCards.forEach { it.visibility = View.INVISIBLE }
    }

    /**
     * Puts cards on the table
     * @author Bognar, Gabor Bela
     */
    private fun putCardsOnTable() {
        val range = when (newState.turnState) {
            TurnState.AFTER_FLOP -> 0..2
            TurnState.AFTER_TURN -> 3..3
            TurnState.AFTER_RIVER -> (oldState.tableCards.size)..4
            else -> IntRange.EMPTY
        }
        activity?.runOnUiThread {
            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.card_animation)
            range.forEach { i ->
                tableCards[i].visibility = View.VISIBLE
                tableCards[i].value = newState.tableCards[i].value
                tableCards[i].symbol = newState.tableCards[i].suit.ordinal
                tableCards[i].startAnimation(anim)
            }
        }
        if (!range.isEmpty())
            Thread.sleep(500)
    }

    /**
     * Handles end of turn
     * @author Bognar, Gabor Bela
     */
    override fun onTurnEnd(turnEndMessage: TurnEndMessage) {
        if (turnEndMessage.tableId != tableId)
            return
        lastTurnResults = turnEndMessage
        activity?.runOnUiThread { binding.btnLastTurn.visibility = View.VISIBLE }
        if (!ResultsFragment.isShowing)
            showTurnResults()
        gatherCards()
        newTurn = true
    }

    /**
     * Shows [ResultsFragment]
     * @author Bognar, Gabor Bela
     */
    private fun showTurnResults() {
        if (!(requireView().parent!! as ViewGroup).isVisible)
            return
        val resultsFragment = ResultsFragment.newInstance(lastTurnResults)
        resultsFragment.show(requireActivity().supportFragmentManager, "result")
    }

    /**
     * Displays current [Hand] of the user
     * @author Bognar, Gabor Bela
     */
    private fun displayCurrentHand() {
        val cards = mutableListOf<Card>()
        cards.addAll(newState.tableCards)
        cards.addAll(newState.receiverCards)
        val type = HandEvaluator.evaluateHand(cards).type.asString
        activity?.runOnUiThread {
            binding.tvCurrentHand.visibility = View.VISIBLE
            binding.tvCurrentHand.text = type
        }
    }

    override fun onGetEliminated(tableId: Int) { }

    override fun onPlayerDisconnection(tableId: Int, name: String) {
        if (tableId != this.tableId)
            return
        activity?.runOnUiThread {
            avatarMap[name]?.tvLastAction?.visibility = View.VISIBLE
            avatarMap[name]?.tvLastAction?.text = getString(R.string.out)
            avatarMap.remove(name)
        }
    }

    override fun onTableWin(tableId: Int) { }

    /**
     * Asks for amount when the user wants to raise
     * @author Bognar, Gabor Bela
     */
    private fun askForAmount() {
        val numPicker = NumberPicker(requireContext())
        val minVal = newState.maxRaiseThisRound + newState.bigBlind
        val myStack = newState.players.single { player -> player.userName == PokerClient.userName }.run { chipStack + inPotThisRound }
        val displayed = (minVal..myStack step newState.bigBlind).toMutableList()
        if (displayed.isEmpty() || displayed.last() != myStack)
            displayed.add(myStack)
        val asArray = displayed.map { it.toString() }.toTypedArray()
        numPicker.displayedValues = asArray
        numPicker.maxValue = asArray.size - 1
        numPicker.minValue = 0
        activity?.runOnUiThread {
            AlertDialog.Builder(requireContext())
                .setTitle("Raise to:")
                .setView(numPicker)
                .setPositiveButton(R.string.ok) { _, _ ->
                    PokerClient.action(Action(ActionType.RAISE, numPicker.displayedValues[numPicker.value].toInt()), tableId)
                    binding.actionButtons.visibility = View.GONE
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()
        }
    }

    private fun handCards() {
        activity?.runOnUiThread {
            binding.playerCard1.alpha = 1f
            binding.playerCard1.value = newState.receiverCards.first().value
            binding.playerCard1.symbol = newState.receiverCards.first().suit.ordinal
            binding.playerCard2.alpha = 1f
            binding.playerCard2.value = newState.receiverCards.last().value
            binding.playerCard2.symbol = newState.receiverCards.last().suit.ordinal
            animatePlayerCards()
        }
    }

    /**
     * Animates cards
     * @author Bognar, Gabor Bela
     */
    private fun animatePlayerCards() {
        AnimationUtils.loadAnimation(requireContext(), R.anim.card_animation).also {
            binding.playerCard1.isVisible = true
            binding.playerCard2.isVisible = true
            binding.playerCard1.startAnimation(it)
            binding.playerCard2.startAnimation(it)
        }
    }

    /**
     * Fold cards with animation
     * @author Bognar, Gabor Bela
     */
    private fun foldCards() {
        activity?.runOnUiThread {
            AnimationUtils.loadAnimation(requireContext(), R.anim.reverse_card_animation).also {
                it.fillAfter = false
                binding.playerCard1.startAnimation(it)
                binding.playerCard2.startAnimation(it)
                binding.playerCard1.alpha = 0.5f
                binding.playerCard2.alpha = 0.5f
            }
        }
    }

    /**
     * Displays backward animation for table [Card]s
     * @author Bognar, Gabor Bela
     */
    private fun gatherCards() {
        val tableCardCount = when (newState.turnState) {
            TurnState.PREFLOP -> 0
            TurnState.AFTER_FLOP -> 3
            TurnState.AFTER_TURN -> 4
            TurnState.AFTER_RIVER -> 5
        }
        activity?.runOnUiThread {
            binding.tvCurrentHand.visibility = View.GONE
            binding.tvPot.visibility = View.INVISIBLE
            val cards = mutableListOf(binding.playerCard1, binding.playerCard2)
            cards.addAll(tableCards.take(tableCardCount))
            cards.forEachIndexed { index, card ->
                val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.reverse_card_animation)
                anim.startOffset = (index * 120).toLong()
                card.startAnimation(anim)
            }
        }
    }

    private fun displayAvatars() {
        binding.avatarSelf.root.visibility = View.VISIBLE
        binding.avatars.children.forEachIndexed { index, view ->
            if (index < tableRules.playerCount - 1)
                view.visibility = View.VISIBLE
        }
    }

    /**
     * Maps names to avatars
     * @author Bognar, Gabor Bela
     */
    private fun mapAvatars() {
        binding.let {
            if (newState.players.size >= 3) {
                while (newState.players[2].userName != PokerClient.userName)
                    newState.players.add(0, newState.players.removeLast())
            }

            avatarMap[PokerClient.userName] = it.avatarSelf
            val self = newState.players.single { self -> self.userName == PokerClient.userName }
            newState.players.removeIf { player -> player.userName == PokerClient.userName }
            val list = listOf(it.avatar1, it.avatar2, it.avatar3, it.avatar4)
            newState.players.forEachIndexed { index, player ->
                avatarMap[player.userName] = list[index]
            }
            newState.players.add(self)
        }
        tableStart = false
    }

    private fun displayChipCounts() {
        activity?.runOnUiThread {
            binding.tvPot.visibility = View.VISIBLE
            binding.tvPot.text = getString(R.string.pot, newState.pot)
            newState.players.forEach { player ->
                avatarMap[player.userName].let {
                    it?.chipStack?.text = player.chipStack.toString()
                    it?.inPotThisRound?.text = player.inPotThisRound.toString()
                }
            }
        }
    }

    private fun showActionButtonsIfNecessary() {
        activity?.runOnUiThread { 
            if (newState.nextPlayer!=PokerClient.userName)
                binding.actionButtons.visibility = View.GONE
            else {
                binding.actionButtons.visibility = View.VISIBLE
                if (newState.run { maxRaiseThisRound - players.single { it.userName == PokerClient.userName }.inPotThisRound } > 0) {
                    binding.btnCheck.visibility = View.INVISIBLE
                    binding.btnCall.visibility = View.VISIBLE
                    binding.btnCall.text = getString(R.string.call_action, newState.maxRaiseThisRound)
                } else {
                    binding.btnCheck.visibility = View.VISIBLE
                    binding.btnCall.visibility = View.INVISIBLE
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(tableId: Int, rules: TableRules) =
            GamePlayFragment().apply {
                arguments = Bundle().apply {
                    putInt(TABLE_ID_PARAM, tableId)
                    putParcelable(RULE_PARAM, rules)
                }
            }
    }
}