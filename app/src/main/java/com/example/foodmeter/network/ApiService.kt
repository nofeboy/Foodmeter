package com.example.foodmeter.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    // 사진 파일을 업로드하는 API 엔드포인트
    @Multipart
    @POST("/upload")
    fun uploadPhoto(
        @Part photo: MultipartBody.Part
    ): Call<ResponseBody>
}
