package com.example.draggableviews.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface Api {

    @GET()
    fun getVersion(@Url url: String): Call<String>

}