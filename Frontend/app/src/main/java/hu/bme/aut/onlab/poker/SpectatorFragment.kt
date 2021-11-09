package hu.bme.aut.onlab.poker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import hu.bme.aut.onlab.poker.databinding.FragmentSpectatorBinding

class SpectatorFragment : Fragment() {
    private lateinit var binding: FragmentSpectatorBinding

    private val args: SpectatorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpectatorBinding.inflate(layoutInflater, container, false)

        return binding.root
    }
}