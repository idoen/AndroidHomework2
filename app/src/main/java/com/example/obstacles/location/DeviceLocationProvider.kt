package com.example.obstacles.location

import android.location.Location

fun interface DeviceLocationProvider {
    fun fetchCurrentLocation(onResult: (Location?) -> Unit)
}
