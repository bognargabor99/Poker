package hu.bme.aut.onlab.poker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import hu.bme.aut.onlab.poker.databinding.ActivityMainBinding
import hu.bme.aut.onlab.poker.network.PokerAPI

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        PokerAPI.connect() {
            runOnUiThread {
                binding.text.text = "It works!"
            }
        }
    }
}