package com.parade621.locationforegroundservice

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import timber.log.Timber

class LocationServiceSample : Application() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        private set
    }

    override fun onCreate() {
        super.onCreate()

        // Timber 좋아용 오홓홓
        Timber.plant(Timber.DebugTree())

        context = applicationContext
    }
}