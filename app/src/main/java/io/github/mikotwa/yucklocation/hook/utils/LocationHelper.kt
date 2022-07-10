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

        location.latitude = 114.514
        location.longitude = 191.9310

        return location
    }
}