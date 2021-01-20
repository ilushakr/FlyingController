package com.example.draggableviews.mainActivityMVVM

import android.content.Context
import com.example.draggableviews.retrofit.Api
import com.example.draggableviews.utils.IPAddress
import retrofit2.Call
import javax.inject.Inject

class Repository @Inject constructor(private val api: Api) {

    fun getVersion(): Call<String> {
        return api.getVersion("http://" + IPAddress.ip + ":" + IPAddress.infoPort + "/info")
    }

}