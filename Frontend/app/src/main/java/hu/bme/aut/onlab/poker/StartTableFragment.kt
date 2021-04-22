package hu.bme.aut.onlab.poker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.findNavController
import hu.bme.aut.onlab.poker.databinding.FragmentStartTableBinding
import hu.bme.aut.onlab.poker.model.TableRules
import hu.bme.aut.onlab.poker.network.PokerClient

class StartTableFragment : Fragment() {
    private lateinit var binding: FragmentStartTableBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MainActivity.backPressDisabled = true
        binding = FragmentStartTableBinding.inflate(LayoutInflater.from(requireContext()))

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
            view?.findNavController()?.popBackStack()
        }

        binding.btnCreate.setOnClickListener {
            PokerClient.startTable(
                TableRules(
                binding.cbOpen.isChecked,
                binding.mspSize.selectedItem.toString().toInt(),
                binding.mspInitialBlind.selectedItem.toString().toInt(),
                binding.mspDoubleBlind.selectedItem.toString().toInt(),
                binding.mspStartStack.selectedItem.toString().toInt()
            ))
            view?.findNavController()?.popBackStack()
        }

        return binding.root
    }
}