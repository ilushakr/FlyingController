package com.example.draggableviews.modules

import com.example.draggableviews.mainActivityMVVM.ControllerActivityViewModel
import com.example.draggableviews.mainActivityMVVM.ControllerActivityViewModelFactory
import com.example.draggableviews.mainActivityMVVM.Repository
import com.example.draggableviews.retrofit.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object MVVMModule {

    @Singleton
    @Provides
    fun provideRepository(
        api: Api
    ): Repository {
        return Repository(api)
    }

    @Singleton
    @Provides
    fun provideControllerViewModel(repository: Repository): ControllerActivityViewModel {
        return ControllerActivityViewModel(repository)
    }

    @Singleton
    @Provides
    fun provideFactory(
        controllerActivityViewModel: ControllerActivityViewModel
    ): ControllerActivityViewModelFactory {
        return ControllerActivityViewModelFactory(controllerActivityViewModel)
    }



}