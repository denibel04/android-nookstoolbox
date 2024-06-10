package com.example.animalcrossing.ui.userDetail

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentUserDetailBinding
import com.example.animalcrossing.ui.islandDetail.IslandDetailAdapter
import com.example.animalcrossing.ui.userDetail.UserDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserDetailFragment : Fragment() {
    private val args: UserDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentUserDetailBinding
    private val viewModel: UserDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = UserIslandDetailAdapter(requireContext())
        val rv = binding.slotsRecyclerView
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getUser(args.uid)
                viewModel.uiState.collect { uiState ->
                    Log.d("uiState", uiState.islandExists.toString())
                    (activity as? AppCompatActivity)?.supportActionBar?.title = "Isla de "+uiState.username
                    if (uiState.islandExists) {
                        binding.island.visibility = View.VISIBLE
                        binding.noIslandText.visibility = View.GONE
                        binding.islandName.text = uiState.islandName
                        binding.islandDescription.text = "Hemisferio: "+uiState.hemisphere
                        viewModel.uiState.collectLatest { user ->
                            adapter.submitList(user.villagers)
                        }
                    } else {
                        binding.island.visibility = View.GONE
                        binding.noIslandText.visibility = View.VISIBLE
                    }
                }
            }
        }

        val toolbar = (activity as? AppCompatActivity)?.findViewById<Toolbar>(R.id.toolbar)

        toolbar?.let {
            it.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(true)
        }




    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(false)
    }

}