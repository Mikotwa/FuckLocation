package fuck.location.xposed.cellar.r

import android.app.AndroidAppHelper
import android.telephony.CellIdentity
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Nr {
    fun HookCellIdentity(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = AndroidAppHelper.currentApplication().applicationContext.packageName
        val clazz: Class<*> = lpparam.classLoader.loadClass("android.telephony.CellIdentityNr")
        XposedBridge.log("FL: Finding method in HookCellIdentity (NR)")
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
            name == "getNci" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getNci! Caller package name: " + packageName)
                
            }
        }

        findAllMethods(clazz) {
            name == "getNrarfcn" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: in getNrarfcn! Caller package name: " + packageName)
                
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