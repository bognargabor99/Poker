package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import hu.bme.aut.onlab.poker.databinding.ActivityMainBinding
import hu.bme.aut.onlab.poker.network.PokerAPI
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (!backPressDisabled)
            AlertDialog.Builder(this)
                .setMessage(R.string.exit_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    PokerAPI.disConnect()
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }

    companion object {
        var backPressDisabled = false
    }
}