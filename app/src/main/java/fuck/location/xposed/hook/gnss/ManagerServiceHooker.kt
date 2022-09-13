package fuck.location.xposed.hook.gnss

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import fuck.location.xposed.hook.utils.ConfigHelper
import io.github.mikotwa.yucklocation.hook.utils.PackageNameHelper

object ManagerServiceHooker : YukiBaseHooker(){
    private val GnssManagerServiceClass = VariousClass(
        "com.android.server.location.gnss.GnssManagerService"
    )

    private val GnssAntennaInfoClass = VariousClass(
        "com.android.server.location.gnss.GnssAntennaInfoProvider"
    )

    private val GnssMeasurementProviderClass = VariousClass(
        "com.android.server.location.gnss"
    )

    private val GnssNavigationMessageProviderClass = VariousClass(
        "com.android.server.location.gnss.GnssNavigationMessageProvider"
    )

    private val GnssMeasurementRequestClass = VariousClass(
        "android.location.GnssMeasurementRequest"
    )

    private val GnssNmeaProviderClass = VariousClass(
        "com.android.server.location.gnss.GnssNmeaProvider"
    )

    private val IGnssAntennaInfoListenerClass = VariousClass(
        "android.location.IGnssAntennaInfoListener"
    )

    private val IGnssStatusListenerClass = VariousClass(
        "android.location.IGnssStatusListener"
    )

    private val IGnssNavigationMessageListenerClass = VariousClass(
        "android.location.IGnssNavigationMessageListener"
    )

    private val IGnssMeasurementListener = VariousClass(
        "android.location.IGnssMeasurementsListener"
    )

    private val IGnssNmeaListenerListener = VariousClass(
        "android.location.IGnssNmeaListener"
    )

    private val CallerIdentityClass = VariousClass(
        "android.location.util.identity.CallerIdentity"
    )

    override fun onHook() {
        GnssAntennaInfoClass.hook {
            injectMember {
                method {
                    name = "addListener"
                    param(CallerIdentityClass, IGnssAntennaInfoListenerClass)
                }
                beforeHook {
                    val packageName = PackageNameHelper().callerIdentityToPackageName(args[0])

                    loggerD(msg = "in addListener (GnssAntennaInfo)! Caller package name: $packageName")
                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "in scope. Drop the request...")

                        result = null
                        return@beforeHook
                    }
                }
            }
        }

        GnssMeasurementProviderClass.hook {
            injectMember {
                method {
                    name = "addListener"
                    param(GnssMeasurementRequestClass, CallerIdentityClass, IGnssMeasurementListener)
                }
                beforeHook {
                    val packageName = PackageNameHelper().callerIdentityToPackageName(args[1])

                    loggerD(msg = "in addListener (GnssMeasurementProvider)! Caller package name: $packageName")
                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "in scope. Drop the request...")

                        result = null
                        return@beforeHook
                    }
                }
            }
        }

        GnssNavigationMessageProviderClass.hook {
            injectMember {
                method {
                    name = "addListener"
                    param(CallerIdentityClass, IGnssNavigationMessageListenerClass)
                }
                beforeHook {
                    val packageName = PackageNameHelper().callerIdentityToPackageName(args[0])

                    loggerD(msg = "in addListener (GnssNavigationMessageProvider)! Caller package name: $packageName")
                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "in scope. Drop the request...")

                        result = null
                        return@beforeHook
                    }
                }
            }
        }

        GnssNmeaProviderClass.hook {
            injectMember {
                method {
                    name = "addListener"
                    param(CallerIdentityClass, IGnssNmeaListenerListener)
                }
            }
        }
    }
}