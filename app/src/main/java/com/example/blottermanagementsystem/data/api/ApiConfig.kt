package com.example.blottermanagementsystem.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    // Change this to your deployed API URL when ready
    private const val BASE_URL = "http://10.0.2.2:3000/" // For Android Emulator
    // private const val BASE_URL = "http://localhost:3000/" // For physical device on same network
    // private const val BASE_URL = "https://your-api.onrender.com/" // For production
    
    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: BlotterApiService by lazy {
        retrofit.create(BlotterApiService::class.java)
    }
}
