package hu.bme.aut.onlab.poker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.onlab.poker.calculation.OddsCalculator
import hu.bme.aut.onlab.poker.calculation.WinningChance
import hu.bme.aut.onlab.poker.databinding.AvatarBinding
import hu.bme.aut.onlab.poker.databinding.FragmentSpectatorBinding
import hu.bme.aut.onlab.poker.model.*
import hu.bme.aut.onlab.poker.network.ActionMessage
import hu.bme.aut.onlab.poker.network.PokerClient
import hu.bme.aut.onlab.poker.network.SpectatorGameStateMessage
import hu.bme.aut.onlab.poker.network.TurnEndMessage
import hu.bme.aut.onlab.poker.view.PokerCardView
import kotlinx.coroutines.DelicateCoroutinesApi

private const val TABLE_ID_PARAM = "tableId"
private const val RULE_PARAM = "rules"

@DelicateCoroutinesApi
class SpectatorFragment : Fragment(), PokerClient.SpectatorGamePlayReceiver {
    private lateinit var binding: FragmentSpectatorBinding

    private lateinit var tableCards: List<PokerCardView>
    private var firstMessage = true
    private var newTurn = true
    private lateinit var oldState: SpectatorGameStateMessage
    private lateinit var newState: SpectatorGameStateMessage
    private lateinit var lastTurnResults: TurnEndMessage
    private val avatarMap = mutableMapOf<String, AvatarBinding>()
    private val cardAnimationMap = mutableMapOf<String, CardAnimation>()
    private val avatars = mutableListOf<AvatarBinding>()
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
        binding = FragmentSpectatorBinding.inflate(layoutInflater, container, false)
        binding.tvTableId.text = getString(R.string.game_play_table_id, tableId)
        tableCards = listOf(binding.tableCard1, binding.tableCard2, binding.tableCard3, binding.tableCard4, binding.tableCard5)

        setOnClickListeners()

