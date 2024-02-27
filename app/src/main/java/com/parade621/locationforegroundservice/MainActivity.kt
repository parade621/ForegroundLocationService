package com.parade621.locationforegroundservice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.parade621.locationforegroundservice.databinding.ActivityMainBinding
import com.parade621.locationforegroundservice.service.LocationService
import com.parade621.locationforegroundservice.service.MyService

/**
 * 가장 기본적인 GPS 서비스의 동작을 확인하기 위한 액티비티입니다
 *
 * 서비스를 바인드하고, 버튼을 클릭하면 위도와 경도를 가져와서 화면에 띄워줍니다.
 *
 * 기능의 동작 확인을 목적으로, 비동기 처리나 별도의 에러 처리는 하지 않았습니다.
 */
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var isServiceLearning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isServiceLearning = startLocationService()

        binding.getLocationButton.setOnClickListener {
            if (isServiceLearning) {
                binding.latitudeText.text = "Latitude: ${MyService.getLatitude()}"
                binding.longitudeText.text = "Longitude: ${MyService.getLongitude()}"
            }
        }
    }

    /**
     * 서비스를 실행하는 함수입니다.
     *
     * return: 서비스 실행 여부
     */
    private fun startLocationService(): Boolean {
        // 서비스를 바인드합니다.
        // 서비스가 이미 바인드되어 있다면, 언바인드 후 다시 바인드합니다.
        if (MyService.isBound) {
            unbindService(MyService)
            MyService.unbindService()
        }

        return try {
            val serviceIntent = Intent(applicationContext, LocationService::class.java)
            bindService(serviceIntent, MyService, BIND_AUTO_CREATE)
            true
        } catch (e: Exception) {
            false
        }
    }


    // 앱이 종료될 때, 서비스를 언바인드합니다.
    // 백스택의 최하단에 항상 존재하는 액티비티에서만 사용하는 것이 좋습니다.
    override fun onDestroy() {
        super.onDestroy()
        if (MyService.isBound) {
            unbindService(MyService)
            MyService.unbindService()
        }
    }
}