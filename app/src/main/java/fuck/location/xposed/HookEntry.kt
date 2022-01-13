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

import fuck.location.app.helpers.WhitelistPersistHelper
import fuck.location.app.helpers.FakeLocationHelper
import fuck.location.xposed.LocationHookerAfterR
import java.lang.Exception

@ExperimentalStdlibApi
class HookEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {
    val wph = WhitelistPersistHelper.get()
    val flhelper = FakeLocationHelper.get()

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
        XposedBridge.log("FL: in initZygote!")
    }

    @SuppressLint("PrivateApi", "ObsoleteSdkInt")
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
                        // For Android 12 and 11, run this hook
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S
                            || Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                            LocationHookerAfterR().hookLastLocation(lpparam)
                        } else {    // For Android 10 and earlier, run this fallback version
                            LocationHookerPreQ().hookLastLocation(lpparam)
                        }
                    } catch (e: Exception) {
                        XposedBridge.log("FL: fuck with exceptions: ${e.toString()}")
                    }
                }
            }
        }

    }
}