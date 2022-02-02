package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPrivate
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.identity.Gsm
import fuck.location.xposed.cellar.identity.Lte
import fuck.location.xposed.helpers.ConfigGateway

class PhoneInterfaceManagerHooker {
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("PrivateApi")
    fun hookCellLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.phone.PhoneInterfaceManager")

        XposedBridge.log("FL: [Cellar] Finding method in PhoneInterfaceManager")

        findAllMethods(clazz) {
            name == "getImeiForSlot" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[1] as String
                val customIMEI = "1234567891011120" // TODO: Support custom IMEI information

                XposedBridge.log("FL: [Cellar] in getImeiForSlot! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    param.result = customIMEI
                    ConfigGateway.get().inWhitelist(param.args[1] as String)
                    XposedBridge.log("FL: [Cellar] In whiteList! Return custom value for testing purpose: $customIMEI")
                }
            }
        }

        findAllMethods(clazz) {
            name == "getMeidForSlot" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[1] as String
                val customMEID = "1234567891011120" // TODO: Support custom MEID information

                XposedBridge.log("FL: [Cellar] in getMeidForSlot! Caller package name: $packageName")
                if (ConfigGateway.get().inWhitelist(packageName)) {
                    param.result = customMEID
                    ConfigGateway.get().inWhitelist(param.args[1] as String)
                    XposedBridge.log("FL: [Cellar] In whiteList! Return custom value for testing purpose: $customMEID")
                }
            }
        }

        findAllMethods(clazz) {
            name == "getCellLocation" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[0] as String
                XposedBridge.log("FL: [Cellar] in getCellLocation! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: [Cellar] in whiteList! Return custom cell data information")

                    when (param.result) {
                        is CellIdentityCdma -> {
                            XposedBridge.log("FL: [Cellar] Using CDMA Network...")
                        }
                        is CellIdentityGsm -> {
                            XposedBridge.log("FL: [Cellar] Using GSM Network...")
                            param.result = Gsm().HookCellIdentity(param)
                            return@after
                        }
                        is CellIdentityLte -> {
                            XposedBridge.log("FL: [Cellar] Using LTE Network...")
                            param.result = Lte().hookCellIdentity(param)
                            return@after
                        }
                        is CellIdentityTdscdma -> {
                            XposedBridge.log("FL: [Cellar] Using TDSCDMA Network...")
                        }
                        is CellIdentityWcdma -> {
                            XposedBridge.log("FL: [Cellar] Using WCDMA Network...")
                        }
                    }

                    // Android 9 does not have this network type
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && param.result is CellIdentityNr) {
                        XposedBridge.log("FL: [Cellar] Using NR Network...")
                    }
                } else {
                    XposedBridge.log("FL: [Cellar] Not in whitelist...")
                }
            }
        }

        findAllMethods(clazz) {
            name == "getAllCellInfo" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[0] as String
                XposedBridge.log("FL: [Cellar] in getAllCellInfo! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: [Cellar] in whiteList! Return empty AllCellInfo for testing purpose.")
                    val customAllCellInfo = ArrayList<CellInfo>()
                    param.result = customAllCellInfo
                }
            }
        }

        findAllMethods(clazz) {
            name == "getNeighboringCellInfo" && isPublic
        }.hookMethod {
            after { param ->
                val packageName = param.args[0] as String
                XposedBridge.log("FL: [Cellar] in getNeighboringCellInfo! Caller package name: $packageName")

                if (ConfigGateway.get().inWhitelist(packageName)) {
                    XposedBridge.log("FL: [Cellar] in whiteList! Return empty NeighboringCellInfo for testing purpose.")
                    val customNeighboringCellInfo = ArrayList<NeighboringCellInfo>()
                    param.result = customNeighboringCellInfo
                }
            }
        }
    }
}