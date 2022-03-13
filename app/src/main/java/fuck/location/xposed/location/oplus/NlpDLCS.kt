package fuck.location.xposed.location.oplus

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.ArrayMap
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class NlpDLCS {
    @SuppressLint("PrivateApi")
    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    fun hookColorOS(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.location.OplusLocationManagerService")

        findAllMethods(clazz) {
            name == "newLocProviderManager" && isPrivate
        }.hookAfter { param ->
            val providerName = param.args[0] as String

            XposedBridge.log("FL: [Color] in newLocProviderManager!")
            if (providerName == "network") {    // Hook Nlp provider manager each time
                XposedBridge.log("FL: [Color] respawn nlp manager, trying to hook...")

                val locationProviderManager = param.result
                if (locationProviderManager != null) {
                    findAllMethods(locationProviderManager.javaClass) {
                        name == "onReportLocation"
                    }.hookMethod {
                        before { param ->
                            hookOnReportLocation(clazz, param)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalStdlibApi::class)
    private fun hookOnReportLocation(clazz: Class<*>, param: XC_MethodHook.MethodHookParam) {
        XposedBridge.log("FL: [Color] in onReportLocation!")

        val mRegistrations = findField(clazz, true) {
            name == "mRegistrations"
        }

        mRegistrations.isAccessible = true

        val registrations = mRegistrations.get(param.thisObject) as ArrayMap<*, *>
        val newRegistrations = ArrayMap<Any, Any>()

        registrations.forEach { registration ->
            val callerIdentity = findField(registration.value.javaClass, true) {
                name == "mIdentity"
            }.get(registration.value)

            val packageName = ConfigGateway.get().callerIdentityToPackageName(callerIdentity!!)

            if (!ConfigGateway.get().inWhitelist(packageName)) {
                newRegistrations[registration.key] = registration.value
            } else {
                val value = registration.value
                val locationResult = param.args[0]

                val mLocationsField = findField(locationResult.javaClass, true) {
                    name == "mLocations" && isPrivate
                }

                mLocationsField.isAccessible = true
                val mLocations = mLocationsField.get(locationResult) as ArrayList<*>

                val originLocation = (mLocations[0] as Location).takeIf { mLocations.isNotEmpty() } ?: Location(LocationManager.GPS_PROVIDER)
                val fakeLocation = ConfigGateway.get().readFakeLocation()

                val location = Location(originLocation.provider)

                location.latitude = fakeLocation.x
                location.longitude = fakeLocation.y
                location.isMock = false
                location.altitude = 0.0
                location.speed = 0F
                location.speedAccuracyMetersPerSecond = 0F

                location.time = originLocation.time
                location.accuracy = originLocation.accuracy
                location.bearing = originLocation.bearing
                location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                location.elapsedRealtimeUncertaintyNanos = originLocation.elapsedRealtimeUncertaintyNanos
                location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters

                mLocationsField.set(locationResult, arrayListOf(location))

                val method = findMethod(value.javaClass, true) {
                    name == "acceptLocationChange"
                }

                val operation = method.invoke(value, locationResult)

                findMethod(value.javaClass, true) {
                    name == "executeOperation"
                }.invoke(value, operation)
            }
        }

        mRegistrations.set(param.thisObject, newRegistrations)
    }
}