package com.parade621.locationforegroundservice

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.parade621.locationforegroundservice.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashActivity : AppCompatActivity() {
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    private val isPermissionGranted = MutableLiveData(false)

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                moveToNextScreen()
            }
        val isIgnoringBatteryOptimizations =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
                packageName
            )

        isPermissionGranted.observe(this) { isGrant ->
            if (isGrant) {
                if (!isIgnoringBatteryOptimizations) {
                    AlertDialog.Builder(this@SplashActivity)
                        .setTitle("Alert")
                        .setMessage(
                            """
                        Excluding it from battery optimization is essential for stable usage.
                        Please "Allow" to ignored battery opimization.
                    """.trimIndent()
                        )
                        .setCancelable(false)
                        .setPositiveButton("To Setting") { _, _ ->
                            val intent =
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            intent.data = Uri.parse("package:$packageName")
                            activityResultLauncher.launch(intent)
                        }.show()
                } else {
                    moveToNextScreen()
                }
            }
        }

        checkPermission()
    }


    private fun moveToNextScreen() {
        lifecycleScope.launch {
            delay(500)
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkPermission() {
        val permissionList = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // 28 까지
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // 32 까지
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // 33 이상
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        val requestList = ArrayList<String>()

        for (permission in permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestList.add(permission)
            }
        }

        isPermissionGranted.value = if (requestList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requestList.toTypedArray(), 0)
            false // 권한 요청 필요, 즉시 false 반환
        } else {
            true // 권한 요청 필요 없음, 즉시 true 반환
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {

            val deniedPermission = ArrayList<String>()

            for ((index, result) in grantResults.withIndex()) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    deniedPermission.add(permissions[index])
                }
            }

            if (deniedPermission.isNotEmpty()) {
                Snackbar.make(
                    binding.root,
                    "Permission denied. Please allow the permission at setting.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("To Setting") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.setAction("Close") {
                    finish()
                }.show()
            } else {
                isPermissionGranted.value = true
            }
        }
    }
}