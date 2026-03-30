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
        OneSignal.initWithContext(this, "10360777-3ada-4145-b83f-00eb0312a53f")
    }
}
