package com.zerosignal.cognitedge

import android.app.Application
import com.zerosignal.cognitedge.logging.AppFileLogger
import com.zerosignal.cognitedge.logging.CrashHandler
import com.zerosignal.cognitedge.logging.ZSLog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZeroSignalApp : Application() {

    companion object {
        private const val TAG = "ZeroSignalApp"
    }

    override fun onCreate() {
        super.onCreate()

        // 1. Initialise the file logger (must be first)
        AppFileLogger.init(this)

        // 2. Install the global crash handler
        CrashHandler.install()

        ZSLog.i(TAG, "Application created — logging and crash handler active")
    }
}
