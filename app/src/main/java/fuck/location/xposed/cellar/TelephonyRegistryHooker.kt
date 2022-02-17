package fuck.location.xposed.cellar

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import com.github.kyuubiran.ezxhelper.utils.*
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.identity.Lte
import fuck.location.xposed.cellar.identity.Nr
import fuck.location.xposed.helpers.ConfigGateway

class TelephonyRegistryHooker {
    @ExperimentalStdlibApi
    @SuppressLint("PrivateApi")
    fun hookListen(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz: Class<*> =
            lpparam.classLoader.loadClass("com.android.server.TelephonyRegistry")

        XposedBridge.log("FL: [Cellar] Finding method in TelephonyRegistry")

        findAllMethods(clazz) {
            name == "validateEventAndUserLocked" && isPrivate
        }.hookAfter { param ->
            val record = param.args[0]
            val event = param.args[1] as Int

            val packageName = findField(record.javaClass) {
                name == "callingPackage"
            }.get(record) as String

            XposedBridge.log("FL: [Cellar] in validateEventAndUserLocked! Caller package name: $packageName")

            val shouldReportOrigin = param.result as Boolean

            if (ConfigGateway.get().inWhitelist(packageName) && shouldReportOrigin) {
                val callBack = findField(record.javaClass) {
                    name == "callback"
                }.get(record)

                val phoneId = findField(record.javaClass) {
                    name == "phoneId"
                }.get(record)

                when (event) {
                    5 -> {
                        XposedBridge.log("FL: [Cellar] in whiteList! Alter EVENT_CELL_LOCATION_CHANGED for now.")

                        if (phoneId != null) {
                            val mCellIdentity = findField(param.thisObject.javaClass) {
                                name == "mCellIdentity"
                            }.get(param.thisObject)
                            if (mCellIdentity != null) {
                                if ((phoneId as Int) >= 0 && phoneId < (mCellIdentity as Array<*>).size) {
                                    val originalCellIdentity = mCellIdentity[phoneId]
                                    if (originalCellIdentity != null) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            when (originalCellIdentity) {
                                                is CellIdentityLte -> {
                                                    findMethod(callBack.javaClass) {
                                                        name == "onCellLocationChanged"
                                                    }.invoke(
                                                        callBack,
                                                        Lte().alterCellIdentity(originalCellIdentity)
                                                    )
                                                }
                                                is CellIdentityNr -> {
                                                    findMethod(callBack.javaClass) {
                                                        name == "onCellLocationChanged"
                                                    }.invoke(
                                                        callBack,
                                                        Nr().alterCellIdentity(originalCellIdentity)
                                                    )
                                                }
                                                else -> {
                                                    findMethod(callBack.javaClass) {
                                                        name == "onCellLocationChanged"
                                                    }.invoke(callBack, null)
                                                }
                                            }
                                        } else {    // Android 9 do not support 5G network
                                            when (originalCellIdentity) {
                                                is CellIdentityLte -> {
                                                    findMethod(callBack.javaClass) {
                                                        name == "onCellLocationChanged"
                                                    }.invoke(
                                                        callBack,
                                                        Lte().alterCellIdentity(originalCellIdentity)
                                                    )
                                                }
                                                else -> {
                                                    findMethod(callBack.javaClass) {
                                                        name == "onCellLocationChanged"
                                                    }.invoke(callBack, null)
                                                }
                                            }
                                        }
                                    } else {
                                        findMethod(callBack.javaClass) {
                                            name == "onCellLocationChanged"
                                        }.invoke(callBack, null)
                                    }
                                }
                            }
                        }

                        param.result = false
                    }

                    11 -> {
                        XposedBridge.log("FL: [Cellar] in whiteList! Alter EVENT_CELL_INFO_CHANGED for now.")

                        if (phoneId != null) {
                            val mCellInfo = findField(param.thisObject.javaClass) {
                                name == "mCellInfo"
                            }.get(param.thisObject)

                            if (mCellInfo != null) {
                                if ((phoneId as Int) >= 0 && phoneId < (mCellInfo as ArrayList<*>).size) {
                                    val originalCellInfoList = mCellInfo[phoneId]
                                    if (originalCellInfoList != null) {
                                        val modifiedCellInfoList = mutableListOf<CellInfo>()

                                        (originalCellInfoList as List<*>).forEach { cellInfo ->
                                            if (cellInfo != null) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    when (cellInfo) {
                                                        is CellInfoLte -> {
                                                            modifiedCellInfoList.add(
                                                                fuck.location.xposed.cellar.info.Lte()
                                                                    .constructNewCellInfoLte(cellInfo)
                                                            )
                                                        }
                                                        is CellInfoNr -> {
                                                            modifiedCellInfoList.add(
                                                                fuck.location.xposed.cellar.info.Nr()
                                                                    .constructNewCellInfoNr(cellInfo)
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    when (cellInfo) {
                                                        is CellInfoLte -> {
                                                            modifiedCellInfoList.add(
                                                                fuck.location.xposed.cellar.info.Lte()
                                                                    .constructNewCellInfoLte(cellInfo)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        findMethod(callBack.javaClass) {
                                            name == "onCellInfoChanged"
                                        }.invoke(callBack, modifiedCellInfoList)    // return cellInfo
                                    } else {
                                        findMethod(callBack.javaClass) {
                                            name == "onCellInfoChanged"
                                        }.invoke(callBack, null)
                                    }
                                }
                            } else {
                                findMethod(callBack.javaClass) {
                                    name == "onCellInfoChanged"
                                }.invoke(callBack, null)
                            }
                        }

                        param.result = false
                    }
                }
            }
        }

        // TODO: Potential breakage in stock behavior. May being used as a detection way
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
                }
            }

            mRecordsField.set(param.thisObject, newRecords)
        }
    }
}