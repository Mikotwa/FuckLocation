package fuck.location.xposed.location.miui

import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MiuiBlurLocationManager {
    fun hookGetBlurryLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.location.MiuiBlurLocationManager")

        findAllMethods(clazz) {
            name == "getBlurryLocation" && isPublic
        }.hookBefore { param ->
            XposedBridge.log("FL: [Shaomi] uid: ${param.args[0]}, str: ${param.args[1]}")
        }
    }
}