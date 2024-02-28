package com.parade621.locationforegroundservice

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.parade621.locationforegroundservice.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 앱을 실행하면 가장 먼저 보이는 화면입니다.
 *
 * 권한을 확인하고, 배터리 최적화를 확인합니다.
 *
 * 권한이 허용되어 있지 않다면, 권한을 요청합니다.
 *
 * 배터리 최적화가 되어 있지 않다면, 배터리 최적화를 요청합니다.
 *
 * 모든 권한과 배터리 최적화가 허용되어 있다면, BasicActivity로 이동합니다.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    /**
     * `ActivityResultLauncher`를 사용하기 위한 변수입니다.
     * `ActivityResultLauncher`는 `startActivityForResult()` 메서드를 대체하는 가장 최신의 방법으로,
     * androidX Activity result API의 일부입니다.
     *
     * 이를 사용함으로써, 보다 Type-Safe하고 직관적인 방법으로 액티비티 결과를 처리할 수 있습니다.
     *
     * 결과를 받아오는 콜백을 `registerForActivityResult()` 메서드를 통해 등록함으로써, 액티비티나 프래그먼트의 생명주기와 강하게 결합되지 않는 동시에
     * 결과 처리 로직을 더욱 명확하게 구성할 수 있습니다.
     */
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    // 굳이 이렇게 작성하실 필요는 없습니다.
    // 개인적으로 권한 체크가 모두 완료된 후, 배터리 최적화를 확인하는 것을 선호해서 권한 허용 여부를 관찰하는
    // `isPermissionGranted` LiveData를 만들었습니다.
    private val isPermissionGranted = MutableLiveData(false)

    /**
     * [@suppressLint] 어노테이션은 안드로이드 리프트 툴이 특정 경고나 오류를 무시하도록 지시하는 어노테이션입니다.
     * Lint는 안드로이드 앱 개발에서 발생할 수 있는 여러 가지 오류나 경고를 검사하는 도구로, 가능한 버그, 최적화 되지 않은 코드,사용성 문제 등을
     * 식별할 수 있습니다. 하지만, 때로는 Lint가 제기하는 경고를 의도적으로 무시해야할 때, 사용하는 어노테이션입니다.
     *
     * BatteryLife는 LInt의 체크 id로, 배터리 최적화를 무시하도록 lint에 지시합니다.
     */
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // `ActivityResultLauncher`를 초기화합니다.
        // 작업이 완료된 이후 moveToNextScreen() 메서드를 호출합니다.
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                moveToNextScreen()
            }

        // 배터리 최적화가 적용되어 있는지 확인합니다.
        val isIgnoringBatteryOptimizations =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
                packageName
            )

        // 모든 권한이 허용되어 있다면, 배터리 최적화를 확인합니다.
        // 배터리 최적화가 적용되어 있지 않다면, 배터리 최적화를 요청합니다.
        // 배터리 최적화 허용과 관련된 다이얼로그를 표시하기 전에, 유저에게 필요성을 고지해 주는게 좋습니다.
        // 저는 AlertDialog를 통해 유저에게 필요성을 고지하고, 배터리 최적화를 요청하는 방식으로 구현했습니다.
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

        // 권한을 확인합니다.
        checkPermission()
    }


    // 다음 화면으로 넘어갑니다.
    private fun moveToNextScreen() {
        lifecycleScope.launch {
            delay(500)
            val intent = Intent(this@SplashActivity, BasicActivity::class.java)
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
                Manifest.permission.FOREGROUND_SERVICE,
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