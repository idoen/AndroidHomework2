package com.example.obstacles.highscores

import android.location.Location
import com.example.obstacles.location.DeviceLocationProvider
import com.example.obstacles.location.LocationLabelFormatter

class HighScoreLocationService(
    private val locationProvider: DeviceLocationProvider,
    private val labelFormatter: LocationLabelFormatter
) {

    fun fetchLocation(onResult: (LocationPoint) -> Unit) {
        locationProvider.fetchCurrentLocation { location ->
            onResult(toLocationPoint(location))
        }
    }

    private fun toLocationPoint(location: Location?): LocationPoint {
        if (location == null) {
            return LocationPoint(
                latitude = DEFAULT_LATITUDE,
                longitude = DEFAULT_LONGITUDE,
                label = labelFormatter.formatUnavailable()
            )
        }
        return LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            label = labelFormatter.format(location)
        )
    }

    companion object {
        private const val DEFAULT_LATITUDE = 31.5
        private const val DEFAULT_LONGITUDE = 35.2
    }
}
