package com.parade621.locationforegroundservice

import android.app.Application
import timber.log.Timber

class LocationServiceSample : Application() {

    override fun onCreate() {
        super.onCreate()

        // Timber 좋아용 오홓홓
        Timber.plant(Timber.DebugTree())
    }
}