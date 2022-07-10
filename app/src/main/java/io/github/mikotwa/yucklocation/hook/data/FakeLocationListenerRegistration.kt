package io.github.mikotwa.yucklocation.hook.data

import android.location.LocationListener
import android.location.LocationRequest

class FakeLocationListenerRegistration(
    var request: LocationRequest,
    var packageName: String,
    var locationListener: LocationListener,
    permissionLevel: Int
) {

}