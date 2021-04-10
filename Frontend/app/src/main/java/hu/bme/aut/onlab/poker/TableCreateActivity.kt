package hu.bme.aut.onlab.poker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.core.content.ContentProviderCompat.requireContext
import hu.bme.aut.onlab.poker.databinding.ActivityTableCreateBinding
import hu.bme.aut.onlab.poker.model.TableRules
import hu.bme.aut.onlab.poker.network.PokerClient

class TableCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTableCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTableCreateBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.mspSize.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, (2..5).map { it }
        )

        binding.mspInitialBlind.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, (20..200 step 20).map { it }
        )

        binding.mspDoubleBlind.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, (1..10).map { it }
        )

        binding.mspStartStack.adapter = ArrayAdapter(
            applicationContext, android.R.layout.simple_spinner_dropdown_item, (1000..20000 step 1000).map { it }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_table_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemCreateTable -> {
                PokerClient.startTable(TableRules(
                    binding.cbOpen.isChecked,
                    binding.mspSize.selectedItem.toString().toInt(),
                    binding.mspInitialBlind.selectedItem.toString().toInt(),
                    binding.mspDoubleBlind.selectedItem.toString().toInt(),
                    binding.mspStartStack.selectedItem.toString().toInt()
                ))
            }
        }
        onBackPressed()
        return true
    }
}