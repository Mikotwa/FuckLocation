package fuck.location.xposed

import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CellarHookerAfterR(){
    fun HookPhoneInterfaceManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> = lpparam.classLoader.loadClass("com.android.phone.PhoneInterfaceManager")
        findAllMethods(clazz) {
            name == "getCellLocation" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getCellLocation! Caller package name: " + param.args[0])
            }
        }
    }
}