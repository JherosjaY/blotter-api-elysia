package com.example.blottermanagementsystem.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API Configuration - Retrofit Setup
 */
object ApiConfig {
    
    // Localhost API Base URL (for development)
    // Use 10.0.2.2 for Android Emulator (maps to host machine's localhost)
    // Use your computer's IP (e.g., 192.168.1.XXX) for physical device
    private const val BASE_URL = "http://10.0.2.2:3000/"
    
    // Cloud API Base URL (for production - currently disabled)
    // private const val BASE_URL = "https://blotter-api-elysia.onrender.com/"
    
    // Logging Interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // OkHttp Client with timeout and logging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Retrofit Instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API Service
    val apiService: BlotterApiService by lazy {
        retrofit.create(BlotterApiService::class.java)
    }
}
