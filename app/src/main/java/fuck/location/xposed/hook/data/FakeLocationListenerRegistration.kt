package fuck.location.xposed.hook.data

import android.location.LocationListener
import android.location.LocationRequest

class FakeLocationListenerRegistration(
    var request: LocationRequest,
    var packageName: String,
    permissionLevel: Int,
    var locationListener: Any?,
) {

}