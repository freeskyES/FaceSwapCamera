package com.es.faceswapcamera.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.es.faceswapcamera.data.MainAdapterItem
import com.es.faceswapcamera.databinding.ActivityMainItemBinding
import com.es.faceswapcamera.listener.MainEventListener

class MainViewHolder constructor(private val binding: ActivityMainItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: MainAdapterItem, position: Int) {
        binding.item = item
        binding.position = position
        binding.executePendingBindings()
    }

    companion object {
        fun from(parent: ViewGroup, eventListener: MainEventListener): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ActivityMainItemBinding.inflate(layoutInflater, parent, false)
            binding.listener = eventListener
            return MainViewHolder(binding)
        }
    }
}
