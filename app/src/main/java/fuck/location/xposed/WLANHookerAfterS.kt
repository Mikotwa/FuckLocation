package fuck.location.xposed

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.net.wifi.WifiInfo
import android.os.Build
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WLANHooker {
    @SuppressLint("PrivateApi")
    fun HookWifiManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> = lpparam.classLoader.loadClass("com.android.server.SystemServiceManager")
        findAllMethods(clazz) {
            name == "startServiceFromJar" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in startServiceFromJar with service: " + param.args[0])
                if (param.args[0] == "com.android.server.wifi.WifiService") {
                    XposedBridge.log("FL: Awesome! We get the reference named: " + param.result.toString())
                }
            }
        }
    }
}