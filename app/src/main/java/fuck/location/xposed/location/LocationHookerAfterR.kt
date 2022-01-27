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
                XposedBridge.log("FL: in getLastLocation! Caller package name: " + it.args[1]);

                val jsonAdapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter<List<String>>()
                val jsonAdapterLocation: JsonAdapter<FakeLocation> = Moshi.Builder().add(
                    KotlinJsonAdapterFactory()
                ).build().adapter<FakeLocation>()
                val jsonFile: File

                // TODO: Provide flexible provider options
                val location = Location(LocationManager.GPS_PROVIDER)
                try {
                    jsonFile = File("/data/system/fuck_location_test/whiteList.json")
                    val list = jsonAdapter.fromJson(jsonFile.readText().toString())

                    if (list != null) {
                        for (name in list) {
                            if (it.args[1].toString().contains(name)) {
                                XposedBridge.log("FL: in whitelist! Return custom location")

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
                        }
                    }
                } catch (e: Exception) {
                    XposedBridge.log("FL: fuck with exceptions! ${e.toString()}")
                }
            }
        }
    }
}