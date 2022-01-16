package fuck.location.xposed

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.net.wifi.WifiInfo
import android.os.Build
import com.github.kyuubiran.ezxhelper.utils.*
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WLANHooker {
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
                        val wifi_clazz = classloader.loadClass("com.android.server.wifi.WifiService")

                        findAllMethods(wifi_clazz) {
                            name == ""
                        }

                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions! $e")
                    }
                }
            }
        }
    }
}