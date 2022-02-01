package fuck.location.xposed.location

import android.annotation.SuppressLint
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

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
                        val wifiClazz = classloader.loadClass("com.android.server.wifi.WifiServiceImpl")

                        findAllMethods(wifiClazz) {
                            name == "getScanResults" && isPublic
                        }.hookMethod {
                            after { param ->
                                val packageName = param.args[0] as String
                                XposedBridge.log("FL: In getScanResults with caller: $packageName")

                                if (ConfigGateway.get().inWhitelist(packageName)) {
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

                        findAllMethods(wifiClazz) {
                            name == "getConnectionInfo" && isPublic
                        }.hookMethod {
                            after { param ->
                                val packageName = param.args[0] as String
                                XposedBridge.log("FL: In getConnectionInfo with caller: $packageName")

                                if (ConfigGateway.get().inWhitelist(packageName)) {
                                    XposedBridge.log("FL: in whitelist! Return custom WiFi information")

                                    var customResult = WifiInfo.Builder()
                                        .setBssid("22:33:11:68:7e:3f")
                                        .setSsid("Android-AP".toByteArray())
                                        .setRssi(-1)
                                        .setNetworkId(0)
                                        .build()

                                    param.result = customResult
                                    XposedBridge.log("FL: BSSID: ${customResult.bssid}, SSID: ${customResult.ssid}")
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