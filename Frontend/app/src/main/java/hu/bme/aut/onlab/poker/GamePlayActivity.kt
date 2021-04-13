package hu.bme.aut.onlab.poker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import hu.bme.aut.onlab.poker.databinding.ActivityGamePlayBinding
import hu.bme.aut.onlab.poker.network.PokerClient

class GamePlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGamePlayBinding
    private var tableId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePlayBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        tableId = intent.getIntExtra("tableId", 0)
        Log.d("pokerWebGamePlay", "Got extra: $tableId")
        binding.tvTableId.text = getString(R.string.game_play_table_id, tableId.toString())

        binding.background.setOnClickListener {
            AnimationUtils.loadAnimation(this, R.anim.card_animation).also {
                binding.playerCard1.startAnimation(it)
                binding.playerCard2.startAnimation(it)
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setMessage("Do you wish to leave the table, you won't be able to join again.")
                .setPositiveButton(R.string.yes) { _, _ ->
                    PokerClient.leaveTable(tableId)
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }
}