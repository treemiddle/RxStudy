package com.jay.rxstudyfirst

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiService {
    private const val BASE_URL = "https://yts.mx/api/v2/"
    //        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiInterface = retrofit.create(
        ApiInterface::class.java
    )
}