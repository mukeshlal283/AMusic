package com.example.cicd.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cicd.databinding.MusicItemBinding
import com.example.cicd.model.MusicItem

class MusicAdapter(val onItemClicked: (MusicItem) -> Unit) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    class MusicViewHolder(val binding: MusicItemBinding)
            : RecyclerView.ViewHolder(binding.root)

    val DiffUtil = object: DiffUtil.ItemCallback<MusicItem>() {
        override fun areItemsTheSame(oldItem: MusicItem, newItem: MusicItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MusicItem, newItem: MusicItem): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, DiffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val data = differ.currentList[position]

        holder.binding.apply {
            songName.text = data.name
            artistName.text = data.artist_name
            Glide.with(imageView.context)
                .load(data.image)
                .into(imageView)
        }

        holder.itemView.setOnClickListener {
            onItemClicked(data)
        }

    }

}