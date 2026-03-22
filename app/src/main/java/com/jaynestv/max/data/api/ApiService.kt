package com.jaynestv.max.data.api

import com.jaynestv.max.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Channels — zote au kwa source/category ──────────────────
    @GET("channels")
    suspend fun getChannels(
        @Query("source")   source:   String? = null,  // azam|nbc|local|global|all
        @Query("category") category: String? = null,  // sports|tamthiliya|muziki|habari
        @Query("q")        query:    String? = null   // search
    ): Response<ChannelsResponse>

    // ── Categories ───────────────────────────────────────────────
    @GET("categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    // ── Subscription check ───────────────────────────────────────
    @GET("subscription/check")
    suspend fun checkSubscription(
        @Query("email") email: String
    ): Response<SubCheckResponse>

    // ── Submit payment ───────────────────────────────────────────
    @POST("payment/submit")
    suspend fun submitPayment(
        @Body body: Map<String, String>
    ): Response<PaymentResponse>

    // ── Maintenance check ────────────────────────────────────────
    @GET("maintenance")
    suspend fun checkMaintenance(): Response<MaintenanceResponse>
}

// ── Supabase Auth moja kwa moja ──────────────────────────────────
interface SupabaseAuthService {
    @retrofit2.http.POST("auth/v1/token")
    suspend fun login(
        @retrofit2.http.Query("grant_type") grantType: String = "password",
        @retrofit2.http.Body body: Map<String, String>
    ): retrofit2.Response<com.jaynestv.max.data.models.LoginResponse>

    @retrofit2.http.POST("auth/v1/recover")
    suspend fun resetPassword(
        @retrofit2.http.Body body: Map<String, String>
    ): retrofit2.Response<Unit>
}
