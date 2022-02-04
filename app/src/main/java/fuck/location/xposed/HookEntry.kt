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
import fuck.location.xposed.helpers.ConfigGateway
import fuck.location.xposed.location.LocationHookerAfterS
import fuck.location.xposed.location.LocationHookerPreQ
import fuck.location.xposed.location.LocationHookerR
import fuck.location.xposed.location.WLANHooker
import java.lang.Exception

@ExperimentalStdlibApi
class HookEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
        XposedBridge.log("FL: in initZygote!")
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

                        // For Android 12, run this hook
                        when (Build.VERSION.SDK_INT) {
                            Build.VERSION_CODES.S -> {
                                LocationHookerAfterS().hookLastLocation(lpparam)
                            }
                            Build.VERSION_CODES.R -> {  // Android 11
                                LocationHookerR().hookLastLocation(lpparam)
                            }
                            else -> {    // For Android 10 and earlier, run this fallback version
                                LocationHookerPreQ().hookLastLocation(lpparam)
                            }
                        }

                        WLANHooker().hookWifiManager(lpparam)
                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions: $e")
                    }
                }

                "com.android.phone" -> {
                    try {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S
                            || Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                            PhoneInterfaceManagerHooker().hookCellLocation(lpparam)
                        } else {    // For Android 10 and earlier, run this fallback version
                            XposedBridge.log( "FL: Custom cellar data info is currently not supported for Android 10 or below.")
                        }
                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions (cellar): $e")
                    }
                }
            }
        }

    }
}