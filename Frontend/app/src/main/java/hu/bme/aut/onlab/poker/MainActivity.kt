package hu.bme.aut.onlab.poker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import hu.bme.aut.onlab.poker.databinding.ActivityMainBinding
import hu.bme.aut.onlab.poker.model.TableRules
import hu.bme.aut.onlab.poker.network.PokerAPI
import hu.bme.aut.onlab.poker.network.PokerClient

class MainActivity : AppCompatActivity(), TableCreateFragment.TableCreationListener{
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.btnConnect.setOnClickListener {
            PokerAPI.connect("14d8c61b8a39")
        }
        binding.btnStartTable.setOnClickListener {
            val tableCreateFragment = TableCreateFragment()
            tableCreateFragment.show(supportFragmentManager, "TAG")
        }
    }

    override fun onTableCreated(rules: TableRules) {
        PokerClient.startTable(rules)
    }
}