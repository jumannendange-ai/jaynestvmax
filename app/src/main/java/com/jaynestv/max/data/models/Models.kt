package com.jaynestv.max.data.models

import com.google.gson.annotations.SerializedName

// ── Channel ──────────────────────────────────────────────────────
data class Channel(
    val id: String = "",
    val name: String = "",
    val logo: String = "",
    @SerializedName("stream_url") val streamUrl: String = "",
    @SerializedName("stream_url_mpd") val streamUrlMpd: String = "",
    val category: String = "",
    @SerializedName("is_live") val isLive: Boolean = true,
    @SerializedName("is_free") val isFree: Boolean = false,
    val description: String = ""
)

// ── Category ─────────────────────────────────────────────────────
data class Category(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val description: String = "",
    val link: String = "",
    @SerializedName("channel_count") val channelCount: Int = 0
)

// ── Slider ───────────────────────────────────────────────────────
data class Slider(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("action_type") val actionType: String = "",
    @SerializedName("action_value") val actionValue: String = ""
)

// ── Subscription Plan ─────────────────────────────────────────────
data class Plan(
    val id: String,
    val name: String,
    val nameSwahili: String,
    val price: Int,
    val durationDays: Int,
    val isPopular: Boolean = false
) {
    companion object {
        fun getPlans() = listOf(
            Plan("wiki",    "Wiki",    "Wiki",    500,   7,   false),
            Plan("mwezi",   "Mwezi",   "Mwezi",   1500,  30,  true),
            Plan("miezi3",  "Miezi 3", "Miezi 3", 3500,  90,  false),
            Plan("miezi6",  "Miezi 6", "Miezi 6", 6000,  180, false),
            Plan("mwaka",   "Mwaka",   "Mwaka",   10000, 365, false),
        )
    }
}

// ── Payment Method ────────────────────────────────────────────────
data class PayMethod(
    val id: String,
    val name: String,
    val icon: String,
    val number: String
) {
    companion object {
        fun getMethods() = listOf(
            PayMethod("mpesa",   "M-PESA",    "📱", "0616393956"),
            PayMethod("tigopesa","Tigo Pesa", "📱", "0616393956"),
            PayMethod("airtelmoney","Airtel Money","📱","0616393956"),
            PayMethod("halopesa","Halo Pesa", "📱", "0616393956"),
        )
    }
}

// ── API Response Wrappers ─────────────────────────────────────────
data class ChannelsResponse(
    val success: Boolean,
    val channels: List<Channel> = emptyList()
)

data class CategoriesResponse(
    val success: Boolean,
    val categories: List<Category> = emptyList()
)

data class SlidersResponse(
    val success: Boolean,
    val sliders: List<Slider> = emptyList()
)

data class SubCheckResponse(
    val active: Boolean,
    @SerializedName("end_date") val endDate: String = "",
    val plan: String = ""
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("refresh_token") val refreshToken: String = "",
    val user: SupabaseUser? = null,
    val error: String? = null,
    @SerializedName("error_description") val errorDescription: String? = null
)

data class SupabaseUser(
    val id: String = "",
    val email: String = "",
    @SerializedName("user_metadata") val metadata: UserMetadata? = null
)

data class UserMetadata(
    @SerializedName("full_name") val fullName: String? = null
)
