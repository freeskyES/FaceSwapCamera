package com.es.faceswapcamera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.es.faceswapcamera.data.MainAdapterItem
import com.es.faceswapcamera.prototype.ProtoTypeActivity
import com.es.faceswapcamera.prototype.ProtoTypeActivity2

class MainViewModel: ViewModel() {

    private val _items = MutableLiveData<List<MainAdapterItem>>().apply { value = emptyList() }
    val items: LiveData<List<MainAdapterItem>> = _items

    private val classes =
        arrayOf<Class<*>>(ProtoTypeActivity::class.java, ProtoTypeActivity2::class.java, MainActivity::class.java)

    private val descriptionIds =
        intArrayOf(R.string.desc_camera_source_activity, R.string.desc_camera_source_activity_2, R.string.desc_camera_source_activity_3)

    fun loadItems() {
        val list = listOf(
            MainAdapterItem("1", classes[0].simpleName, descriptionIds[0], classes[0]),
            MainAdapterItem("2", classes[1].simpleName, descriptionIds[1], classes[1]),
            MainAdapterItem("3", classes[2].simpleName, descriptionIds[2], classes[2]))

        _items.value = list
    }

}