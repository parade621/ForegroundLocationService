package com.parade621.locationforegroundservice.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

object MyService : ServiceConnection {

    // 서비스 인스턴스를 저장합니다.
    private var service: LocationService? = null

    // 서비스가 바인드되었는지 여부를 반환합니다.
    var isBound = false
        private set

    // 서비스를 바인드합니다.
    fun bindService() {
        isBound = true
    }

    // 서비스를 언바인드합니다.
    fun unbindService() {
        service?.onDestroy()
        isBound = false
        service = null
    }

    // 서비스가 연결되면 호출됩니다.
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocationService.LocalBinder
        MyService.service = binder.getService()
        bindService()
    }

    // 서비스가 강제 종료되면 호출됩니다.
    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        // 보통 여기서 에러 로그를 서버에 남기곤 합니다.
    }

    // 서비스의 위도를 반환합니다.
    fun getLatitude(): Double = service?.latitude ?: 0.0

    // 서비스의 경도를 반환합니다.
    fun getLongitude(): Double = service?.longitude ?: 0.0

}