package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPrivate
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class TelephonyRegistryHooker {
    @SuppressLint("PrivateApi")
    fun hookListen(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.TelephonyRegistry")

        XposedBridge.log("FL: [Cellar] Finding method in TelephonyRegistry")

        findAllMethods(clazz) {
            name == "listen" && isPrivate
        }.hookMethod {
            before { param ->
                val packageName = param.args[0] as String

                XposedBridge.log("FL: [Cellar] in listen! Caller package name: $packageName")
                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: [Cellar] in whiteList! Dropping listen request...")
                    param.result = null
                }
            }
        }
    }
}