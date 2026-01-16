package com.example.obstacles.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class LocationPermissionManager(
    private val activity: ComponentActivity,
    private val onResult: (Boolean) -> Unit
) : LocationPermissionChecker {

    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result.values.any { it }
            onResult(granted)
        }

    override fun hasLocationPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions() {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
