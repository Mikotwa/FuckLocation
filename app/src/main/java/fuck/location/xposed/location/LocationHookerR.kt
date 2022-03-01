package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.graphics.Bitmap
import android.location.*
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.Exception
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class LocationHookerR {
    @SuppressLint("PrivateApi")
    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")

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

                    location.latitude = fakeLocation.x
                    location.longitude = fakeLocation.y
                    location.altitude = 0.0
                    location.speed = 0F
                    location.speedAccuracyMetersPerSecond = 0F

                    try {
                        HiddenApiBypass.invoke(
                            location.javaClass,
                            location,
                            "setIsFromMockProvider",
                            false
                        )
                    } catch (e: Exception) {
                        XposedBridge.log("FL: Not possible to mock (R)! $e")
                    }

                    XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                    it.result = location
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

        /* What we are doing there is:
         * Firstly, we remove all the package that are listed on the whitelist (before)
         * Then, we will manually notify these apps individually (after)
         *
         * In short, we let the framework broadcast all normal apps first,
         * then we will manually give other answers to whitelisted apps :)
         */

        findAllMethods(clazz) {
            name == "handleLocationChangedLocked" && isNotPublic
        }.hookMethod {
            before { param ->
                XposedBridge.log("FL: in handleLocationChangedLocked (R)! Removing whitelisted apps...")
                val mRecordsByProviderField = findField(clazz) {
                    name == "mRecordsByProvider"
                }

                mRecordsByProviderField.isAccessible = true

                val records = mRecordsByProviderField.get(param.thisObject) as HashMap<*, *>
                val newRecords = HashMap<String, ArrayList<*>>()

                records.entries.forEach { entry ->
                    val key = entry.key as String
                    val value = entry.value as ArrayList<*>
                    val newValue = ArrayList<Any>()

                    value.forEach { record ->
                        val mReceiver = findField(record.javaClass) {
                            name == "mReceiver"
                        }.get(record)

                        val mCallerIdentity = findField(mReceiver.javaClass, true) {
                            name == "mCallerIdentity"
                        }.get(mReceiver)

                        val packageName =
                            ConfigGateway.get().callerIdentityToPackageName(mCallerIdentity!!)

                        if (!ConfigGateway.get().inWhitelist(packageName)) {
                            newValue.add(record)
                        } else {
                            val originLocation =
                                (param.args[1] as Location).takeIf { param.args[1] != null }
                                    ?: Location(LocationManager.GPS_PROVIDER)
                            val fakeLocation = ConfigGateway.get().readFakeLocation()

                            val location = Location(originLocation.provider)

                            location.latitude = fakeLocation.x
                            location.longitude = fakeLocation.y
                            location.altitude = 0.0
                            location.speed = 0F
                            location.speedAccuracyMetersPerSecond = 0F

                            location.time = originLocation.time
                            location.accuracy = originLocation.accuracy
                            location.bearing = originLocation.bearing
                            location.bearingAccuracyDegrees = originLocation.bearingAccuracyDegrees
                            location.elapsedRealtimeNanos = originLocation.elapsedRealtimeNanos
                            location.verticalAccuracyMeters = originLocation.verticalAccuracyMeters

                            try {
                                HiddenApiBypass.invoke(
                                    location.javaClass,
                                    location,
                                    "setIsFromMockProvider",
                                    false
                                )
                            } catch (e: Exception) {
                                XposedBridge.log("FL: Not possible to mock (R)! $e")
                            }

                            // TODO: this is a unsafe call that bypass the validation of system
                            findMethod(mReceiver.javaClass, false) {
                                name == "callLocationChangedLocked" && isPublic
                            }.invoke(mReceiver, location)
                        }
                    }

                    newRecords[key] = newValue
                }

                mRecordsByProviderField.set(param.thisObject, newRecords)
                XposedBridge.log("FL: Finished delivering altered records...")
            }
        }

        findAllMethods(clazz) {
            name == "requestGeofence" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[3] as String
            XposedBridge.log("FL: in requestGeofence (R)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

    }

    @SuppressLint("PrivateApi")
    fun hookDLC(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")

        /*findAllMethods(clazz) {
            name == "initializeProvidersLocked" && isPrivate
        }.hookAfter { param ->
            val locationProviderManager = findMethod(clazz) {
                name == "getLocationProviderManager"
            }.invoke(param.thisObject, "fused")

            findMethod(locationProviderManager.javaClass) {
                name == "setRealProvider"
            }.invoke(locationProviderManager, null)
        }*/
    }
}