        return binding.root
    }

    private fun setOnClickListeners() {
        binding.btnLastTurn.setOnClickListener {
            showTurnResults()
        }
    }

    private fun setAvatarTheme(action: ActionMessage) {
        val resourceId = when (action.action.type) {
            ActionType.CHECK -> R.drawable.avatar_background_check
            ActionType.CALL -> R.drawable.avatar_background_call
            ActionType.RAISE -> R.drawable.avatar_background_raise
            else -> R.drawable.avatar_background_default
        }

        activity?.runOnUiThread {
            avatarMap[action.name]?.let {
                it.root.setBackgroundResource(resourceId)
                it.tvLastAction.visibility = View.VISIBLE
                it.tvLastAction.text = action.action.type.name
            }
        }
    }

    private fun setDefaultAvatarThemes() {
        activity?.runOnUiThread {
            avatarMap.values.forEach {
                it.tvLastAction.visibility = View.GONE
                it.root.setBackgroundResource(R.drawable.avatar_background_default)
            }
        }
    }

    private fun putCardsOnTable() {
        val range = when (newState.turnState) {
            TurnState.AFTER_FLOP -> 0..2
            TurnState.AFTER_TURN -> 3..3
            TurnState.AFTER_RIVER -> (if (firstMessage || !this::oldState.isInitialized) 0 else oldState.tableCards.size)..4
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
    }

    private fun handCards() {
        activity?.runOnUiThread {
            newState.players.forEach { player ->
                cardAnimationMap[player.playerDto.userName]?.let {
                    it.card1.alpha = 1f
                    it.card1.value = player.inHandCards.first().value
                    it.card1.symbol = player.inHandCards.first().suit.ordinal
                    it.card2.alpha = 1f
                    it.card2.value = player.inHandCards.last().value
                    it.card2.symbol = player.inHandCards.last().suit.ordinal
                }
            }
            animatePlayerCards()
        }
    }

    private fun animatePlayerCards() {
        newState.players.forEach { player ->
            cardAnimationMap[player.playerDto.userName]?.let {
                AnimationUtils.loadAnimation(requireContext(), it.animId).also { cardAnimation ->
                    it.card1.isVisible = true
                    it.card2.isVisible = true
                    it.card1.startAnimation(cardAnimation)
                    it.card2.startAnimation(cardAnimation)
                }
            }
        }
    }

    private fun displayAvatars() {
        requireActivity().runOnUiThread {
            avatars.forEachIndexed { i, avatar ->
                if (i < tableRules.playerCount)
                    avatar.root.visibility = View.VISIBLE
            }
        }
    }

    private fun mapAvatars() {
        avatars.add(binding.avatar1)
        avatars.add(binding.avatar2)
        avatars.add(binding.avatar3)
        avatars.add(binding.avatar4)
        avatars.add(binding.avatar5)

        val cards = listOf(binding.player1Card1, binding.player1Card2,
            binding.player2Card1, binding.player2Card2,
            binding.player3Card1, binding.player3Card2,
            binding.player4Card1, binding.player4Card2,
            binding.player5Card1, binding.player5Card2)

        val animations = listOf(R.anim.card_animation_player1,
            R.anim.card_animation_player2,
            R.anim.card_animation,
            R.anim.card_animation_player4,
            R.anim.card_animation_player5)

        val reverseAnimations = listOf(R.anim.reverse_card_animation_player1,
            R.anim.reverse_card_animation_player2,
            R.anim.reverse_card_animation,
            R.anim.reverse_card_animation_player4,
            R.anim.reverse_card_animation_player5)

        val tvChances = listOf(binding.tvChancePlayer1,
            binding.tvChancePlayer2,
            binding.tvChancePlayer3,
            binding.tvChancePlayer4,
            binding.tvChancePlayer5)

        newState.players.forEachIndexed { i, player ->
            avatarMap[player.playerDto.userName] = avatars[i]
            cardAnimationMap[player.playerDto.userName] = CardAnimation(
                card1 = cards[i*2],
                card2 = cards[i*2+1],
                animId = animations[i],
                reverseAnimId = reverseAnimations[i],
                tvChance = tvChances[i]
            )
        }
    }

    private fun displayChipCounts() {
        activity?.runOnUiThread {
            binding.tvPot.visibility = View.VISIBLE
            binding.tvPot.text = getString(R.string.pot, newState.pot)
            newState.players.forEach { player ->
                avatarMap[player.playerDto.userName]?.let {
                    it.chipStack.text = player.playerDto.chipStack.toString()
                    it.inPotThisRound.text = player.playerDto.inPotThisRound.toString()
                }
            }
        }
    }

    private fun disableTableCards() {
        tableCards.forEach { it.visibility = View.INVISIBLE }
    }

    override fun onNewGameState(stateMessage: SpectatorGameStateMessage) {
        if (stateMessage.tableId != tableId)
            return
        if (this::newState.isInitialized)
            oldState = newState
        newState = stateMessage
        if (firstMessage) {
            mapAvatars()
            displayAvatars()
        }
        if (newTurn) {
            handCards()
            disableTableCards()
            newTurn = false
        }
        if (newState.lastAction != null) {
            setAvatarTheme(newState.lastAction!!)
            if (newState.lastAction!!.action.type == ActionType.FOLD) {
                foldCard(newState.lastAction!!.name)
                displayOdds()
            }
        } else {
            setDefaultAvatarThemes()
            displayOdds()
            if (tableCards.count { card -> card.isVisible } < newState.tableCards.size)
                putCardsOnTable()
        }
        displayChipCounts()
        firstMessage = false
    }

    private fun displayOdds() {
        val oddsMap: Map<String, WinningChance> =
            OddsCalculator.calculateOdds(newState.tableCards.toMutableList(), newState.players)

        activity?.runOnUiThread {
            newState.players.forEach { player ->
                val name = player.playerDto.userName
                cardAnimationMap[name]?.tvChance?.text = if (player.playerDto.isInTurn) oddsMap[name].toString() else ""
            }
        }
    }

    private fun foldCard(name: String) {
        if (!cardAnimationMap.keys.contains(name))
            return
        val anim = AnimationUtils.loadAnimation(requireContext(), cardAnimationMap[name]!!.reverseAnimId)
        activity?.runOnUiThread {
            cardAnimationMap[name]!!.card1.startAnimation(anim)
            cardAnimationMap[name]!!.card2.startAnimation(anim)
            cardAnimationMap[name]!!.tvChance.text = ""
        }
    }

    override fun onTurnEnd(turnEndMessage: TurnEndMessage) {
        if (turnEndMessage.tableId != tableId)
            return
        lastTurnResults = turnEndMessage
        activity?.runOnUiThread { binding.btnLastTurn.visibility = View.VISIBLE }
        gatherCards()
        newTurn = true
    }

    private fun gatherCards() {
        val tableCardCount = when (newState.turnState) {
            TurnState.PREFLOP -> 0
            TurnState.AFTER_FLOP -> 3
            TurnState.AFTER_TURN -> 4
            TurnState.AFTER_RIVER -> 5
        }

        activity?.runOnUiThread {
            binding.tvPot.visibility = View.INVISIBLE
            newState.players.filter { player -> player.playerDto.isInTurn }.forEachIndexed { i, player ->
                val anim = AnimationUtils.loadAnimation(requireContext(), cardAnimationMap[player.playerDto.userName]!!.reverseAnimId)
                anim.startOffset = i * 200L
                cardAnimationMap[player.playerDto.userName]!!.card1.startAnimation(anim)
                cardAnimationMap[player.playerDto.userName]!!.card2.startAnimation(anim)
                cardAnimationMap[player.playerDto.userName]!!.tvChance.text = ""
            }
            tableCards.take(tableCardCount).forEachIndexed { i, view ->
                val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.reverse_card_animation)
                anim.startOffset = i * 120L
                view.startAnimation(anim)
            }
        }
    }

    private fun showTurnResults() {
        if (!(requireView().parent!! as ViewGroup).isVisible)
            return
        val resultsFragment = ResultsFragment.newInstance(lastTurnResults)
        resultsFragment.show(requireActivity().supportFragmentManager, "result")
    }

    override fun onPlayerDisconnection(tableId: Int, name: String) {
        if (tableId != this.tableId)
            return
        activity?.runOnUiThread {
            avatarMap[name]?.tvLastAction?.visibility = View.VISIBLE
            avatarMap[name]?.tvLastAction?.text = getString(R.string.out)
            avatarMap.remove(name)
            cardAnimationMap[name]?.tvChance?.isVisible = false
            foldCard(name)
            cardAnimationMap.remove(name)
            Snackbar.make(requireView(), getString(R.string.alert_disconnection, name, tableId), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    override fun onTableWin(tableId: Int, name: String) { }

    companion object {
        @JvmStatic
        fun newInstance(tableId: Int, rules: TableRules) =
            SpectatorFragment().apply {
                arguments = Bundle().apply {
                    putInt(TABLE_ID_PARAM, tableId)
                    putParcelable(RULE_PARAM, rules)
                }
            }
    }
}