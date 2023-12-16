package com.example.animalcrossing.ui.islandDetail

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.VillagerListItemBinding
import com.example.animalcrossing.databinding.VillagerSlotItemBinding
import com.example.animalcrossing.ui.list.VillagerListAdapter

class IslandDetailAdapter(
    private val context: Context,
    private val onSlotClicked: ((Int) -> Unit)? = null
) : ListAdapter<Villager, IslandDetailAdapter.IslandDetailViewHolder>(VillagerDiffCallback) {

    inner class IslandDetailViewHolder(private val binding: VillagerSlotItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
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
            } else {
                binding.slotText.text = context.getString(R.string.add_villager)
                binding.slotIcon.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }


    private object VillagerDiffCallback : DiffUtil.ItemCallback<Villager>() {
        override fun areItemsTheSame(oldItem: Villager, newItem: Villager) =
            oldItem.name == newItem.name

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
    }


}