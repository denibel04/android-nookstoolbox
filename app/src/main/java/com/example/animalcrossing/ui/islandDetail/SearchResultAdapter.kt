package com.example.animalcrossing.ui.islandDetail

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
import com.example.animalcrossing.databinding.VillagerListItemSmallBinding

class SearchResultAdapter(
    private val context: Context,
    private val onResultClicked: ((Villager) -> Unit)? = null
) : ListAdapter<Villager, SearchResultAdapter.VillagerViewHolder>(ResultDiffCallback) {
    inner class VillagerViewHolder(private val binding: VillagerListItemSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(v: Villager) {
            binding.villagerName.text = v.name
            binding.villagerImage.load(v.image_url)
            val imageRequest = ImageRequest.Builder(context)
                .data(v.image_url)
                .crossfade(true)
                .target(binding.villagerImage)
                .build()

            context.imageLoader.enqueue(imageRequest)

        }
    }

    private object ResultDiffCallback : DiffUtil.ItemCallback<Villager>() {
        override fun areItemsTheSame(oldItem: Villager, newItem: Villager) =
            oldItem.name == newItem.name

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Villager, newItem: Villager) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VillagerViewHolder =
        VillagerViewHolder(
            VillagerListItemSmallBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: VillagerViewHolder, position: Int) {
        val villager = getItem(position)
        holder.bind(villager)
        holder.itemView.setOnClickListener {
            onResultClicked?.invoke(villager)
        }
    }
}
