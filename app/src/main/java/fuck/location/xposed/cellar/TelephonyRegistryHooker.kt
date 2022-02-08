package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.*
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.info.Lte
import fuck.location.xposed.helpers.ConfigGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass

class TelephonyRegistryHooker {
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("PrivateApi")
    fun hookListen(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.TelephonyRegistry")

        XposedBridge.log("FL: [Cellar] Finding method in TelephonyRegistry")

        findAllMethods(clazz) {
            name == "validateEventAndUserLocked" && isPrivate
        }.hookBefore { param ->
            val record = param.args[0]
            val event = param.args[1] as Int

            val packageName = findField(record.javaClass) {
                name == "callingPackage"
            }.get(record) as String

            XposedBridge.log("FL: [Cellar] in validateEventAndUserLocked! Caller package name: $packageName")

            if (ConfigGateway.get().inWhitelist(packageName) && (event == 5 || event == 11)) {
                XposedBridge.log("FL: [Cellar] in whiteList! Do not report EVENT_CELL_LOCATION_CHANGED for now. $event")



                param.result = false
            }
        }

        findAllMethods(clazz) {
            name == "notifyCellInfoForSubscriber" && isPublic
        }.hookBefore { param ->
            XposedBridge.log("FL: [Cellar] in notifyCellInfoForSubscriber!")

            val mRecordsField = findField(clazz) {
                name == "mRecords"
            }

            val mRecords = mRecordsField.get(param.thisObject) as ArrayList<*>
            val newRecords = arrayListOf<Any>()

            mRecords.forEach { record ->
                val packageName = findField(record.javaClass) {
                    name == "callingPackage"
                }.get(record) as String

                if (!ConfigGateway.get().inWhitelist(packageName)) {
                    newRecords.add(record)
                } else {
                    XposedBridge.log("FL: [Cellar] Calling $packageName with custom result...")
                    val originResult = param.args[1]    // List<CellInfo> cellInfo
                    val customResult = arrayListOf<CellInfo>()

                    if (originResult != null) {
                        val cellInfoList = (originResult as List<*>)

                        if (cellInfoList.isNotEmpty()) {
                            when (val cellInfo = cellInfoList[0]) {
                                is CellInfoLte -> {
                                    XposedBridge.log("FL: [Cellar] is LTE Network...")
                                    customResult.add(Lte().constructNewCellInfoLte(cellInfo))
                                }
                                else -> {
                                    XposedBridge.log("FL: [Cellar] wtf with this? $cellInfo")
                                }
                            }

                            val callback = findField(record.javaClass) {
                                name == "callback"
                            }.get(record)

                            findMethod(callback.javaClass) {
                                name == "onCellInfoChanged"
                            }.invoke(callback, customResult)
                        }
                        else {
                            XposedBridge.log("FL: [Cellar] cellInfoList is empty? So do nothing")
                        }
                    }
                }
            }

            mRecordsField.set(param.thisObject, newRecords)
        }
    }
}