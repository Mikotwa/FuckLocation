package fuck.location.xposed.location.miui

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.identity.Lte
import fuck.location.xposed.cellar.identity.Nr
import fuck.location.xposed.helpers.ConfigGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.Exception

class MiuiBlurLocationManagerHookerR {
    @SuppressLint("PrivateApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(ExperimentalStdlibApi::class)
    fun hookGetBlurryLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        lateinit var clazz: Class<*>
        when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.S -> {
                clazz =
                    lpparam.classLoader.loadClass("com.android.server.location.MiuiBlurLocationManagerImpl")
            }

            Build.VERSION_CODES.R -> {
                clazz =
                    lpparam.classLoader.loadClass("com.android.server.location.MiuiBlurLocationManager")
            }

            else -> {
                XposedBridge.log("FL: [Shaomi R] This is an unsupported version of MIUI! You may want to report this with detailed information. ${Build.VERSION.SDK_INT}")
                return
            }
        }

        findAllMethods(clazz) {
            name == "getBlurryLocation" && isPublic
        }.hookAfter { param ->
            val packageName = param.args[2] as String
            XposedBridge.log("FL: [Shaomi R] in getBlurryLocation! Caller packageName: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: [Shaomi R] in whitelist! Return custom location")
                val fakeLocation = ConfigGateway.get().readFakeLocation()

                lateinit var location: Location
                lateinit var originLocation: Location

                if (param.result == null) {
                    location = Location(LocationManager.GPS_PROVIDER)
                    location.time = System.currentTimeMillis() - (100..10000).random()
                } else {
                    originLocation = param.result as Location
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
                    XposedBridge.log("FL: [Shaomi R] Not possible to mock! $e")
                }

                XposedBridge.log("FL: [Shaomi R] x: ${location.latitude}, y: ${location.longitude}")
                param.result = location
            }
        }

        findAllMethods(clazz) {
            name == "getBlurryCellLocation" && isPublic && parameterCount == 1
        }.hookAfter { param ->
            try {
                val packageName = ConfigGateway.get().callerIdentityToPackageName(param.args[0])
                XposedBridge.log("FL: [Shaomi R] in getBlurryCellLocation! Caller packageName: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    when (param.result) {
                        is CellIdentityLte -> {
                            XposedBridge.log("FL: [Shaomi R] Using LTE Network...")
                            param.result = Lte().alterCellIdentity(param.result as CellIdentityLte)
                        }
                        is CellIdentityNr -> {
                            XposedBridge.log("FL: [Shaomi R] Using Nr Network...")
                            param.result = Nr().alterCellIdentity(param.result as CellIdentityNr)
                        }
                        else -> {
                            XposedBridge.log("FL: [Shaomi R] Unsupported network type. Return null as fallback")
                            param.result = null
                        }
                    }
                }
            } catch (e: Exception) {
                XposedBridge.log("FL: [Shaomi R] Manually workaround for possibly invalid class...")
            }
        }

        findAllMethods(clazz) {
            name == "getBlurryCellInfos" && isPublic && parameterCount == 3
        }.hookAfter { param ->
            val packageName = param.args[2] as String
            XposedBridge.log("FL: [Shaomi R] in getBlurryCellInfos! Caller packageName: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: [Shaomi R] in whiteList! Return empty CellInfos for testing purpose.")
                val customAllCellInfo = ArrayList<CellInfo>()
                param.result = customAllCellInfo
            }
        }

        findAllMethods(clazz) {
            name == "blurLastGpsLocation" && isPublic
        }.hookAfter { param ->
            val packageName = ConfigGateway.get().callerIdentityToPackageName(param.args[2])
            XposedBridge.log("FL: [Shaomi R] in blurLastGpsLocation! Caller packageName: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: [Shaomi R] in whitelist! Return custom location")
                val fakeLocation = ConfigGateway.get().readFakeLocation()

                lateinit var location: Location
                lateinit var originLocation: Location

                if (param.result == null) {
                    location = Location(LocationManager.GPS_PROVIDER)
                    location.time = System.currentTimeMillis() - (100..10000).random()
                } else {
                    originLocation = param.result as Location
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
                param.result = location
            }
        }

        findAllMethods(clazz) {
            name == "handleGpsLocationChangedLocked" && isPublic
        }.hookBefore { param ->
            val packageName = ConfigGateway.get().callerIdentityToPackageName(param.args[1])
            XposedBridge.log("FL: [Shaomi R] in handleGpsLocationChangedLocked! Caller packageName: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: [Shaomi R] in whiteList! Dropping update request for testing purpose...")
                param.result = null
                return@hookBefore
            }
        }
    }
}