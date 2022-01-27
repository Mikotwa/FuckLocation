package fuck.location.xposed.helpers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AndroidAppHelper
import android.content.Context
import android.os.UserHandle
import com.github.kyuubiran.ezxhelper.utils.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.app.helpers.WhitelistPersistHelper
import fuck.location.app.ui.activities.MainActivity
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.File
import java.lang.Exception
import kotlin.properties.Delegates

/*
 * This hook acts as a gateway from phone to framework
 * in order to read the config file
 */

class WhitelistGateway {
    // Magic number to identify whether this call is from our module
    private val magicNumber = -114514
    lateinit var whitelistPersistHelper: WhitelistPersistHelper

    // For getting started in framework
    @ExperimentalStdlibApi
    @SuppressLint("PrivateApi")
    fun hookWillChangeBeEnabled(lpparam: XC_LoadPackage.LoadPackageParam) {
        var clazz = lpparam.classLoader.loadClass("com.android.server.am.ActivityManagerService")

        if (!this::whitelistPersistHelper.isInitialized) {
            whitelistPersistHelper = WhitelistPersistHelper.get()
        }

        XposedBridge.log("FL: [debug !!] Finding method")
        findAllMethods(clazz) {
            name == "setProcessMemoryTrimLevel" && isPublic
        }.hookMethod {
            before { param ->
                if (param.args[1] == magicNumber) {
                    val packageName = param.args[0]
                    XposedBridge.log("FL: [debug !!] Magic call received! Getting packageName: $packageName")

                    val jsonAdapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter<List<String>>()
                    val jsonFile = File("/data/system/fuck_location_test/whiteList.json")

                    val list = jsonAdapter.fromJson(jsonFile.readText())

                    for (name in list!!) {
                        if (packageName.toString().contains(name)) {
                            XposedBridge.log("FL: [debug !!] in whitelist!")
                            param.result = true
                            break
                        }
                    }

                    param.result = false;    // Block from calling real method
                } else {
                    XposedBridge.log("FL: [debug !!] Not with magic number, do nothing.")
                }
            }
        }
    }

    // For caller outside of framework
    @SuppressLint("PrivateApi")
    fun inWhitelist(packageName: String): Boolean {
        val magicContext = AndroidAppHelper.currentApplication().applicationContext
        val activityManager = magicContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        XposedBridge.log("FL: [debug !!] Calling magic method! $activityManager")

        val result = HiddenApiBypass.invoke(activityManager.javaClass,
            activityManager,
            "setProcessMemoryTrimLevel", packageName, magicNumber, 0) as Boolean

        XposedBridge.log("FL: [debug !!] Finished calling method!")
        return result
    }
}