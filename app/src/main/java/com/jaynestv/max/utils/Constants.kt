package com.jaynestv.max.utils

object Constants {
    const val SUPABASE_URL = "https://dablnrggyfcddmdeiqxi.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_d8mzJ3iulCU7YdlV_lrdQw_32pOzDXc"
    const val BASE_URL     = "https://mq.xo.je/"
    const val FALLBACK_URL = "https://dde.ct.ws/"

    // SharedPreferences keys
    const val PREF_NAME     = "jaynes_prefs"
    const val KEY_TOKEN     = "token"
    const val KEY_EMAIL     = "email"
    const val KEY_NAME      = "name"
    const val KEY_UID       = "uid"
    const val KEY_PLAN      = "plan"
    const val KEY_SUB_END   = "sub_end"
    const val KEY_TRIAL_END = "trial_end"

    // Plans
    const val PLAN_FREE    = "free"
    const val PLAN_PREMIUM = "premium"

    // Trial duration (ms)
    const val TRIAL_MS = 30 * 60 * 1000L  // dakika 30

    // Admin emails
    val ADMINS = setOf("swajayfour@gmail.com", "jaynestvmax@gmail.com")

    // Free channels (hazihitaji subscription)
    val FREE_CHANNELS = setOf(
        "tbc1", "tbc 1", "tb1", "tbc2", "tbc 2",
        "zbc", "azam one", "azamone", "azam 1",
        "wasafi", "wasafi tv", "wasafi channel"
    )
}
