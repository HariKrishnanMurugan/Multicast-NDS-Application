package com.example.multicastndsapplication

import android.app.Application
import android.content.IntentFilter
import android.location.LocationManager
import dagger.hilt.android.HiltAndroidApp

/**
 * The base application class
 */
@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initInstance()
    }

    /**
     * To initial the instances which are using for whole project
     */
    private fun initInstance() {
        instance = this
        registerGPSListener()
    }

    companion object {
        private val TAG = this::class.java.simpleName
        private lateinit var instance: BaseApplication
        var isGpsEnabled: Boolean = false

        /**
         * To get the application instance
         *
         * @return The application instance
         */
        fun getInstance(): BaseApplication = instance
    }

    /**
     * To register gps state broadcast receiver
     */
    private fun registerGPSListener() {
        val locationIntentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(LocationStateReceiver(), locationIntentFilter)
    }
}