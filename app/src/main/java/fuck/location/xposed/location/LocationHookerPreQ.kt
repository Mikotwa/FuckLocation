package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass
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
            name == "handleLocationChangedLocked" && isNotPublic
        }.hookMethod {
            before { param ->
                XposedBridge.log("FL: in handleLocationChangedLocked (Pre Q)! Removing whitelisted apps...")
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

                        val packageName = ConfigGateway.get().callerIdentityToPackageName(mCallerIdentity!!)

                        if (!ConfigGateway.get().inWhitelist(packageName)) {
                            newValue.add(record)
                        } else {
                            val originLocation = (param.args[0] as Location).takeIf { param.args[0] != null } ?: Location(LocationManager.GPS_PROVIDER)
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
                                HiddenApiBypass.invoke(location.javaClass, location, "setIsFromMockProvider", false)
                            } catch (e: Exception) {
                                XposedBridge.log("FL: Not possible to mock (Pre Q)! $e")
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
    }
}