package fuck.location.xposed.location.gnss

import android.annotation.SuppressLint
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class GnssManagerServiceHookerR {
    @SuppressLint("PrivateApi")
    fun hookAddGnssBatchingCallback(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz =
            lpparam.classLoader.loadClass("com.android.server.location.gnss.GnssManagerService")

        findAllMethods(clazz) {
            name == "addGnssBatchingCallback" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssBatchingCallback (R)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "registerGnssStatusCallback" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in registerGnssStatusCallback (R)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "addGnssMeasurementsListener" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[2] as String
            XposedBridge.log("FL: in addGnssMeasurementsListener (R)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "addGnssNavigationMessageListener" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssNavigationMessageListener (R)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "addGnssAntennaInfoListener" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssAntennaInfoListener (R)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = null
                return@hookBefore
            }
        }
    }
}