package com.example.animalcrossing.ui.islandDetail

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentIslandDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IslandDetailFragment : Fragment() {
    private lateinit var binding: FragmentIslandDetailBinding
    private val viewModel:IslandDetailViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIslandDetailBinding.inflate(inflater,
            container,
            false)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    Log.d("DENI", uiState.islandExists.toString())
                    if (uiState.islandExists) {
                        // Si la isla existe, muestra la información y oculta el botón
                        binding.island.visibility = View.VISIBLE
                        binding.addIsland.visibility = View.GONE
                        binding.islandName.text = uiState.name
                    } else {
                        // Si no existe isla, muestra el botón y oculta la información
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
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
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
        input.setText(viewModel.uiState.value.name) // Poner el nombre actual como predeterminado

        AlertDialog.Builder(requireContext()).apply {
            setTitle("Cambiar Nombre de la Isla")
            setView(input)
            setPositiveButton("Cambiar") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameIsland(newName)
                } else {
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancelar", null)
        }.show()
    }



}