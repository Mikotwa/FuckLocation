package fuck.location.xposed.cellar.identity

import android.app.AndroidAppHelper
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Lte {
    fun HookCellIdentity(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
        val clazz: Class<*> = lpparam.classLoader.loadClass("android.telephony.CellIdentityLte")
        XposedBridge.log("FL: Finding method in HookCellIdentity (LTE)")
        findAllMethods(clazz) {
            name == "getAdditionalPlmns" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getAdditionalPlmns! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getBands" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getBands! Caller package name: " + packageName)
                
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
            name == "getEarfcn" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getEarfcn! Caller package name: " + packageName)
                
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
            name == "getPci" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getPci! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getTac" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getTac! Caller package name: " + packageName)
                
            }
        }
    }
}