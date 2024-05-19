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
    private val context: Context,
    private val onFollowClicked: ((User?) -> Unit)? = null
) : ListAdapter<User, ProfileUsersAdapter.ProfileUsersViewHolder>(UserDiffCallback) {

    inner class ProfileUsersViewHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(u: User) {
                binding.username.text = u.username
                binding.profilePicture.load(u.profile_picture)
                val imageRequest = ImageRequest.Builder(context)
                    .data(u.profile_picture)
                    .crossfade(true)
                    .target(binding.profilePicture)
                    .build()

                context.imageLoader.enqueue(imageRequest)
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
        holder.itemView.setOnClickListener {
            onFollowClicked?.invoke(slot)
        }
    }



}