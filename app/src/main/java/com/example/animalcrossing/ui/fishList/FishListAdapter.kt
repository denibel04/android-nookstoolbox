package com.example.animalcrossing.ui.fishList

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.example.animalcrossing.data.repository.Fish
import com.example.animalcrossing.data.repository.Villager
import com.example.animalcrossing.databinding.FishListItemBinding

class FishListAdapter(private val context: Context, private val onFishClicked: ((Fish) -> Unit)? = null):
    ListAdapter<Fish, FishListAdapter.FishViewHolder>(FishDiffCallback) {
    inner class FishViewHolder(private val binding: FishListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(f: Fish) {
            binding.fishName.text = f.name
            binding.fishImage.load(f.image_url)
            val imageRequest = ImageRequest.Builder(context)
                .data(f.image_url)
                .crossfade(true)
                .target(binding.fishImage)
                .build()

            context.imageLoader.enqueue(imageRequest)

        }
    }

    private object FishDiffCallback: DiffUtil.ItemCallback<Fish>(){
        override fun areItemsTheSame(oldItem: Fish, newItem: Fish) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: Fish, newItem: Fish) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FishViewHolder = FishViewHolder(
        FishListItemBinding.inflate(
        LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: FishViewHolder, position: Int) {
        val fish = getItem(position)
        holder.bind(fish)
        holder.itemView.setOnClickListener{
            onFishClicked?.invoke(fish)
        }
    }
}