package com.example.animalcrossing.ui.userDetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentUserDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment to display detailed user information including island details and villagers.
 */
@AndroidEntryPoint
class UserDetailFragment : Fragment() {
    private val args: UserDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentUserDetailBinding
    private val viewModel: UserDetailViewModel by viewModels()

    /**
     * Inflates the fragment layout and initializes view binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configures the UI components and observes ViewModel data changes.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = UserIslandDetailAdapter(requireContext())
        val rv = binding.slotsRecyclerView
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getUser(args.uid)
                viewModel.uiState.collect { uiState ->
                    (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.userIsland, uiState.username)
                    if (uiState.islandExists) {
                        binding.island.visibility = View.VISIBLE
                        binding.noIslandText.visibility = View.GONE
                        binding.islandName.text = uiState.islandName
                        val hemisphereText = when (uiState.hemisphere) {
                            "north" -> getString(R.string.north)
                            "south" -> getString(R.string.south)
                            else -> getString(R.string.no_hemisphere)
                        }
                        binding.islandDescription.text = hemisphereText
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

    /**
     * Cleans up resources when the view is destroyed, including resetting toolbar settings.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(false)
    }

}