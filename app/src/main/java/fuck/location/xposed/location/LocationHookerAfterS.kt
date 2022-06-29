package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.location.*
import android.os.Build
import android.util.ArrayMap
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway
import java.lang.Exception

class LocationHookerAfterS {
    @SuppressLint("PrivateApi")
    @RequiresApi(Build.VERSION_CODES.S)
    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.location.provider.LocationProviderManager")

        findAllMethods(clazz) {
            name == "onReportLocation"
        }.hookMethod {
            before { param ->
                hookOnReportLocation(clazz, param)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("PrivateApi")
    fun hookDLC(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")

        findAllMethods(clazz) {
            name == "getLastLocation" && isPublic
        }.hookMethod {
            after {
                try {
                    // Workaround for MIUI. F**k!
                    val targetParam: Any = if (it.args[0] is String) it.args[2]
                    else it.args[1]

                    val packageName = ConfigGateway.get().callerIdentityToPackageName(targetParam)
                    XposedBridge.log("FL: in getLastLocation! Caller package name: $packageName")

                    if (ConfigGateway.get().inWhitelist(packageName)) {
                        XposedBridge.log("FL: in whitelist! Return custom location")
                        val fakeLocation = ConfigGateway.get().readFakeLocation()

                        lateinit var location: Location
                        lateinit var originLocation: Location

                        if (it.result == null) {
                            location = Location(LocationManager.FUSED_PROVIDER)
                            location.time = System.currentTimeMillis() - (100..10000).random()
                        } else {
                            originLocation = it.result as Location
                            location = Location(originLocation.provider)

                            location.time = originLocation.time
                            location.accuracy = originLocation.accuracy
                            location.bearing = originLocation.bearing
                            location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                            location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                            location.elapsedRealtimeUncertaintyNanos = originLocation.elapsedRealtimeUncertaintyNanos
                            location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters
                        }

                        location.latitude = fakeLocation.x + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                        location.longitude = fakeLocation.y + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                        location.altitude = 0.0
                        location.isMock = false
                        location.speed = 0F
                        location.speedAccuracyMetersPerSecond = 0F

                        XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                        it.result = location
                    }
                } catch (e: Exception) {
                    XposedBridge.log("FL: Fuck with exceptions! $e")
                    e.printStackTrace()
                }
            }
        }

        findAllMethods(clazz) {
            name == "getCurrentLocation" && isPublic
        }.hookMethod {
            after { param ->
                // Workaround for MIUI. F**k!
                val targetParam: Any = if (param.args[0] is String) param.args[0]
                else param.args[1]

                val packageName = ConfigGateway.get().callerIdentityToPackageName(targetParam)

                XposedBridge.log("FL: in getCurrentLocation! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: in whiteList! Inject null...")
                    param.result = null
                }
            }
        }

        findAllMethods(clazz) {
            name == "registerGnssStatusCallback" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in registerGnssStatusCallback (S, DLC)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "registerGnssNmeaCallback" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in registerGnssNmeaCallback (S, DLC)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "requestGeofence" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[2] as String
            XposedBridge.log("FL: in requestGeofence (S, DLC)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalStdlibApi::class)
    private fun hookOnReportLocation(clazz: Class<*>, param: XC_MethodHook.MethodHookParam) {
        XposedBridge.log("FL: in onReportLocation!")

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

                location.latitude = fakeLocation.x + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
                location.longitude = fakeLocation.y + (Math.random() * fakeLocation.offset - fakeLocation.offset / 2)
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