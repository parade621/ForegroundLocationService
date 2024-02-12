package com.parade621.locationforegroundservice.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

object MyService : ServiceConnection {

    var service: LocationService? = null
        private set

    var isBound = false
        private set

    fun bindService() {
        isBound = true
    }

    fun unbindService() {
        service?.onDestroy()
        isBound = false
        service = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocationService.LocalBinder
        MyService.service = binder.getService()
        bindService()
    }

    // 서비스가 강제 종료되면 호출됨
    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        // 보통 여기서 에러 로그를 서버에 남기곤 합니다.
    }

    fun getLatitude(): Double = service?.mLatitude ?: 0.0

    fun getLongitude(): Double = service?.mLongitude ?: 0.0

}