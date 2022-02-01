package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.location.Location
import android.location.LocationManager
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.app.ui.models.FakeLocation
import fuck.location.xposed.helpers.ConfigGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.File
import java.lang.Exception

class LocationHookerPreQ {
    @SuppressLint("PrivateApi")
    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.LocationManagerService")

        findAllMethods(clazz) {
            name == "getLastLocation" && isPublic
        }.hookMethod {
            after {
                val packageName = ConfigGateway.get().callerIdentityToPackageName(it.args[1])
                XposedBridge.log("FL: in getLastLocation (Pre Q)! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: in whitelist! Return custom location")
                    val fakeLocation = ConfigGateway.get().readFakeLocation()

                    lateinit var location: Location
                    lateinit var originLocation: Location

                    if (it.result == null) {
                        location = Location(LocationManager.NETWORK_PROVIDER)
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
                        XposedBridge.log("FL: Not possible to mock (Pre Q)! $e")
                    }

                    XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                    it.result = location
                }
            }
        }

        findAllMethods(clazz) {
            name == "requestLocationUpdates" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[3] as String
                XposedBridge.log("FL: in requestLocationUpdates (Pre Q)! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: in whiteList! Inject custom location...")

                    val locationListener = param.args[1].javaClass

                    XposedBridge.log("FL: Finding method in LocationListener (Pre Q)")

                    val targetMethod = findAllMethods(locationListener) {
                        name == "onLocationChanged" && parameterCount == 1
                    }

                    targetMethod.hookMethod {
                        before { param ->
                            XposedBridge.log("FL: Hooking onLocationChanged for whitelist apps (Pre Q)!")
                            val originalLocation = param.args[0] as Location
                            val fakeLocation = ConfigGateway.get().readFakeLocation()

                            originalLocation.latitude = fakeLocation?.x!!
                            originalLocation.longitude = fakeLocation.y
                            originalLocation.altitude = 0.0
                            originalLocation.speed = 0F
                            originalLocation.speedAccuracyMetersPerSecond = 0F

                            try {
                                HiddenApiBypass.invoke(originalLocation.javaClass, originalLocation, "setIsFromMockProvider", false)
                            } catch (e: Exception) {
                                XposedBridge.log("FL: Not possible to mock (Pre Q)! $e")
                            }
                            XposedBridge.log("FL: Return custom location (Pre Q): $originalLocation")
                        }
                    }
                }
            }
        }
    }
}