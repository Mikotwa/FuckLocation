package fuck.location.xposed.cellar.r

import android.app.AndroidAppHelper
import android.telephony.CellIdentity
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Gsm {
    fun HookCellIdentity(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
        val clazz: Class<*> = lpparam.classLoader.loadClass("android.telephony.CellIdentityGsm")
        XposedBridge.log("FL: Finding method in HookCellIdentity (GSM)")
        findAllMethods(clazz) {
            name == "getAdditionalPlmns" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getAdditionalPlmns! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getArfcn" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getArfcn! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getBsic" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getBsic! Caller package name: " + packageName)
                
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
            name == "getLac" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getLac! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getMcc" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getMcc! Caller package name: " + packageName)
                
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
            name == "getMnc" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getMnc! Caller package name: " + packageName)
                
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
            name == "getPsc" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getPsc! Caller package name: " + packageName)
                
            }
        }
    }
}