package fuck.location.xposed

import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPrivate
import com.github.kyuubiran.ezxhelper.utils.isStatic
import de.robv.android.xposed.callbacks.XC_LoadPackage

class CellarHooker(){
    fun HookPhoneInterfaceManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> = lpparam.classLoader.loadClass("com.android.phone.PhoneInterfaceManager")
        findAllMethods(clazz) {
            name == "loadClassFromLoader" && isPrivate && isStatic
        }.hookMethod {

        }
    }
}