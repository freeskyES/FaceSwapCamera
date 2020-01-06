package com.es.faceswapcamera.di

import com.es.faceswapcamera.MainViewModel
import org.koin.dsl.module
import org.koin.android.viewmodel.dsl.viewModel

val presentationModule = module {

    viewModel { MainViewModel() }

}
