package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import hu.bme.aut.onlab.poker.databinding.FragmentGamePlayBinding
import hu.bme.aut.onlab.poker.network.PokerClient

class GamePlayFragment : Fragment(), PokerClient.GamePlayReceiver {
    private lateinit var binding: FragmentGamePlayBinding
    private var tableId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        PokerClient.receiver = this
        binding = FragmentGamePlayBinding.inflate(LayoutInflater.from(requireContext()))

        arguments?.let {
            tableId = it.getInt("tableId")
        }

        binding.tvTableId.text = getString(R.string.game_play_table_id, tableId.toString())

        binding.background.setOnClickListener {
            AnimationUtils.loadAnimation(requireContext(), R.anim.card_animation).also {
                binding.playerCard1.isVisible = true
                binding.playerCard2.isVisible = true
                binding.playerCard1.startAnimation(it)
                binding.playerCard2.startAnimation(it)
            }
        }

        return binding.root
    }

    /*override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(R.string.leave_table_alert)
            .setPositiveButton(R.string.yes) { _, _ ->
                PokerClient.leaveTable(tableId)
                super.onBackPressed()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }*/

    override fun receiveMessage(text: String) {
        Log.d("pokerWebSocket", "GamePlayActivity: $text")
    }
}