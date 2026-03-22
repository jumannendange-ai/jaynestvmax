package com.jaynestv.max.data.api

import com.jaynestv.max.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("channels.php")
    suspend fun getChannels(
        @Query("category") category: String? = null
    ): Response<ChannelsResponse>

    @GET("categories.php")
    suspend fun getCategories(): Response<CategoriesResponse>

    @GET("auth.php")
    suspend fun checkSubscription(
        @Query("action") action: String = "check_sub",
        @Query("email") email: String
    ): Response<SubCheckResponse>
}

// Supabase Auth — separate interface
interface SupabaseAuthService {

    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: Map<String, String>
    ): Response<LoginResponse>

    @POST("auth/v1/recover")
    suspend fun resetPassword(
        @Body body: Map<String, String>
    ): Response<Unit>
}
