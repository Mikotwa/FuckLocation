package fuck.location.xposed.location.miui

import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MIUIFusedLocationHooker {
    // TODO: Write actual code after prototype is verified
    fun hookFusedLocationService(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.xiaomi.location.fused.FusedLocationService")

        XposedBridge.log("FL: [MIUI] Finding method in FusedLocationService")

        val fusedLocationProvider = findField(clazz) {
            name == "a" && isPrivate
        }.get(clazz)

        val locationProviderManager = findField(fusedLocationProvider.javaClass) {
            name == "mManager"
        }.get(fusedLocationProvider)

        findAllMethods(locationProviderManager.javaClass) {
            true
        }.forEach { method ->
            XposedBridge.log("FL: [MIUI] Demo: ${method.name}")
        }
    }
}