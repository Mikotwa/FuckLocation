package fuck.location.xposed.cellar.legacy

import android.app.AndroidAppHelper
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class TelephonyManagerHooker {
    fun HookCellLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("android.telephony.TelephonyManager")
        XposedBridge.log("FL: [Cellar] Finding method in TelephonyManager")
        findAllMethods(clazz) {
            name == "getCellLocation" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
                XposedBridge.log("FL: [Cellar] in getCellLocation! Caller package name: $packageName")
            }
        }

        findAllMethods(clazz) {
            name == "getAllCellInfo" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
                XposedBridge.log("FL: [Cellar] in getAllCellInfo! Caller package name: $packageName")
            }
        }

        findAllMethods(clazz) {
            name == "getNeighboringCellInfo" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
                XposedBridge.log("FL: [Cellar] in getNeighboringCellInfo! Caller package name: $packageName")
            }
        }
    }
}