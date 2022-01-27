package fuck.location.xposed.cellar.legacy

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.helpers.WhitelistGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass

class PhoneInterfaceManagerHooker {
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("PrivateApi")
    fun HookCellLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.phone.PhoneInterfaceManager")

        XposedBridge.log("FL: [Cellar] Finding method in PhoneInterfaceManager")

        findAllMethods(clazz) {
            name == "getImeiForSlot" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: [Cellar] in getImeiForSlot! Caller package name: ${param.args[1]}")
                val customIMEI = "1234567891011120"

                param.result = customIMEI
                WhitelistGateway().inWhitelist(param.args[1] as String)
                XposedBridge.log("FL: return custom value for testing purpose: $customIMEI")
            }
        }

        findAllMethods(clazz) {
            name == "getMeidForSlot" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: [Cellar] in getMeidForSlot! Caller package name: ${param.args[1]}")
                val customMEID = "1234567891011120"

                param.result = customMEID
                WhitelistGateway().inWhitelist(param.args[1] as String)
                XposedBridge.log("FL: return custom value for testing purpose: $customMEID")
            }
        }

        findAllMethods(clazz) {
            name == "getCellLocation" && isPublic
        }.hookMethod {
            after { param ->
                XposedBridge.log("FL: [Cellar] in getCellLocation! Caller package name: ${param.args[0]}")

                if (WhitelistGateway().inWhitelist(param.args[1] as String)) { // TODO: Check whether in whiteList by ContentProvider
                    XposedBridge.log("FL: [Cellar] in whiteList! Return custom cell data information")

                    when (param.result) {
                        is CellIdentityCdma -> {
                            XposedBridge.log("FL: [Cellar] Using CDMA Network...")
                        }
                        is CellIdentityGsm -> {
                            XposedBridge.log("FL: [Cellar] Using GSM Network...")
                        }
                        is CellIdentityLte -> {
                            XposedBridge.log("FL: [Cellar] Using LTE Network...")

                            val constructor = HiddenApiBypass.getDeclaredConstructor(CellIdentityLte::class.java,
                                Int::class.java,    // ci
                                Int::class.java,    // pci
                                Int::class.java,    // tac
                                Int::class.java,    // earfcn
                                IntArray::class.java,  // bands
                                Int::class.java,    // bandwidth
                                String::class.java, // mccStr
                                String::class.java, // mncStr
                                String::class.java, // alphal
                                String::class.java, // alphas
                                Collection::class.java, // additionalPlmns
                                ClosedSubscriberGroupInfo::class.java,  // csgInfo
                            )

                            XposedBridge.log("FL: [Cellar] Seems to be success 1/3... $constructor")

                            val existedResult = param.result as CellIdentityLte
                            val customResult = constructor.newInstance(
                                existedResult.ci,
                                existedResult.pci,
                                existedResult.tac,
                                existedResult.earfcn,
                                existedResult.bands,
                                existedResult.bandwidth,
                                existedResult.mccString,
                                existedResult.mncString,
                                existedResult.operatorAlphaLong,
                                existedResult.operatorAlphaShort,
                                existedResult.additionalPlmns,
                                existedResult.closedSubscriberGroupInfo
                            )
                            XposedBridge.log("FL: [Cellar] Seems to be success 2/3... $customResult")

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
                XposedBridge.log("FL: [Cellar] in getAllCellInfo! Caller package name: ${param.args[0]}")

                val customAllCellInfo = ArrayList<CellInfo>()

                param.result = customAllCellInfo
                XposedBridge.log("FL: return empty AllCellInfo for testing purpose.")
            }
        }
    }
}