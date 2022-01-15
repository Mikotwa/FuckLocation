package fuck.location.xposed

import android.annotation.SuppressLint
import android.net.wifi.WifiInfo
import android.os.Build
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class WLANHooker {
    //TODO: migrate to newer ConnectivityManager on S (deprecated)
    @SuppressLint("PrivateApi")
    fun HookWifiManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> = lpparam.classLoader.loadClass("com.android.server.wifi.WifiServiceImpl")
        findAllMethods(clazz) {
            name == "getConnectionInfo" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("in getConnectionInfo! caller: " + param.args[0])
            }
        }
    }
}