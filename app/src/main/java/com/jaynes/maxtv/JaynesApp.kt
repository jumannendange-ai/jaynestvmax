package com.jaynes.maxtv

import android.app.Application
import com.jaynes.maxtv.data.prefs.TokenManager
import com.jaynes.maxtv.utils.NetworkUtils
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class JaynesApp : Application() {

    companion object {
        lateinit var instance: JaynesApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initOneSignal()
    }

    private fun initOneSignal() {
        OneSignal.Debug.logLevel = LogLevel.NONE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
    }
}
