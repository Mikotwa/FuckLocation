package fuck.location.xposed

import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.callbacks.XC_LoadPackage

class registerLocationHooker {
    fun hookRegisterLocationListener(lpparam: XC_LoadPackage.LoadPackageParam) {
        var clazz = lpparam.classLoader.loadClass("com.android.server.location.LocationManagerService")

        findAllMethods(clazz) {
            name == "registerLocationListener" && isPublic
        }.hookMethod {

        }
    }
}