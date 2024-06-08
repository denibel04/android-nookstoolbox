package com.example.animalcrossing.ui.profile

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.R
import com.example.animalcrossing.data.firebase.UserDetail
import com.example.animalcrossing.data.repository.Island
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.UserListItemBinding
import com.example.animalcrossing.databinding.VillagerListItemBinding
import com.example.animalcrossing.databinding.VillagerSlotItemBinding
import com.example.animalcrossing.ui.list.VillagerListAdapter
import com.google.firebase.auth.FirebaseAuth

class ProfileUsersAdapter(
    private val context: Context,
    private val onFollowClicked: ((UserDetail) -> Unit)? = null
) : ListAdapter<UserDetail, ProfileUsersAdapter.ProfileUsersViewHolder>(UserDiffCallback) {

    inner class ProfileUsersViewHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        fun bind(u: UserDetail) {
            var username = "@"+u.username
            if (username.length > 10) {
                binding.username.textSize = 23F
            }
            if (username.length > 15) {
                binding.username.textSize = 20F
            }
            binding.username.text = username
            if (u.dreamCode != null) {
                binding.dreamCode.text = u.dreamCode
            } else {
                binding.dreamCode.text = "Sin código de ensueño"
            }
            if (u.profile_picture != "") {
                binding.profilePicture.load(u.profile_picture)
                val imageRequest = ImageRequest.Builder(context)
                    .data(u.profile_picture)
                    .crossfade(true)
                    .target(binding.profilePicture)
                    .build()
                context.imageLoader.enqueue(imageRequest)
            } else {
                binding.profilePicture.load(R.drawable.ic_account_circle)
            }

            if (u.followers?.contains(currentUser?.uid) == true) {
                binding.followButton.text = "Dejar de seguir"
            } else {
                binding.followButton.text = "Seguir"
            }

            binding.followedTextView.text = "Siguiendo: ${u.following?.size ?: 0}"
            binding.followersTextView.text = "Seguidores: ${u.followers?.size ?: 0}"

            }

        }



    private object UserDiffCallback : DiffUtil.ItemCallback<UserDetail>() {


        override fun areItemsTheSame(oldItem: UserDetail, newItem: UserDetail) =
            oldItem.uid == newItem.uid


        override fun areContentsTheSame(oldItem: UserDetail, newItem: UserDetail) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileUsersViewHolder =
        ProfileUsersViewHolder(
            UserListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ProfileUsersViewHolder, position: Int) {
        val slot = getItem(position)
        holder.bind(slot)

        holder.binding.followButton.setOnClickListener {
            onFollowClicked?.invoke(slot)
        }
    }



}