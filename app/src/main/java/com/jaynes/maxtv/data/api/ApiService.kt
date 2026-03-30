package com.jaynes.maxtv.data.api

import com.jaynes.maxtv.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── HEALTH ────────────────────────────────────────────────
    @GET("api/health")
    suspend fun health(): Response<HealthResponse>

    // ── AUTH ──────────────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<ApiResponse>

    // ── CHANNELS ──────────────────────────────────────────────
    @GET("api/channels")
    suspend fun getChannels(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null
    ): Response<ChannelsResponse>

    @GET("api/azam")
    suspend fun getAzamChannels(): Response<ChannelsResponse>

    @GET("api/nbc")
    suspend fun getNbcChannels(): Response<ChannelsResponse>

    // ── EPG ───────────────────────────────────────────────────
    @GET("api/epg")
    suspend fun getEpg(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null
    ): Response<EpgResponse>

    // ── STREAM KEY ────────────────────────────────────────────
    @GET("api/key")
    suspend fun getStreamKey(
        @Header("Authorization") token: String,
        @Query("channel") channelName: String
    ): Response<Map<String, Any>>

    // ── SUBSCRIPTION ──────────────────────────────────────────
    @GET("api/plans")
    suspend fun getPlans(): Response<PlansResponse>

    @GET("api/sub/status")
    suspend fun getSubscription(
        @Header("Authorization") token: String
    ): Response<SubscriptionResponse>

    @POST("api/sub/pay")
    suspend fun initiatePayment(
        @Header("Authorization") token: String,
        @Body request: PaymentRequest
    ): Response<PaymentResponse>

    @POST("api/sub/verify/{paymentId}")
    suspend fun verifyPayment(
        @Header("Authorization") token: String,
        @Path("paymentId") paymentId: String
    ): Response<PaymentResponse>

    // ── FAVORITES ─────────────────────────────────────────────
    @GET("api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): Response<FavoriteResponse>

    @POST("api/favorites/{channelId}")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Path("channelId") channelId: String
    ): Response<ApiResponse>

    @DELETE("api/favorites/{channelId}")
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Path("channelId") channelId: String
    ): Response<ApiResponse>

    // ── PROFILE ───────────────────────────────────────────────
    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @PUT("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse>

    // ── NOTIFICATIONS ─────────────────────────────────────────
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<NotificationsResponse>

    @PUT("api/notifications/{id}/read")
    suspend fun markRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: String
    ): Response<ApiResponse>

    // ── SEARCH ────────────────────────────────────────────────
    @GET("api/search")
    suspend fun search(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): Response<SearchResponse>

    // ── UPDATE ────────────────────────────────────────────────
    @GET("api/update/check")
    suspend fun checkUpdate(
        @Query("version_code") versionCode: Int
    ): Response<UpdateResponse>
}
