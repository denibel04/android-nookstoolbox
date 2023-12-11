package com.example.animalcrossing.ui.fishList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentFishListBinding
import com.example.animalcrossing.databinding.FragmentVillagerListBinding
import com.example.animalcrossing.ui.list.VillagerListAdapter
import com.example.animalcrossing.ui.list.VillagerListFragmentDirections
import com.example.animalcrossing.ui.list.VillagerListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FishListFragment : Fragment() {
    private lateinit var binding: FragmentFishListBinding
    private val viewModel: FishListViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFishListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = FishListAdapter(requireContext()) { fish ->
            val action = FishListFragmentDirections.actionFishListFragmentToFishDetailFragment(fish.name)
            findNavController().navigate(action)
        }
        val rv = binding.fishList
        rv.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.fish)
                }
            }
        }
    }  }

