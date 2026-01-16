package com.example.obstacles.location

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

class AndroidLocationProvider(
    private val context: Context,
    private val permissionChecker: LocationPermissionChecker,
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
) : DeviceLocationProvider {

    override fun fetchCurrentLocation(onResult: (Location?) -> Unit) {
        if (!permissionChecker.hasLocationPermissions()) {
            onResult(null)
            return
        }

        val lastKnown = locationManager.getProviders(true)
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }

        if (lastKnown != null) {
            onResult(lastKnown)
            return
        }

        val provider = locationManager.getBestProvider(Criteria(), true)
        if (provider == null) {
            onResult(null)
            return
        }

        val delivered = AtomicBoolean(false)
        val mainHandler = Handler(Looper.getMainLooper())
        lateinit var listener: LocationListener

        val deliverResult: (Location?) -> Unit = { location ->
            if (delivered.compareAndSet(false, true)) {
                locationManager.removeUpdates(listener)
                mainHandler.removeCallbacksAndMessages(null)
                onResult(location)
            }
        }

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                deliverResult(location)
            }

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit
        }

        try {
            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                listener,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            deliverResult(null)
            return
        }

        mainHandler.postDelayed(
            { deliverResult(null) },
            LOCATION_TIMEOUT_MS
        )
    }

    companion object {
        private const val LOCATION_TIMEOUT_MS = 3000L
    }
}
