package com.parade621.locationforegroundservice

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
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
            // 서비스가 이미 바인딩되어 있다면 해제
            unbindService(MyService)
            MyService.unbindService()
        }

        val serviceIntent = Intent(applicationContext, LocationService::class.java)
        bindService(serviceIntent, MyService, BIND_AUTO_CREATE)
        MyService.bindService()
    }
}