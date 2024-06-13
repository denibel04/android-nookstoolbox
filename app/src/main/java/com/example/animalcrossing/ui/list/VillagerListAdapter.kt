package com.example.animalcrossing.ui.list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.VillagerListItemBinding

/**
 * Adapter for displaying a list of villagers in a RecyclerView.
 *
 * @param context The context used for loading images with Coil.
 * @param onVillagerClicked Callback triggered when a villager item is clicked.
 */
class VillagerListAdapter(
    private val context: Context,
    private val onVillagerClicked: ((Villager) -> Unit)? = null
) : ListAdapter<Villager, VillagerListAdapter.VillagerViewHolder>(VillagerDiffCallback) {

    /**
     * ViewHolder class for holding references to the views for each villager item.
     *
     * @param binding The ViewBinding object for the villager list item layout.
     */
    inner class VillagerViewHolder(private val binding: VillagerListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a villager object to the views in the ViewHolder.
         *
         * @param v The Villager object to bind.
         */
        fun bind(v: Villager) {
            binding.villagerName.text = v.name
            binding.villagerImage.load(v.image_url)

            // Load image using Coil ImageLoader
            val imageRequest = ImageRequest.Builder(context)
                .data(v.image_url)
                .crossfade(true)
                .target(binding.villagerImage)
                .build()

            context.imageLoader.enqueue(imageRequest)
        }
    }

    /**
     * DiffCallback for calculating the difference between two lists of villagers.
     */
    private object VillagerDiffCallback : DiffUtil.ItemCallback<Villager>() {
        override fun areItemsTheSame(oldItem: Villager, newItem: Villager) =
            oldItem.name == newItem.name

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Villager, newItem: Villager) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VillagerViewHolder =
        VillagerViewHolder(
            VillagerListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: VillagerViewHolder, position: Int) {
        val villager = getItem(position)
        holder.bind(villager)

        holder.itemView.setOnClickListener {
            onVillagerClicked?.invoke(villager)
        }
    }

}