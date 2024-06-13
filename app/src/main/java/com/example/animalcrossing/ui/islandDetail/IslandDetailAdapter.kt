package com.example.animalcrossing.ui.islandDetail

import android.annotation.SuppressLint
import android.content.Context
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
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.VillagerSlotItemBinding

/**
 * Adapter for displaying and managing the list of villagers in the island detail view.
 *
 * @property context The context in which the adapter is used.
 * @property onSlotClicked Callback to handle click events on a slot in the list.
 * @property onVillagerDeleteClicked Callback to handle click events on the delete button for a villager.
 */
class IslandDetailAdapter(
    private val context: Context,
    private val onSlotClicked: ((Int) -> Unit)? = null,
    private val onVillagerDeleteClicked: ((String?) -> Unit)? = null
) : ListAdapter<Villager, IslandDetailAdapter.IslandDetailViewHolder>(VillagerDiffCallback) {

    /**
     * ViewHolder class for holding and binding views for each item in the RecyclerView.
     *
     * @param binding The ViewBinding object for the item layout.
     */
    inner class IslandDetailViewHolder(val binding: VillagerSlotItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds data to the ViewHolder views.
         *
         * @param v The Villager object to bind.
         */
        fun bind(v: Villager?) {
            if (v != null) {
                binding.slotText.text = v.name
                binding.slotIcon.load(v.image_url)
                val imageRequest = ImageRequest.Builder(context)
                    .data(v.image_url)
                    .crossfade(true)
                    .target(binding.slotIcon)
                    .build()

                context.imageLoader.enqueue(imageRequest)
                binding.deleteVillager.visibility = View.VISIBLE
            } else {
                binding.slotText.text = context.getString(R.string.add_villager)
                binding.slotIcon.load(R.drawable.ic_cat)
                binding.deleteVillager.visibility = View.GONE
            }
        }
    }

    /**
     * DiffCallback for calculating the difference between two lists of Villagers.
     */
    private object VillagerDiffCallback : DiffUtil.ItemCallback<Villager>() {
        override fun areItemsTheSame(oldItem: Villager, newItem: Villager) =
            oldItem.name == newItem.name

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Villager, newItem: Villager) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IslandDetailViewHolder =
        IslandDetailViewHolder(
            VillagerSlotItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: IslandDetailViewHolder, position: Int) {
        val slot = getItem(position)
        holder.bind(slot)
        holder.itemView.setOnClickListener {
            onSlotClicked?.invoke(position)
        }
        holder.binding.deleteVillager.setOnClickListener {
            onVillagerDeleteClicked?.invoke(slot.name)
        }
    }
}