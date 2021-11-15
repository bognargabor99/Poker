package hu.bme.aut.onlab.poker

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import hu.bme.aut.onlab.poker.databinding.FragmentStartTableBinding
import hu.bme.aut.onlab.poker.model.TableRules
import hu.bme.aut.onlab.poker.network.PokerClient
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class StartTableFragment : DialogFragment() {
    private lateinit var binding: FragmentStartTableBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MainActivity.backPressDisabled = true
        binding = FragmentStartTableBinding.inflate(layoutInflater, container, false)

        binding.mspSize.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, (2..5).map { it }
        )

        binding.mspInitialBlind.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, (20..200 step 20).map { it }
        )

        binding.mspDoubleBlind.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, (1..10).map { it }
        )

        binding.mspStartStack.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, (1000..20000 step 1000).map { it }
        )

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnCreate.setOnClickListener {
            PokerClient.startTable(
                TableRules(
                    binding.cbOpen.isChecked,
                    if (binding.mspSize.selectedItem != null) binding.mspSize.selectedItem.toString().toInt() else 2,
                    if (binding.mspInitialBlind.selectedItem != null) binding.mspInitialBlind.selectedItem.toString().toInt() else 40,
                    if (binding.mspDoubleBlind.selectedItem != null) binding.mspDoubleBlind.selectedItem.toString().toInt() else 10,
                    if (binding.mspStartStack.selectedItem != null) binding.mspStartStack.selectedItem.toString().toInt() else 5000,
                    binding.cbRoyal.isChecked
            ))
            dismiss()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val size = Point()

        val display: Display? = dialog!!.window?.windowManager?.defaultDisplay
        display?.getSize(size)

        val width: Int = size.x

        dialog!!.window?.setLayout((width * 0.75).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setGravity(Gravity.CENTER)
    }
}