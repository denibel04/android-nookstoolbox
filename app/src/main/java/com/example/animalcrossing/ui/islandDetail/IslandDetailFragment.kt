package com.example.animalcrossing.ui.islandDetail

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentIslandDetailBinding
import com.example.animalcrossing.ui.list.VillagerListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IslandDetailFragment : Fragment() {
    private lateinit var binding: FragmentIslandDetailBinding
    private val viewModel: IslandDetailViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIslandDetailBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.island_title)
        super.onViewCreated(view, savedInstanceState)
        val adapter = IslandDetailAdapter(requireContext(), onSlotClicked = { slotIndex ->
            showVillagerSelectionDialog(slotIndex)
        }, onVillagerDeleteClicked = { villagerName ->
            viewModel.uiState.value.islandId?.let {
                viewModel.deleteVillagerFromIsland(
                    villagerName!!,
                    it
                )}
        })
        val rv = binding.slotsRecyclerView
        rv.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    if (uiState.islandExists) {
                        binding.island.visibility = View.VISIBLE
                        binding.addIsland.visibility = View.GONE
                        binding.shareIsland.visibility = View.VISIBLE
                        binding.islandName.text = uiState.name
                        viewModel.villagers.collectLatest { nuevosVillagers ->
                            adapter.submitList(nuevosVillagers)
                        }
                    } else {
                        binding.island.visibility = View.GONE
                        binding.addIsland.visibility = View.VISIBLE
                        binding.shareIsland.visibility = View.GONE
                    }
                }
            }
        }
        binding.addIsland.setOnClickListener {
            showCreateIslandDialog()
        }

        binding.deleteIsland.setOnClickListener {
            showDeleteIslandConfirmationDialog()
        }

        binding.renameIsland.setOnClickListener {
            showRenameIslandDialog()
        }

        binding.shareIsland.setOnClickListener {
            val shareText = getString(R.string.share_text, viewModel.getIslandName(), viewModel.getVillagersString())

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, null)
            startActivity(shareIntent)
        }


    }

    private fun showDeleteIslandConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_island_title)
            .setMessage(R.string.delete_island_confirmation)
            .setPositiveButton(R.string.accept) { dialog, _ ->
                viewModel.deleteIsland()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showCreateIslandDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.is_name)
        }

        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.is_new))
            setView(input)
            setPositiveButton(getString(R.string.create)) { dialog, _ ->
                val islandName = input.text.toString()
                if (islandName.isNotBlank()) {
                    viewModel.createIsland(islandName)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.show()
    }


    private fun showRenameIslandDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.newname)
        }
        input.setText(viewModel.uiState.value.name)

        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.is_rename))
            setView(input)
            setPositiveButton(getString(R.string.change)) { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameIsland(newName)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setNegativeButton(getString(R.string.cancel), null)
        }.show()
    }


    private fun showVillagerSelectionDialog(slotIndex: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.villager_selection, null)
        val searchBar = dialogView.findViewById<SearchBar>(R.id.search_bar)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_view_search_results)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_villager))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val adapter = SearchResultAdapter(requireContext()) { villager ->
                    viewModel.uiState.value.islandId?.let {
                        viewModel.addVillagerToIsland(
                            villager.name,
                            it,
                            slotIndex
                        )
                    }
                    dialog.dismiss()
                }
                recyclerView.adapter = adapter
                searchVillagers("", adapter)
                searchView.getEditText().setOnEditorActionListener { v, actionId, event ->
                    searchBar.setText(searchView.getText())
                    val searchQuery = searchBar.text.toString()
                    searchVillagers(searchQuery, adapter)
                    searchView.hide()
                    false
                }
            }
        }
        dialog.show()
    }

    private fun searchVillagers(query: String, adapter: SearchResultAdapter) {
        lifecycleScope.launch {
            viewModel.searchVillagers(query).collectLatest { villagers ->
                adapter.submitList(villagers)
            }
        }
    }

}