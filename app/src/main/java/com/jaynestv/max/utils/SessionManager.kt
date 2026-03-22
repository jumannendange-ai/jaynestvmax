package com.jaynestv.max.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    // ── Save login ──────────────────────────────────────────────
    fun saveLogin(token: String, email: String, name: String, uid: String) {
        prefs.edit()
            .putString(Constants.KEY_TOKEN, token)
            .putString(Constants.KEY_EMAIL, email)
            .putString(Constants.KEY_NAME,  name)
            .putString(Constants.KEY_UID,   uid)
            .apply()
    }

    fun saveSubscription(plan: String, endDate: String) {
        prefs.edit()
            .putString(Constants.KEY_PLAN,    plan)
            .putString(Constants.KEY_SUB_END, endDate)
            .apply()
    }

    fun saveTrialEnd(endMs: Long) {
        prefs.edit().putLong(Constants.KEY_TRIAL_END, endMs).apply()
    }

    // ── Getters ─────────────────────────────────────────────────
    fun getToken()   = prefs.getString(Constants.KEY_TOKEN, "") ?: ""
    fun getEmail()   = prefs.getString(Constants.KEY_EMAIL, "") ?: ""
    fun getName()    = prefs.getString(Constants.KEY_NAME,  "") ?: ""
    fun getUid()     = prefs.getString(Constants.KEY_UID,   "") ?: ""
    fun getPlan()    = prefs.getString(Constants.KEY_PLAN,  Constants.PLAN_FREE) ?: Constants.PLAN_FREE
    fun getSubEnd()  = prefs.getString(Constants.KEY_SUB_END, "") ?: ""
    fun getTrialEnd()= prefs.getLong(Constants.KEY_TRIAL_END, 0L)

    // ── Checks ──────────────────────────────────────────────────
    fun isLoggedIn() = getToken().isNotEmpty() && getEmail().isNotEmpty()

    fun isAdmin() = Constants.ADMINS.contains(getEmail().lowercase())

    fun hasPremium(): Boolean {
        if (getPlan() != Constants.PLAN_PREMIUM) return false
        val end = getSubEnd()
        if (end.isEmpty()) return false
        return try {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            fmt.parse(end)?.time ?: 0L > System.currentTimeMillis()
        } catch (e: Exception) { false }
    }

    fun trialActive(): Boolean {
        val end = getTrialEnd()
        return end > 0L && end > System.currentTimeMillis()
    }

    fun trialSecondsLeft(): Long {
        val end = getTrialEnd()
        return if (end > 0L) maxOf(0L, (end - System.currentTimeMillis()) / 1000L) else 0L
    }

    fun hasAnyAccess() = isAdmin() || hasPremium() || trialActive()

    fun isFreeChannel(title: String): Boolean {
        val t = title.lowercase().trim()
        return Constants.FREE_CHANNELS.any { free ->
            t == free || t.startsWith("$free ") || t.startsWith("$free-") || t.contains(free)
        }
    }

    // ── Logout ──────────────────────────────────────────────────
    fun logout() {
        prefs.edit()
            .remove(Constants.KEY_TOKEN)
            .remove(Constants.KEY_EMAIL)
            .remove(Constants.KEY_NAME)
            .remove(Constants.KEY_UID)
            .remove(Constants.KEY_PLAN)
            .remove(Constants.KEY_SUB_END)
            .apply()
    }
}
