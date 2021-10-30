package hu.bme.aut.onlab.poker

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import hu.bme.aut.onlab.poker.databinding.FragmentMainBinding
import hu.bme.aut.onlab.poker.network.PokerAPI
import hu.bme.aut.onlab.poker.network.PokerClient
import hu.bme.aut.onlab.poker.network.TableJoinedMessage
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class MainFragment : Fragment(), PokerClient.TableJoinedListener {
    private lateinit var binding: FragmentMainBinding

    var interactionEnabled: Boolean = PokerAPI.isConnected
        set(value) {
            activity?.runOnUiThread {
                binding.btnStartTable.isEnabled = value
                binding.btnJoinTable.isEnabled = value
            }
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MainActivity.backPressDisabled = false
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        PokerClient.listener = this

        _this = this
        setOnClickListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        interactionEnabled = PokerAPI.isConnected
    }

    private fun setOnClickListeners() {
        binding.btnConnect.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_authFragment)
        }

        binding.btnStartTable.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_startTableFragment)
        }

        binding.btnJoinTable.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.alert_join_table)
                .setNegativeButton(R.string.random) { _, _ ->
                    PokerClient.tryJoin(null)
                }
                .setPositiveButton(R.string.specific) { _, _ ->
                    joinSpecificTable()
                }
                .show()
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun joinSpecificTable() {
        var table: Int
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.enter_id)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                table = input.text.toString().toInt()
                if (table >= 100)
                    PokerClient.tryJoin(table)
                else
                    toast("Please enter a valid ID")
            }
            .show()
    }

    override fun tableJoined(joinedMessage: TableJoinedMessage) {
        Log.d("pokerWeb", "Got join message for table ${joinedMessage.tableId}")
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToGamePlayFragment(joinedMessage.tableId, joinedMessage.rules))
    }

    companion object {
        lateinit var _this: MainFragment
        const val POKER_DOMAIN = "d49b-109-74-48-2"
    }
}