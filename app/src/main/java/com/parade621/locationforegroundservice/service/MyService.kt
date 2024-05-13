package com.parade621.locationforegroundservice.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.parade621.locationforegroundservice.LocationServiceSample

/**
 * 서비스와 액티비티 간의 통신을 위한 싱글톤 객체입니다.
 *
 * ServiceConnection은 Android에서 애플리케이션 컴포넌트와 백그라운드 서비스 간의 연결을 관리하는 인터페이스로,
 * 액티비티나 다른 컴포넌트가 서비스에 바인드/언바인드 될 때 실행될 콜백 메서드를 정의할 수 있습니다.
 * 주로 bindService() 메서드를 호출하여 서비스에 바인드할 때 사용됩니다.
 */
object MyService : ServiceConnection {

    // 서비스 인스턴스를 저장합니다.
    private var service: LocationService? = null

    // 서비스가 바인드되었는지 여부를 반환합니다.
    var isBound = false
        private set

    /**
     * 서비스에 성공적으로 바인드되었을 때 호출됩니다.
     * 이 메서드는 onBind() 메서드가 반환하는 IBinder 인스턴스를 받아, 클라이언트가 서비스의 메서드를 호출할 수 있도록 합니다.
     *
     * [ComponentName]: 서비스의 이름을 나타내는 클래스입니다.
     */
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocationService.LocalBinder
        MyService.service = binder.getService()
        isBound = true
    }

    /**
     * 서비스가 예상치 못하게 끊어질 때 호출됩니다.
     * 시스템에 의해 서비스가 강제로 종료되거나, 서비스가 실행되는 프로세스에서 Crash가 발생하여 해당 프로세스가 종료되었을 때 호출됩니다.
     * 명시적으로 unbindService() 메서드를 호출하여 서비스를 언바인드하면 이 메서드는 호출되지 않습니다.
     */
    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        // 보통 여기서 에러 로그를 서버에 남기곤 합니다.
    }

    fun bindService() {
        if (!isBound) {
            // LocationService에 바인딩하기 위한 Intent를 생성합니다.
            // 이 Intent는 LocationService 클래스를 명시적으로 지정합니다.
            // 어플리케이션 전역에서 사용하기 위해 applicationContext를 사용합니다.
            val serviceIntent = Intent(LocationServiceSample.context, LocationService::class.java)
            // 생성한 Intent와 serviceConnection 객체(MyService)를 사용하여 LocationService에 바인드합니다.
            // BIND_AUTO_CREATE 플래그는 서비스가 아직 실행 중이 아니라면 자동으로 생성하도록 합니다.
            LocationServiceSample.context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (isBound) {
            LocationServiceSample.context.unbindService(this)
            isBound = false
            service?.onDestroy()
            service = null
        }
    }

    // 서비스의 위도를 반환합니다.
    fun getLatitude(): Double = service?.latitude ?: 0.0

    // 서비스의 경도를 반환합니다.
    fun getLongitude(): Double = service?.longitude ?: 0.0

}