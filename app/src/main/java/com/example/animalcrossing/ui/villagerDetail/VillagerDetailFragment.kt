package com.example.animalcrossing.ui.villagerDetail

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
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
import com.example.animalcrossing.databinding.FragmentVillagerDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VillagerDetailFragment : Fragment() {
    private val args: VillagerDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentVillagerDetailBinding
    private val viewModel: VillagerDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVillagerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetch(args.name)
                viewModel.uiState.collect{
                    (activity as? AppCompatActivity)?.supportActionBar?.title = it.name
                    binding.villagerName.text = it.name
                    binding.villagerImage.load(it.image_url)

                    if (viewModel.uiState.value.gender == "Female") {
                        binding.villagerGender.load(R.drawable.ic_female)
                        binding.villagerGender.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorFemale), PorterDuff.Mode.SRC_IN)
                    } else if (viewModel.uiState.value.gender == "Male") {
                        binding.villagerGender.load(R.drawable.ic_male)
                        binding.villagerGender.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorMale), PorterDuff.Mode.SRC_IN)
                    }
                    binding.villagerSpecies.text = getString(R.string.species, it.species)
                    binding.villagerPersonality.text = getString(R.string.personality, it.personality)
                    binding.villagerBirthday.text = getString(R.string.birthday,  it.birthday_month,  it.birthday_day.toString())
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