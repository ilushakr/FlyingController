package com.example.draggableviews.modules

import android.content.Context
import com.example.draggableviews.utils.IPAddress
import com.example.draggableviews.retrofit.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideApi(): Api {
        return Retrofit.Builder()
            .baseUrl("https://" + IPAddress.ip + ":" + IPAddress.infoPort)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build().create(Api::class.java)
    }


}