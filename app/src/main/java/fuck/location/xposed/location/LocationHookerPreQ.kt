package fuck.location.xposed.location

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
import java.io.File
import java.lang.Exception

class LocationHookerPreQ {
    @ExperimentalStdlibApi
    fun hookLastLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        var clazz = lpparam.classLoader.loadClass("android.location.LocationManager")

        findAllMethods(clazz) {
            name == "getLastLocation" && isPublic
        }.hookMethod {
            after {
                var packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
                XposedBridge.log("FL: in getLastLocation (pre Q)! Caller package name: $packageName");

                val jsonAdapterLocation: JsonAdapter<FakeLocation> = Moshi.Builder().add(
                    KotlinJsonAdapterFactory()
                ).build().adapter<FakeLocation>()

                // TODO: Provide flexible provider options
                val location = Location(LocationManager.GPS_PROVIDER)
                try {
                    if (ConfigGateway().inWhitelist(packageName)) {
                        XposedBridge.log("FL (pre Q): in whitelist! Return custom location")

                        val jsonFileLocation = File("/data/system/fuck_location_test/fakeLocation.json")
                        val fakeLocation = jsonAdapterLocation.fromJson(jsonFileLocation.readText().toString())

                        location.latitude = fakeLocation?.x!!
                        location.longitude = fakeLocation?.y!!
                        location.time = System.currentTimeMillis()
                        location.altitude = 0.0

                        XposedBridge.log("FL: x: ${location.latitude}, y: ${location.longitude}")
                        it.result = location
                    }
                } catch (e: Exception) {
                    XposedBridge.log("FL: fuck with exceptions! ${e.toString()}")
                }
            }
        }
    }
}