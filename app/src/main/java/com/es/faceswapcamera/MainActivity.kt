package com.es.faceswapcamera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.es.faceswapcamera.databinding.ActivityMainBinding
import com.es.faceswapcamera.listener.MainEventListener
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), MainEventListener {

    private lateinit var viewDataBinding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModel()

    private lateinit var adapter: MainListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewDataBinding.viewmodel = viewModel

        setupListAdapter()
        viewModel.loadItems()
    }

    private fun setupListAdapter() {

        adapter = MainListAdapter(viewModel, this)
        viewDataBinding.mainRv.adapter = adapter
    }

    override fun onClicked(position: Int) {
        viewModel.items.value?.let {
            startActivity(Intent(this, it[position].targetClass))
        }
    }
}
