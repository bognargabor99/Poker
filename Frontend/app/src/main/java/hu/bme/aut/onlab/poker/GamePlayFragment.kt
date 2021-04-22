package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import hu.bme.aut.onlab.poker.databinding.FragmentGamePlayBinding
import hu.bme.aut.onlab.poker.network.PokerClient

class GamePlayFragment : Fragment(), PokerClient.GamePlayReceiver {
    private lateinit var binding: FragmentGamePlayBinding

    private val args: GamePlayFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        PokerClient.receiver = this
        MainActivity.backPressDisabled = true
        binding = FragmentGamePlayBinding.inflate(LayoutInflater.from(requireContext()))

        binding.tvTableId.text = getString(R.string.game_play_table_id, args.tableId)
        binding.btnBack.setOnClickListener {
            AlertDialog.Builder(requireContext())
                    .setMessage(R.string.leave_table_alert)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        PokerClient.leaveTable(args.tableId)
                        view?.findNavController()?.popBackStack()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        }

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

    override fun receiveMessage(text: String) {
        Log.d("pokerWebSocket", "GamePlayActivity: $text")
    }
}