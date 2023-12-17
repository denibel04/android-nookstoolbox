package com.example.animalcrossing.ui.fishDetail

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentFishDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FishDetailFragment : Fragment() {
    private val args: FishDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentFishDetailBinding
    private val viewModel: FishDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFishDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetch(args.name)
                viewModel.uiState.collect {
                    (activity as? AppCompatActivity)?.supportActionBar?.title = it.name
                    binding.fishName.text = it.name
                    binding.fishImage.load(it.image_url)
                    binding.fishLocation.text = getString(R.string.location, it.location)
                    binding.fishRarity.text = getString(R.string.rarity, it.rarity)
                    binding.fishShadow.text = getString(R.string.shadow, it.shadow_size)

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