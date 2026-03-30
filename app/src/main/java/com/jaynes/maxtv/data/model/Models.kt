package com.jaynes.maxtv.data.model

import com.google.gson.annotations.SerializedName

// ── AUTH ──────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    val user: UserProfile? = null,
    val error: String? = null
)

// ── USER ──────────────────────────────────────────────────────

data class UserProfile(
    val id: String,
    val email: String,
    @SerializedName("full_name") val fullName: String? = null,
    val phone: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val plan: String = "trial",
    @SerializedName("trial_end") val trialEnd: String? = null,
    @SerializedName("sub_end") val subEnd: String? = null,
    @SerializedName("is_banned") val isBanned: Boolean = false,
    val devices: List<String> = emptyList()
)

// ── CHANNEL ───────────────────────────────────────────────────

data class Channel(
    val id: String? = null,
    val name: String,
    val category: String = "OTHER CHANNELS",
    val url: String,
    @SerializedName("logo_url") val logoUrl: String? = null,
    val image: String? = null,
    val key: String? = null,
    val type: String = "hls",        // hls | dash
    @SerializedName("is_premium") val isPremium: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    val description: String? = null
) {
    val logoFinal: String? get() = logoUrl ?: image
    val isLive: Boolean get() = true
}

data class ChannelsResponse(
    val success: Boolean,
    val count: Int = 0,
    val channels: List<Channel> = emptyList(),
    val error: String? = null
)

// ── EPG ───────────────────────────────────────────────────────

data class EpgProgramme(
    val id: String? = null,
    @SerializedName("channel_id") val channelId: String? = null,
    val title: String,
    val description: String? = null,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val category: String? = null,
    @SerializedName("poster_url") val posterUrl: String? = null,
    @SerializedName("is_live") val isLive: Boolean = false
)

data class EpgResponse(
    val success: Boolean,
    val epg: Map<String, List<EpgProgramme>> = emptyMap(),
    val error: String? = null
)

// ── SUBSCRIPTION ──────────────────────────────────────────────

data class Plan(
    val id: String,
    val name: String,
    @SerializedName("price_monthly") val priceMonthly: Int,
    @SerializedName("price_yearly") val priceYearly: Int? = null,
    @SerializedName("max_devices") val maxDevices: Int = 1,
    val features: List<String> = emptyList(),
    @SerializedName("is_active") val isActive: Boolean = true
)

data class PlansResponse(
    val success: Boolean,
    val plans: List<Plan> = emptyList(),
    val error: String? = null
)

data class Subscription(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val plan: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("is_active") val isActive: Boolean = true
)

data class SubscriptionResponse(
    val success: Boolean,
    val subscription: Subscription? = null,
    val error: String? = null
)

// ── PAYMENT ───────────────────────────────────────────────────

data class PaymentRequest(
    val plan: String,
    val method: String,
    val phone: String? = null
)

data class PaymentResponse(
    val success: Boolean,
    val message: String? = null,
    @SerializedName("payment_id") val paymentId: String? = null,
    val error: String? = null
)

// ── APP VERSION ───────────────────────────────────────────────

data class AppVersion(
    val id: String,
    @SerializedName("version_name") val versionName: String,
    @SerializedName("version_code") val versionCode: Int,
    val type: String,                // major | minor | patch
    @SerializedName("force_update") val forceUpdate: Boolean = false,
    @SerializedName("apk_url") val apkUrl: String? = null,
    @SerializedName("apk_sha256") val apkSha256: String? = null,
    @SerializedName("size_mb") val sizeMb: Double? = null,
    val changelog: List<String> = emptyList()
)

data class UpdateResponse(
    val success: Boolean,
    @SerializedName("update_available") val updateAvailable: Boolean = false,
    val version: AppVersion? = null,
    val error: String? = null
)

// ── NOTIFICATION ──────────────────────────────────────────────

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String? = null,
    val type: String = "general",
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("created_at") val createdAt: String
) {
    val isRead: Boolean get() = readAt != null
}

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationItem> = emptyList(),
    val error: String? = null
)

// ── FAVORITE ──────────────────────────────────────────────────

data class FavoriteResponse(
    val success: Boolean,
    val favorites: List<Channel> = emptyList(),
    val error: String? = null
)

// ── SEARCH ────────────────────────────────────────────────────

data class SearchResponse(
    val success: Boolean,
    val results: List<Channel> = emptyList(),
    val count: Int = 0,
    val error: String? = null
)

// ── HEALTH ────────────────────────────────────────────────────

data class HealthResponse(
    val success: Boolean,
    val service: String,
    val version: String,
    val time: String,
    @SerializedName("uptime_s") val uptimeS: Int
)

// ── GENERIC ───────────────────────────────────────────────────

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
