package com.parade621.locationforegroundservice.service


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.parade621.locationforegroundservice.MainActivity
import com.parade621.locationforegroundservice.R
import kotlinx.coroutines.launch

class LocationService : LifecycleService() {

    private val localBinder = LocalBinder()
    private var isServiceRunning = false
        private set

    private val googleLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    var mLatitude: Double = 0.0
        private set

    var mLongitude: Double = 0.0
        private set
    var accuracy: Float = 0f
        private set

    private fun checkLocationPermissions(): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (checkLocationPermissions()) {
            lifecycleScope.launch {
                if (!isServiceRunning) {
                    startForeground(1, createChannel().build())
                }
                locationUpdates()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)

        isServiceRunning = true
        handleBind()
        return localBinder
    }

    private fun handleBind() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26 이상은 포그라운드 서비스 시작
            startForegroundService(Intent(this@LocationService, LocationService::class.java))
        } else {
            // API 26 미만은 일반 서비스로 시작
            startService(Intent(this, LocationService::class.java))
        }
        startForeground(1, createChannel().build())
    }


    override fun onUnbind(intent: Intent?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopSelf()
        }
        stopForeground(true)
        isServiceRunning = false
        return true
    }

    private fun createChannel(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val builder = if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            val channelId = "foreground_gps_service_channel"
            val channel = NotificationChannel(
                channelId,
                "My GPS Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
            NotificationCompat.Builder(this, channelId)
        } else {
            NotificationCompat.Builder(this)
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Location Service Test")
            .setContentText("Location Tracking is Running")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder
    }

    @SuppressLint("MissingPermission")
    private fun locationUpdates() {
        googleLocation.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                mLatitude = location.latitude
                mLongitude = location.longitude
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.run {
            interval = 60_000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // 저전력, wifi와 셀타워 위치를 사용
            //priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 가장 정확한 위치를 요청, GPS,wifi, 셀타워등 모든 수단으로 위치를 확인(자전거 트레킹과 같이 정확한 트레킹이 필요할 때)
        }

        googleLocation.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        )
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations[0] != null) {
                mLatitude = locationResult.locations[0].latitude
                mLongitude = locationResult.locations[0].longitude
                accuracy = locationResult.locations[0].accuracy
            }

            actionFunction()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceRunning) {
            stopService(Intent(this, LocationService::class.java))
            stopForeground(true)
            isServiceRunning = false
        }
    }

    internal inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

}

