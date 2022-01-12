package fuck.location.xposed

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Build

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XCallback

import fuck.location.app.helpers.WhitelistPersistHelper
import fuck.location.app.helpers.FakeLocationHelper
import fuck.location.app.ui.models.FakeLocation

import java.io.File
import java.lang.Exception

@ExperimentalStdlibApi
class HookEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {
    val wph = WhitelistPersistHelper.get()
    val flhelper = FakeLocationHelper.get()

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
        XposedBridge.log("FL: in initZygote!")
    }

    @SuppressLint("PrivateApi", "ObsoleteSdkInt")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam != null) {
            when (lpparam.packageName) {
                "android" -> {
                    EzXHelperInit.initHandleLoadPackage(lpparam)
                    EzXHelperInit.setLogTag("FuckLocation Xposed")
                    EzXHelperInit.setToastTag("FL")

                    XposedBridge.log("FL: Finding method")

                    // Deal with multiple Android version
                    var clazz: Class<*> = when (Build.VERSION.SDK_INT) {
                        Build.VERSION_CODES.S -> lpparam.classLoader.loadClass("com.android.server.location.provider.LocationProviderManager")
                        Build.VERSION_CODES.R -> lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")
                        else -> lpparam.classLoader.loadClass("android.location.LocationManager")
                    }

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
        }

    }
}