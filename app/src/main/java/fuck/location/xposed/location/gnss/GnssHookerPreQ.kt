package fuck.location.xposed.location.gnss

import android.annotation.SuppressLint
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.isPrivate
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class GnssHookerPreQ {
    @SuppressLint("PrivateApi")
    fun hookAddGnssBatchingCallback(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz =
            lpparam.classLoader.loadClass("com.android.server.LocationManagerService")

        findAllMethods(clazz) {
            name == "addGnssBatchingCallback" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssBatchingCallback (Pre Q)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = false
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "addGnssDataListener" && isPrivate
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssDataListener (Pre Q)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = false
                return@hookBefore
            }
        }

        // Anything below is for Android 9
        findAllMethods(clazz) {
            name == "addGnssMeasurementsListener" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssMeasurementsListener (Pre Q)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = false
                return@hookBefore
            }
        }

        findAllMethods(clazz) {
            name == "addGnssNavigationMessageListener" && isPublic
        }.hookBefore { param ->
            val packageName = param.args[1] as String
            XposedBridge.log("FL: in addGnssNavigationMessageListener (Pre Q)! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: in whiteList! Dropping register request...")
                param.result = false
                return@hookBefore
            }
        }
    }
}