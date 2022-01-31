package fuck.location.xposed.location

import android.location.Location
import android.location.LocationManager
import android.os.Build
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
import fuck.location.xposed.helpers.WhitelistGateway
import java.io.File
import java.lang.Exception

class LocationHookerAfterR {

    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        var clazz: Class<*> = when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.S -> lpparam.classLoader.loadClass("com.android.server.location.provider.LocationProviderManager")
            Build.VERSION_CODES.R -> lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")
            else -> lpparam.classLoader.loadClass("android.location.LocationManager")
        }

        // TODO: Upgrade to modern WhitelistGateway
        findAllMethods(clazz) {
            name == "getLastLocation" && isPublic
        }.hookMethod {
            after {
                try {
                    val packageName = WhitelistGateway().callerIdentityToPackageName(it.args[1])
                    XposedBridge.log("FL: in getLastLocation! Caller package name: $packageName")

                    if (WhitelistGateway().inWhitelist(packageName)) {
                        XposedBridge.log("FL: in whitelist! Return custom location")
                        val jsonAdapterLocation: JsonAdapter<FakeLocation> = Moshi.Builder().add(
                            KotlinJsonAdapterFactory()
                        ).build().adapter<FakeLocation>()
                        val location = Location(LocationManager.GPS_PROVIDER)

                        val jsonFileLocation = File("/data/system/fuck_location_test/fakeLocation.json")
                        val fakeLocation = jsonAdapterLocation.fromJson(jsonFileLocation.readText().toString())

                        location.latitude = fakeLocation?.x!!
                        location.longitude = fakeLocation?.y!!
                        location.time = System.currentTimeMillis()
                        location.accuracy = 22.1F
                        location.altitude = 0.0

                        XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                        it.result = location
                    }
                } catch (e: Exception) {
                    XposedBridge.log("FL: Fuck with exceptions! $e");
                }
            }
        }

        findAllMethods(clazz) {
            name == "registerLocationRequest" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in registerLocationRequest!")
            }
        }
    }
}