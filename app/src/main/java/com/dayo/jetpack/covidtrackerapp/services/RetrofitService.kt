package com.dayo.jetpack.covidtrackerapp.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://covidtracking.com/api/v1/"
object RetrofitService {
    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
    val retrofit : CovidService by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CovidService::class.java)
    }
}