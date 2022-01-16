package fuck.location.xposed

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

class WLANHooker {
    @RequiresApi(Build.VERSION_CODES.R)
    @ExperimentalStdlibApi
    @SuppressLint("PrivateApi")
    fun HookWifiManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> = lpparam.classLoader.loadClass("com.android.server.SystemServiceManager")
        findAllMethods(clazz) {
            name == "loadClassFromLoader" && isPrivate && isStatic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in loadClassFromLoader with service: " + param.args[0])
                if (param.args[0] == "com.android.server.wifi.WifiService") {
                    XposedBridge.log("FL: Awesome! Now we are finding the REAL method...")
                    try {
                        val classloader = param.args[1] as PathClassLoader
                        val wifi_clazz = classloader.loadClass("com.android.server.wifi.WifiServiceImpl")

                        findAllMethods(wifi_clazz) {
                            name == "getScanResults" && isPublic
                        }.hookMethod {
                            after { param ->
                                XposedBridge.log("FL: In getScanResults with caller: " + param.args[0])

                                val jsonAdapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter<List<String>>()
                                val jsonFile = File("/data/system/fuck_location_test/whiteList.json")

                                val list = jsonAdapter.fromJson(jsonFile.readText())

                                for (name in list!!) {
                                    if (param.args[0].toString().contains(name)) {
                                        XposedBridge.log("FL: in whitelist! Return custom WiFi information")

                                        var customResult = ScanResult()
                                        customResult.BSSID = "22:33:11:68:7e:3f"
                                        customResult.SSID = "AndroidAP"
                                        customResult.capabilities = "WPA-2"
                                        customResult.level = -1

                                        val result: List<ScanResult> = listOf(customResult)
                                        param.result = result

                                        XposedBridge.log("FL: BSSID: ${customResult.BSSID}, SSID: ${customResult.SSID}")
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions! $e")
                    }
                }
            }
        }
    }
}