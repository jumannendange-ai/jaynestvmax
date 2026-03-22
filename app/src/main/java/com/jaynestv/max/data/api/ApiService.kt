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
