package com.example.animalcrossing.ui.list


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.animalcrossing.databinding.FragmentVillagerListBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.animalcrossing.R
import kotlinx.coroutines.launch

/**
 * Fragment displaying a list of villagers.
 */
@AndroidEntryPoint
class VillagerListFragment : Fragment() {
    private lateinit var binding: FragmentVillagerListBinding
    private val viewModel: VillagerListViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVillagerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.villagers_title)
        super.onViewCreated(view, savedInstanceState)
        val adapter = VillagerListAdapter(requireContext()) { villager ->
            val action =
                VillagerListFragmentDirections.actionVillagerListFragmentToVillagerDetailFragment(
                    villager.name
                )
            findNavController().navigate(action)
        }
        val rv = binding.villagerList
        rv.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.villager)
                }
            }
        }
    }
}