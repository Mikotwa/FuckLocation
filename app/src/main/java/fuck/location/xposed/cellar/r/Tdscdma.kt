package fuck.location.xposed.cellar.r

import android.app.AndroidAppHelper
import android.telephony.CellIdentity
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Tdscdma {
    fun HookCellIdentity(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
        val clazz: Class<*> = lpparam.classLoader.loadClass("android.telephony.CellIdentityTdscdma")
        XposedBridge.log("FL: Finding method in HookCellIdentity (TDSCDMA)")
        findAllMethods(clazz) {
            name == "getAdditionalPlmns" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getAdditionalPlmns! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getCid" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getCid! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getClosedSubscriberGroupInfo" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getClosedSubscriberGroupInfo! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getCpid" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getCpid! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getLac" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getLac! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getMccString" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getMccString! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getMncString" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getMncString! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getMobileNetworkOperator" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getMobileNetworkOperator! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getUarfcn" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getUarfcn! Caller package name: " + packageName)
                
            }
        }
    }
}