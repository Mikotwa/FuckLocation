package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import com.github.kyuubiran.ezxhelper.utils.*
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.ConfigGateway

class TelephonyRegistryHooker {
    @SuppressLint("PrivateApi")
    fun hookListen(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.TelephonyRegistry")

        XposedBridge.log("FL: [Cellar] Finding method in TelephonyRegistry")

        findAllMethods(clazz) {
            name == "listen" && isPrivate
        }.hookBefore { param ->
            val packageName = param.args[0] as String

            XposedBridge.log("FL: [Cellar] in listen! Caller package name: $packageName")
            if (ConfigGateway.get().inWhitelist(packageName)) {
                XposedBridge.log("FL: [Cellar] in whiteList! Do not notify now for returning custom info.")

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    param.args[3] = false
                } else {
                    param.args[4] = false
                }
            }
        }

        findAllMethods(clazz) {
            name == "notifyCellInfoForSubscriber" && isPublic
        }.hookBefore {
            XposedBridge.log("FL: [Cellar] in notifyCellInfoForSubscriber!")

            val mRecords = findField(clazz) {
                name == "mRecords"
            }.get(clazz) as ArrayList<*>

            mRecords.forEach { record ->
                findAllFields(record.javaClass) {
                    true
                }.forEach { field ->
                    XposedBridge.log("FL: [Cellar] $field")
                }
            }
        }
    }
}