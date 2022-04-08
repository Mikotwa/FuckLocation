package fuck.location.xposed

import android.annotation.SuppressLint
import android.os.Build
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.BuildConfig

import fuck.location.xposed.cellar.PhoneInterfaceManagerHooker
import fuck.location.xposed.cellar.TelephonyRegistryHooker
import fuck.location.xposed.helpers.ConfigGateway
import fuck.location.xposed.helpers.workround.Miui
import fuck.location.xposed.helpers.workround.Oplus
import fuck.location.xposed.location.LocationHookerAfterS
import fuck.location.xposed.location.LocationHookerPreQ
import fuck.location.xposed.location.LocationHookerR
import fuck.location.xposed.location.WLANHooker
import fuck.location.xposed.location.gnss.GnssHookerPreQ
import fuck.location.xposed.location.gnss.GnssManagerServiceHookerR
import fuck.location.xposed.location.gnss.GnssManagerServiceHookerS
import fuck.location.xposed.location.miui.MiuiBlurLocationManagerHookerR
import fuck.location.xposed.location.miui.MiuiBlurLocationManagerHookerS
import fuck.location.xposed.location.oplus.NlpDLCS
import java.lang.Exception

@ExperimentalStdlibApi
class HookEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
        XposedBridge.log("FL: in initZygote!")

        ConfigGateway.get().setDataPath()
    }

    @SuppressLint("PrivateApi", "ObsoleteSdkInt", "NewApi")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam != null) {
            when (lpparam.packageName) {
                BuildConfig.APPLICATION_ID -> {
                    XposedBridge.log("FL: Try to hook the module")
                    val clazz = lpparam.classLoader.loadClass("fuck.location.app.ui.activities.MainActivity")

                    findAllMethods(clazz) {
                        name == "isModuleActivated" && isPublic
                    }.hookMethod {
                        after { param ->
                            XposedBridge.log("FL: Unlock the module")
                            param.result = true
                        }
                    }
                }

                "android" -> {
                    EzXHelperInit.initHandleLoadPackage(lpparam)
                    EzXHelperInit.setLogTag("FuckLocation Xposed")
                    EzXHelperInit.setToastTag("FL")

                    XposedBridge.log("FL: Finding method")

                    try {
                        // Initialize gateway
                        ConfigGateway.get().hookWillChangeBeEnabled(lpparam)
                        ConfigGateway.get().hookGetTagForIntentSender(lpparam)

                        TelephonyRegistryHooker().hookListen(lpparam)

                        // For Android 12 and MIUI, run this hook
                        when (Build.VERSION.SDK_INT) {
                            Build.VERSION_CODES.S -> {
                                if (Miui().isMIUI()) {
                                    MiuiBlurLocationManagerHookerS().hookGetBlurryLocationS(lpparam)
                                } else if (Oplus().isOplus()) {
                                    NlpDLCS().hookColorOS(lpparam)
                                }
                                LocationHookerAfterS().hookLastLocation(lpparam)
                                LocationHookerAfterS().hookDLC(lpparam)

                                GnssManagerServiceHookerS().hookRegisterGnssNmeaCallback(lpparam)
                            }
                            Build.VERSION_CODES.R -> {  // Android 11 and MIUI
                                if (Miui().isMIUI()) {
                                    MiuiBlurLocationManagerHookerR().hookGetBlurryLocation(lpparam)
                                }

                                LocationHookerR().hookLastLocation(lpparam)
                                LocationHookerR().hookDLC(lpparam)

                                GnssManagerServiceHookerR().hookAddGnssBatchingCallback(lpparam)
                            }
                            else -> {    // For Android 10 and earlier, run this fallback version
                                LocationHookerPreQ().hookLastLocation(lpparam)

                                GnssHookerPreQ().hookAddGnssBatchingCallback(lpparam)
                            }
                        }

                        WLANHooker().hookWifiManager(lpparam)
                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions: $e")
                    }
                }

                "com.android.phone" -> {
                    try {
                        PhoneInterfaceManagerHooker().hookCellLocation(lpparam)
                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions (cellar): $e")
                    }
                }
            }
        }

    }
}