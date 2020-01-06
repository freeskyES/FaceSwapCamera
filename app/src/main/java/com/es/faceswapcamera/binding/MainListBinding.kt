package com.es.faceswapcamera.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.es.faceswapcamera.MainListAdapter
import com.es.faceswapcamera.data.MainAdapterItem

@BindingAdapter("main_items")
fun setMainItems(listView: RecyclerView, items: List<MainAdapterItem>?) {
    listView.adapter?.let {
        if (listView.adapter is MainListAdapter) {
            (listView.adapter as MainListAdapter).submitList(items)
        }
    }
}