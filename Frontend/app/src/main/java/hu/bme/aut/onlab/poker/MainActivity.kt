package hu.bme.aut.onlab.poker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import hu.bme.aut.onlab.poker.databinding.ActivityMainBinding
import hu.bme.aut.onlab.poker.network.PokerAPI
import hu.bme.aut.onlab.poker.network.PokerClient

class MainActivity : AppCompatActivity(), PokerClient.TableJoinedListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PokerClient.listener = this
        PokerAPI.connect("a75b52e452d6") {
            runOnUiThread {
                Toast.makeText(this, "Couldn't connect to server", Toast.LENGTH_LONG).show()
            }
        }
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.btnStartTable.setOnClickListener {
            startActivity(Intent(this, TableCreateActivity::class.java))
        }

        binding.btnJoinTable.setOnClickListener {
            PokerClient.tryJoin(null)
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setMessage(R.string.exit_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    PokerAPI.disConnect()
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }

    override fun tableJoined(tableId: Int) {
        Log.d("pokerWeb", "Got join message for table $tableId")
        val intent = Intent(this, GamePlayActivity::class.java)
        intent.putExtra("tableId", tableId)
        startActivity(intent)
    }
}