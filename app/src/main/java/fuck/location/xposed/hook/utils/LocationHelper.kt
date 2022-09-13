package io.github.mikotwa.yucklocation.hook.utils

import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi

class LocationHelper {
    fun generateMockedLocation(result: Any?): Location {
        lateinit var location: Location
        if (result == null) {
            location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Location(LocationManager.FUSED_PROVIDER)
            } else {
                Location(LocationManager.GPS_PROVIDER)
            }
            location.time = System.currentTimeMillis() - (100..10000).random()
        } else {
            location = result as Location
        }

        location.latitude = 1.0
        location.longitude = 1.0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock = false
        }

        location.altitude = 0.0
        location.speed = 0F
        location.speedAccuracyMetersPerSecond = 0F

        location.time = 1657454958
        location.accuracy = 1.2F
        location.bearing = 1F
        location.bearingAccuracyDegrees = 1F
        location.elapsedRealtimeNanos = 1657454958

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            location.elapsedRealtimeUncertaintyNanos = 0.0
        }

        location.verticalAccuracyMeters = 1.2F

        return location
    }
}