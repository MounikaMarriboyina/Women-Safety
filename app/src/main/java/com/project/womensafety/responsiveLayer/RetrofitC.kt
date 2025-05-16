package com.project.womensafety.responsiveLayer

import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitC {
private const val URL="https://wizzie.online/WomenSafety/"
   private val client=OkHttpClient.Builder()
      .connectTimeout(60,TimeUnit.SECONDS)
      .writeTimeout(60,TimeUnit.SECONDS)
      .readTimeout(60,TimeUnit.SECONDS)
      .build()

    val api: Api by lazy {
       retrofit2.Retrofit.Builder()
          .addConverterFactory(GsonConverterFactory.create())
          .baseUrl(URL)
          .client(client)
          .build()
          .create(Api::class.java)
    }
}