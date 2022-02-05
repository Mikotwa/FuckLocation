package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.location.*
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.Exception

class LocationHookerR {
    @SuppressLint("PrivateApi")
    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> = lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")

        // TODO: Upgrade to modern WhitelistGateway
        findAllMethods(clazz) {
            name == "getLastLocation" && isPublic
        }.hookMethod {
            after {
                val packageName = ConfigGateway.get().callerIdentityToPackageName(it.args[1])
                XposedBridge.log("FL: in getLastLocation! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: in whitelist! Return custom location")
                    val fakeLocation = ConfigGateway.get().readFakeLocation()

                    lateinit var location: Location
                    lateinit var originLocation: Location

                    if (it.result == null) {
                        location = Location(LocationManager.GPS_PROVIDER)
                        location.time = System.currentTimeMillis() - (100..10000).random()
                    } else {
                        originLocation = it.result as Location
                        location = Location(originLocation.provider)

                        location.time = originLocation.time
                        location.accuracy = originLocation.accuracy
                        location.bearing = originLocation.bearing
                        location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                        location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                        location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters
                    }

                    location.latitude = fakeLocation?.x!!
                    location.longitude = fakeLocation.y
                    location.altitude = 0.0
                    location.speed = 0F
                    location.speedAccuracyMetersPerSecond = 0F

                    try {
                        HiddenApiBypass.invoke(location.javaClass, location, "setIsFromMockProvider", false)
                    } catch (e: Exception) {
                        XposedBridge.log("FL: Not possible to mock (R)! $e")
                    }

                    XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                }
            }
        }

        findAllMethods(clazz) {
            name == "getCurrentLocation" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = ConfigGateway.get().callerIdentityToPackageName(param.args[2])
                XposedBridge.log("FL: in getCurrentLocation! Caller package name: $packageName")

                param.result = null
            }
        }

        findAllMethods(clazz) {
            name == "requestLocationUpdatesLocked" && isNotPublic
        }.hookMethod {
            before { param ->
                val receiver = param.args[1]
                XposedBridge.log("FL: in requestLocationUpdatesLocked (R)!")

                val methods = findAllMethods(receiver.javaClass) {
                    name == "getListener" && isPublic
                }

                XposedBridge.log("FL: Finished finding method (R)! Here is all methods: ")

                for (i in methods) {
                    XposedBridge.log("FL: $i")
                }

                val listener = methods.invokeMethod("getListener")

                if (listener != null) {
                    XposedBridge.log("FL: Finished finding listener (R)! Hooking method...")
                } else {
                    XposedBridge.log("FL: Listener is null, this situation in currently not handled...")
                }
            }
        }
    }
}