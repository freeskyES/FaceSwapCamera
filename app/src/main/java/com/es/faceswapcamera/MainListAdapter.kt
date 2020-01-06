package com.es.faceswapcamera

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.es.faceswapcamera.data.MainAdapterItem
import com.es.faceswapcamera.holder.MainViewHolder
import com.es.faceswapcamera.listener.MainEventListener

class MainListAdapter(val viewModel: MainViewModel, private val eventListener: MainEventListener) : ListAdapter<MainAdapterItem, RecyclerView.ViewHolder>(AdapterDataDiffCallback()) {



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (holder is MainViewHolder) {
            holder.bind(item, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MainViewHolder.from(parent, eventListener)
    }

    class AdapterDataDiffCallback : DiffUtil.ItemCallback<MainAdapterItem>() {
        override fun areItemsTheSame(oldItem: MainAdapterItem, newItem: MainAdapterItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MainAdapterItem, newItem: MainAdapterItem): Boolean {
            return oldItem == newItem
        }
    }
}


