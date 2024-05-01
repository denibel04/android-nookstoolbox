package com.example.animalcrossing.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.bumptech.glide.Glide
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentProfileBinding
import com.example.animalcrossing.databinding.FragmentVillagerListBinding
import com.example.animalcrossing.ui.list.VillagerListAdapter
import com.example.animalcrossing.ui.list.VillagerListFragmentDirections
import com.example.animalcrossing.ui.list.VillagerListViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    var auth = FirebaseAuth.getInstance()


    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.profilePicture.setOnClickListener {
            val modalBottomSheet = PictureOptionsFragment()
            modalBottomSheet.show(
                requireActivity().supportFragmentManager,
                PictureOptionsFragment.TAG
            )
        }

        lifecycleScope.launch {
            viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                Log.d("PROFILE PICTURE", user.toString())
                if (user.profile_picture.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(user.profile_picture)
                        .into(binding.profilePicture)
                }
            }


        }
    }
}