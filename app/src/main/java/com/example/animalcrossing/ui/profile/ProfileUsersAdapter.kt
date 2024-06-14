package com.example.animalcrossing.ui.profile

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.R
import com.example.animalcrossing.data.firebase.UserDetail
import com.example.animalcrossing.databinding.UserListItemBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Adapter for displaying a list of user profiles in a RecyclerView.
 *
 * @param context The context in which the adapter is used.
 * @param onFollowClicked Callback to handle follow/unfollow button clicks.
 * @param onUserClicked Callback to handle clicks on user profiles.
 */
class ProfileUsersAdapter(
    private val context: Context,
    private val onFollowClicked: ((UserDetail) -> Unit)? = null,
    private val onUserClicked: ((String) -> Unit)? = null
) : ListAdapter<UserDetail, ProfileUsersAdapter.ProfileUsersViewHolder>(UserDiffCallback) {

    /**
     * ViewHolder class for holding each user profile item's view.
     *
     * @param binding The view binding for the user list item.
     */
    inner class ProfileUsersViewHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        /**
         * Binds user details to the ViewHolder's views.
         *
         * @param u The UserDetail object representing the user's details.
         */
        fun bind(u: UserDetail) {
            val username = "@"+u.username
            if (username.length >= 10) {
                binding.username.textSize = 18F
            }
            if (username.length > 15) {
                binding.username.textSize = 16F
            } else {
                binding.username.textSize = 25F
            }
            binding.username.text = username

            if (u.dreamCode != null) {
                binding.dreamCode.text = u.dreamCode
                binding.dreamCode.textSize = 15f
            } else {
                binding.dreamCode.text = context.getString(R.string.no_dream_code)
                binding.dreamCode.textSize = 13f
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
                binding.followButton.text = context.getString(R.string.unfollow)
                binding.followButton.textSize = 10f
            } else {
                binding.followButton.text = context.getString(R.string.follow)
                binding.followButton.textSize = 15f
            }

            binding.followedTextView.text = context.getString(R.string.following_count, u.following?.size ?: 0)
            binding.followersTextView.text = context.getString(R.string.followers_count, u.followers?.size ?: 0)

            if (u.role == "banned") {
                binding.root.setCardBackgroundColor(Color.parseColor("#FFE0E0"))
                binding.bannedText.text = context.getString(R.string.banned)
            } else {
                binding.root.setCardBackgroundColor(Color.WHITE)
                binding.bannedText.text = ""
            }

            }

        }


    /**
     * DiffCallback for calculating the difference between two lists of UserDetail objects.
     */
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

        holder.itemView.setOnClickListener {
            onUserClicked?.invoke(slot.uid)
        }
    }



}