package com.example.animalcrossing.ui.profile

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Island
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.UserListItemBinding
import com.example.animalcrossing.databinding.VillagerListItemBinding
import com.example.animalcrossing.databinding.VillagerSlotItemBinding
import com.example.animalcrossing.ui.list.VillagerListAdapter

class ProfileUsersAdapter(
    private val context: Context
) : ListAdapter<User, ProfileUsersAdapter.ProfileUsersViewHolder>(UserDiffCallback) {

    inner class ProfileUsersViewHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(u: User) {
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

            binding.followedTextView.text = "Siguiendo: ${u.following?.size ?: 0}"
            binding.followersTextView.text = "Seguidores: ${u.followers?.size ?: 0}"

            binding.followButton.visibility = View.GONE

            }

        }



    private object UserDiffCallback : DiffUtil.ItemCallback<User>() {


        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.uid == newItem.uid


        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
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
    }



}