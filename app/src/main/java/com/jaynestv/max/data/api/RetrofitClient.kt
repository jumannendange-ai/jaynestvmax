package com.jaynestv.max.data.api

import com.jaynestv.max.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val supabaseAuth: SupabaseAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.SUPABASE_URL + "/")
            .client(okHttp.newBuilder()
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .addHeader("apikey", Constants.SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build()
                    chain.proceed(req)
                }.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseAuthService::class.java)
    }
}
