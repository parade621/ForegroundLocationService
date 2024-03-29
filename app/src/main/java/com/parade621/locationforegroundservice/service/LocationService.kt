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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.parade621.locationforegroundservice.BasicActivity
import com.parade621.locationforegroundservice.R
import timber.log.Timber

class LocationService : LifecycleService() {

    // 서비스 바인딩을 위한 Binder 클래스
    private val localBinder = LocalBinder()

    // 서비스가 실행중인지 확인하는 변수
    private var isServiceRunning = false

    // 구글 위치 서비스를 사용하기 위한 변수
    private val googleLocation by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    // 위도, 경도, 정확도를 저장하기 위한 변수들
    var latitude: Double = 0.0
        private set
    var longitude: Double = 0.0
        private set
    var accuracy: Float = 0f
        private set

    // 권한이 허용되어 있는지 확인하는 함수
    // 논리적으로 반드시 권한이 부여되어 있다면 굳이 작성할 필요는 없지만, 샘플코드라서 작성하였습니다.
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

    // 서비스를 시작하는데 사용되며, 바인드와는 직접적인 연관이 없습니다.
    // startService 또는 startForegroundService로 서비스를 시작할 때 호출됩니다.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (checkLocationPermissions()) {
            if (!isServiceRunning) {
                startForeground(1, createChannel().build())
            }
            locationUpdates()
        }
        // START_STICKY, START_NOT_STICKY, START_REDELIVER_INTENT와 같은 반환 값은
        // 서비스가 시스템에 의해 종료된 후 재시작되는 방식을 제어합니다.
        return START_STICKY
    }

    // bindService를 통해 실행됩니다.
    // 바인딩은 클라이언트와 서비스 사이에 상호 작용할 수 있는 통신 채널을 제공합니다.
    // 바인딩된 서비스는 바인딩된 컴포넌트가 존재하는 동안에만 실행됩니다.
    // onBind는 서비스가 시작되었을 때가 아니라, 서비스에 바인딩되었을 때 호출되는 것입니다.
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        isServiceRunning = true
        MyService.bindService()
        handleBind()
        return localBinder
    }


    //    override fun onRebind(intent: Intent?) {
    //        handleBind()
    //    }

    // 바인드 함수에서 호출되는 커스텀 함수로, Rebound까지 고려하여 별도로 함수로 분리하여 작성하였습니다.
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

    // 서비스가 언바인드 될 때 호출되는 함수
    override fun onUnbind(intent: Intent?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopSelf()
        }
        stopForeground(true)
        isServiceRunning = false
        return true
    }

    // 알림 채널을 생성하는 함수
    // API 26 이상에서는 알림 채널을 생성해야 합니다.
    // 알림 채널은 ID가 동일한 경우, 굳이 서비스를 재시작할 때 알림을 삭제하고 다시 생성하지 않아도 됩니다.
    // 동일한 ID의 알림은 업데이트 되는 방식이기 때문입니다.
    private fun createChannel(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, BasicActivity::class.java).apply {
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

    // 위치 업데이트를 요청하는 함수
    @SuppressLint("MissingPermission")
    private fun locationUpdates() {
        googleLocation.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
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

    // 위치 업데이트를 받는 콜백 함수
    // 설정된 시간 간격마다 위치 정보를 업데이트 받습니다.
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations[0] != null) {
                Timber.e("확인: ${locationResult.locations[0].latitude} / ${locationResult.locations[0].longitude}")
                latitude = locationResult.locations[0].latitude
                longitude = locationResult.locations[0].longitude
                accuracy = locationResult.locations[0].accuracy
            }

            actionFunction()
        }
    }

    // 서비스를 종료하는 함수
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceRunning) {
            stopService(Intent(this, LocationService::class.java))
            stopForeground(true)
            isServiceRunning = false
        }
    }

    //
    internal inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
}

