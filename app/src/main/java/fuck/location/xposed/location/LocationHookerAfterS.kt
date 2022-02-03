package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.location.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway
import java.lang.Exception

class LocationHookerAfterS {
    @SuppressLint("PrivateApi")
    @RequiresApi(Build.VERSION_CODES.S)
    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")

        findAllMethods(clazz) {
            name == "getLastLocation" && isPublic
        }.hookMethod {
            after { param ->
                try {
                    val packageName = param.args[2] as String
                    XposedBridge.log("FL: in getLastLocation! Caller package name: $packageName")

                    if (ConfigGateway.get().inWhitelist(packageName)) {
                        XposedBridge.log("FL: in whitelist! Return custom location")
                        val fakeLocation = ConfigGateway.get().readFakeLocation()

                        lateinit var location: Location
                        lateinit var originLocation: Location

                        if (param.result == null) {
                            location = Location(LocationManager.FUSED_PROVIDER)
                            location.time = System.currentTimeMillis() - (100..10000).random()
                        } else {
                            originLocation = param.result as Location

                            location = Location(originLocation.provider)

                            location.time = originLocation.time
                            location.accuracy = originLocation.accuracy
                            location.bearing = originLocation.bearing
                            location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                            location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                            location.elapsedRealtimeUncertaintyNanos = originLocation.elapsedRealtimeUncertaintyNanos
                            location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters
                        }

                        location.latitude = fakeLocation?.x!!
                        location.longitude = fakeLocation.y
                        location.altitude = 0.0
                        location.isMock = false
                        location.speed = 0F
                        location.speedAccuracyMetersPerSecond = 0F

                        XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                        param.result = location
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
                val packageName = param.args[3] as String
                XposedBridge.log("FL: in getCurrentLocation! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: in whiteList! Inject null...")
                    param.result = null
                }
            }
        }

        findAllMethods(clazz) {
            name == "registerLocationListener" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[3] as String
                XposedBridge.log("FL: in registerLocationListener! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: in whiteList! Inject custom location...")

                    val lastParam = param.args[2].javaClass
                    if (lastParam.typeName == "android.location.ILocationListener\$Stub\$Proxy") {
                        XposedBridge.log("FL: is LocationListener!")
                        val locationListener = param.args[3].javaClass

                        XposedBridge.log("FL: Finding method in LocationListener")

                        val targetMethod = findAllMethods(locationListener) {
                            name == "onLocationChanged" && parameterCount == 2
                        }

                        targetMethod.hookMethod {
                            before { param ->
                                val originalLocationList = param.args[0] as List<*>
                                val originLocation = originalLocationList[0] as Location

                                val fakeLocation = ConfigGateway.get().readFakeLocation()
                                val location = Location(originLocation.provider)

                                location.latitude = fakeLocation?.x!!
                                location.longitude = fakeLocation.y
                                location.altitude = 0.0
                                location.isMock = false
                                location.speed = 0F
                                location.speedAccuracyMetersPerSecond = 0F

                                location.time = originLocation.time
                                location.accuracy = originLocation.accuracy
                                location.bearing = originLocation.bearing
                                location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                                location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                                location.elapsedRealtimeUncertaintyNanos = originLocation.elapsedRealtimeUncertaintyNanos
                                location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters

                                val newLocationList = arrayListOf(location)
                                param.args[0] = newLocationList
                            }
                        }
                    } else {
                        // TODO: Implement PendingIntent
                        XposedBridge.log("FL: is PendingIntent that currently not supported")
                    }
                }
            }
        }
    }
}