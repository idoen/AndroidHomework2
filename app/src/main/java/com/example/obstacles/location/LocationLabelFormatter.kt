package com.example.obstacles.location

import android.content.Context
import android.location.Location
import com.example.obstacles.R

class LocationLabelFormatter(private val context: Context) {

    fun format(location: Location): String {
        return context.getString(
            R.string.location_coordinates_format,
            location.latitude,
            location.longitude
        )
    }

    fun formatUnavailable(): String {
        return context.getString(R.string.location_unavailable)
    }
}
