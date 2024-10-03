package com.example.foodmeter.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://yourserver.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())  // JSON 변환을 위한 Gson 추가
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
