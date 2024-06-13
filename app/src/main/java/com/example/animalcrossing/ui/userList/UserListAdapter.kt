package com.example.animalcrossing.ui.userList

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
 * Adapter for displaying a list of users with various details.
 *
 * @property context Context of the adapter.
 * @property onFollowClicked Callback triggered when the follow button is clicked.
 * @property onUserClicked Callback triggered when a user item is clicked.
 */
class UserListAdapter(
    private val context: Context,
    private val onFollowClicked: ((UserDetail) -> Unit)? = null,
    private val onUserClicked: ((String) -> Unit)? = null
) : ListAdapter<UserDetail, UserListAdapter.UserListViewHolder>(UserDiffCallback) {

    /**
     * ViewHolder for each user list item.
     *
     * @property binding View binding for the ViewHolder.
     */
    inner class UserListViewHolder(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        /**
         * Binds user data to the ViewHolder.
         *
         * @param u UserDetail object to bind.
         */
        fun bind(u: UserDetail) {
            val username = "@"+u.username
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
                binding.dreamCode.text = context.getString(R.string.no_dream_code)
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
            } else {
                binding.followButton.text = context.getString(R.string.follow)
            }

            binding.followedTextView.text = context.getString(R.string.following_count, u.following?.size ?: 0)
            binding.followersTextView.text = context.getString(R.string.followers_count, u.followers?.size ?: 0)


            if (u.role == "banned") {
                binding.root.setCardBackgroundColor(Color.parseColor("#FFE0E0"))
                binding.bannedText.text = context.getString(R.string.banned)
            }

            }
        }



    /**
     * DiffUtil callback for comparing UserDetail items.
     */
    private object UserDiffCallback : DiffUtil.ItemCallback<UserDetail>() {
        override fun areItemsTheSame(oldItem: UserDetail, newItem: UserDetail) =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: UserDetail, newItem: UserDetail) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder =
        UserListViewHolder(
            UserListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
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