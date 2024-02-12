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
import androidx.lifecycle.lifecycleScope
import com.parade621.locationforegroundservice.databinding.ActivityMainBinding
import com.parade621.locationforegroundservice.service.LocationService
import com.parade621.locationforegroundservice.service.MyService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        startLocationService()

        binding.latitudeText.text = "Latitude: ${MyService.getLatitude()}"
        binding.longitudeText.text = "Longitude: ${MyService.getLongitude()}"
    }

    private fun startLocationService() {
        if (MyService.isBound) {
            unbindService(MyService)
            MyService.unbindService()
        }

        val serviceIntent = Intent(applicationContext, LocationService::class.java)
        bindService(serviceIntent, MyService, BIND_AUTO_CREATE)
    }
}