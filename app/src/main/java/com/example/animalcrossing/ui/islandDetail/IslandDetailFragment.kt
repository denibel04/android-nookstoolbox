package com.example.animalcrossing.ui.islandDetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
        super.onViewCreated(view, savedInstanceState)
        val adapter = IslandDetailAdapter(requireContext(), onSlotClicked = { slotIndex ->
            showVillagerSelectionDialog(slotIndex)
        })
        val rv = binding.slotsRecyclerView
        rv.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    Log.d("DENI", uiState.islandExists.toString())
                    if (uiState.islandExists) {
                        binding.island.visibility = View.VISIBLE
                        binding.addIsland.visibility = View.GONE
                        binding.islandName.text = uiState.name
                        viewModel.villagers.collectLatest { nuevosVillagers ->
                            Log.d("adapter", nuevosVillagers.toString())
                            adapter.submitList(nuevosVillagers)
                        }
                    } else {
                        binding.island.visibility = View.GONE
                        binding.addIsland.visibility = View.VISIBLE
                    }
                }
            }
        }
        binding.addIsland.setOnClickListener {
            showCreateIslandDialog()
        }

        binding.deleteIsland.setOnClickListener {
            viewModel.deleteIsland()
        }

        binding.renameIsland.setOnClickListener {
            showRenameIslandDialog()
        }

    }

    private fun showCreateIslandDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Introduce el nombre de la isla"
        }

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Nueva Isla")
            setView(input)
            setPositiveButton("Crear") { dialog, _ ->
                val islandName = input.text.toString()
                if (islandName.isNotBlank()) {
                    viewModel.createIsland(islandName)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "El nombre no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setNegativeButton("Cancelar", null)
        }.show()
    }


    private fun showRenameIslandDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Nuevo nombre de la isla"
        }
        input.setText(viewModel.uiState.value.name)

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Cambiar Nombre de la Isla")
            setView(input)
            setPositiveButton("Cambiar") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameIsland(newName)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "El nombre no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setNegativeButton("Cancelar", null)
        }.show()
    }


    private fun showVillagerSelectionDialog(slotIndex: Int) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.villager_selection, null)
        val searchBar = dialogView.findViewById<SearchBar>(R.id.search_bar)
        val searchView = dialogView.findViewById<SearchView>(R.id.search_view)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_view_search_results)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Selecciona un Aldeano")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .create()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val adapter = SearchResultAdapter(requireContext()) { villager ->

                    viewModel.uiState.value.islandId?.let {
                        Log.d("AAA", villager.name)
                        Log.d("AAAA", it.toString())
                        viewModel.addVillagerToIsland(
                            villager.name,
                            it
                        )
                    }
                    dialog.dismiss()
                }
                recyclerView.adapter = adapter


                searchView.getEditText().setOnEditorActionListener { v, actionId, event ->
                    searchBar.setText(searchView.getText())
                    val searchQuery = searchBar.text.toString()
                    Log.d("query", searchQuery.toString())
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