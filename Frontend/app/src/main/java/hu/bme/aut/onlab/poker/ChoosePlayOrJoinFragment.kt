package hu.bme.aut.onlab.poker

import android.graphics.Point
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import hu.bme.aut.onlab.poker.databinding.FragmentChoosePlayOrJoinBinding
import hu.bme.aut.onlab.poker.network.PokerClient
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class ChoosePlayOrJoinFragment : DialogFragment() {
    private lateinit var binding: FragmentChoosePlayOrJoinBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoosePlayOrJoinBinding.inflate(layoutInflater, container, false)

        setOnClickListeners()

        return binding.root
    }

    private fun setOnClickListeners() {
        binding.btnStartTable.setOnClickListener {
            findNavController().navigate(ChoosePlayOrJoinFragmentDirections.actionChoosePlayOrJoinFragmentToStartTableFragment())
            findNavController().popBackStack()
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
            findNavController().popBackStack()
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

    override fun onResume() {
        super.onResume()
        val window: Window? = dialog!!.window
        val size = Point()

        val display: Display? = window?.windowManager?.defaultDisplay
        display?.getRealSize(size)

        window?.setLayout((size.x * 0.65).toInt(), (size.y * 0.75).toInt())
        window?.setGravity(Gravity.CENTER)
    }
}