package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.onlab.poker.databinding.ActivityMainBinding
import hu.bme.aut.onlab.poker.network.PokerAPI

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        PokerAPI.connect("6319c58828b6") {
            runOnUiThread {
                toast("Couldn't connect to server")
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
}