package hu.bme.aut.onlab.poker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import hu.bme.aut.onlab.poker.databinding.FragmentCreateBinding
import hu.bme.aut.onlab.poker.model.TableRules

class TableCreateFragment : DialogFragment() {
    private lateinit var binding: FragmentCreateBinding
    private lateinit var listener: TableCreationListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = if (targetFragment != null) {
                targetFragment as TableCreationListener
            } else {
                activity as TableCreationListener
            }
        } catch (e: ClassCastException) {
            throw RuntimeException(e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateBinding.inflate(inflater, container, false)
        dialog?.setTitle(R.string.fragment_create_title)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spSize.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, (2..5).map { it }
        )

        binding.spInitialBlind.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, (20..200 step 20).map { it }
        )

        binding.spDoubleBlind.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, (1..10).map { it }
        )

        binding.spStartingStack.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, (1000..20000 step 1000).map { it }
        )

        binding.btnCreateTable.setOnClickListener {
            listener.onTableCreated(
                TableRules(
                    binding.cbOpen.isChecked,
                    binding.spSize.selectedItem.toString().toInt(),
                    binding.spInitialBlind.selectedItem.toString().toInt(),
                    binding.spDoubleBlind.selectedItem.toString().toInt(),
                    binding.spStartingStack.selectedItem.toString().toInt()
                )
            )
            dismiss()
        }

        binding.btnCancelCreateTable.setOnClickListener {
            dismiss()
        }
    }

    interface TableCreationListener {
        fun onTableCreated(rules: TableRules)
    }
}