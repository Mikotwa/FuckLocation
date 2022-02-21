package fuck.location.xposed.location.miui

import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class FusedLocationHooker {
    fun hookFusedLocationService(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.xiaomi.location.fused.FusedLocationService")

        XposedBridge.log("FL: [MIUI] Finding method in FusedLocationService")

        findAllMethods(clazz) {
            name == "onBind" && isPublic
        }.hookAfter { param ->
            val fusedLocationProvider = findField(clazz) {
                name == "a" && isPrivate
            }.get(param.thisObject)

            findMethod(fusedLocationProvider.javaClass) {
                name == "reportLocation"
            }.hookBefore { param ->
                XposedBridge.log("FL: [MIUI] Demo: Stop reporting fused location")
                param.result = null
                return@hookBefore
            }
        }
    }
}