package com.parade621.locationforegroundservice

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.parade621.locationforegroundservice.databinding.ActivityMainBinding
import com.parade621.locationforegroundservice.service.LocationService
import com.parade621.locationforegroundservice.service.MyService

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        startLocationService()
    }

    private fun startLocationService() {
        if (MyService.isBound) {
            unbindService(MyService)
            MyService.unbindService()
        }

        val serviceIntent = Intent(applicationContext, LocationService::class.java)
        bindService(serviceIntent, MyService, BIND_AUTO_CREATE)

        binding.refreshButton.setOnClickListener {
            binding.latitudeText.text = "Latitude: ${MyService.getLatitude()}"
            binding.longitudeText.text = "Longitude: ${MyService.getLongitude()}"
        }
    }
}