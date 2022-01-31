package fuck.location.xposed.helpers

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AndroidAppHelper
import android.content.Context
import com.github.kyuubiran.ezxhelper.utils.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.app.ui.models.FakeLocation
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.File
import java.io.FileNotFoundException
import java.lang.IllegalArgumentException
import java.lang.reflect.Field

/*
 * This hook acts as a gateway from phone to framework
 * in order to read the config file
 */

class ConfigGateway private constructor(){
    // Magic number to identify whether this call is from our module
    private val magicNumber = -114514
    private val magicNumberLocation = -191931

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private lateinit var customContext: Context

    /* For getting started in framework. In default, it judges whether a
     * packageName is in whiteList.json or not.
     *
     * param.args[2] determines what this function will actually do:
     * 0: input: packageName; output: true / false (in whiteList or not)
     * 1: input: jsonString; output: void (writePackageList)
     * 2: input: void; output: jsonString (readPackageList)
     * 3: input: jsonString; output: void (writeFakeLocation)
     * 4: input: void; output: jsonString (readFakeLocation)
     */

    // 单例，防止写入与读取过程出现竞争
    companion object {
        // TODO: Memory leak
        private var instance: ConfigGateway? = null
            get() {
                if (field == null) {
                    field = ConfigGateway()
                }
                return field
            }
        fun get(): ConfigGateway {
            return instance!!
        }
    }

    @ExperimentalStdlibApi
    @SuppressLint("PrivateApi")
    fun hookWillChangeBeEnabled(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.am.ActivityManagerService")

        XposedBridge.log("FL: [debug !!] Finding method")
        findAllMethods(clazz) {
            name == "setProcessMemoryTrimLevel" && isPublic
        }.hookMethod {
            before { param ->
                if (param.args[1] == magicNumber) {
                    when {  // Check what this call intend to do
                        param.args[2] == 0 -> {
                            XposedBridge.log("FL: [debug !!] Calling method inWhitelistOrNot")
                            inWhitelistOrNot(param)
                        }
                        param.args[2] == 1 -> {
                            XposedBridge.log("FL: [debug !!] Calling method writePackageListInternal")
                            writePackageListInternal(param)
                        }
                        param.args[2] == 3 -> {
                            XposedBridge.log("FL: [debug !!] Calling method writeFakeLocationInternal")
                            writeFakeLocationInternal(param)
                            return@before
                        }
                    }

                    return@before
                } else {
                    XposedBridge.log("FL: [debug !!] Not with magic number, do nothing.")
                }
            }
        }
    }

    @SuppressLint("PrivateApi")
    @ExperimentalStdlibApi
    fun hookGetTagForIntentSender(lpparam: XC_LoadPackage.LoadPackageParam) {
        val clazz = lpparam.classLoader.loadClass("com.android.server.pm.PackageManagerService")

        XposedBridge.log("FL: [debug !!] Finding method in getDefaultBrowserPackageNameAsUser")
        findAllMethods(clazz) {
            name == "getInstallerPackageName"
        }.hookMethod {
            before { param ->
                when {
                    param.args[0] == magicNumber.toString() -> {
                        XposedBridge.log("FL: [debug !!] Calling method readPackageListInternal")
                        readPackageListInternal(param)
                    }
                    param.args[0] == magicNumberLocation.toString() -> {
                        XposedBridge.log("FL: [debug !!] Calling method readFakeLocationInternal")
                        readFakeLocationInternal(param)
                    }
                }
                return@before
            }
        }
    }

    @ExperimentalStdlibApi
    private fun inWhitelistOrNot(param: XC_MethodHook.MethodHookParam) {
        val packageName = param.args[0]

        val jsonAdapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter()
        val jsonFile = File("/data/system/fuck_location_test/whiteList.json")

        val list = jsonAdapter.fromJson(jsonFile.readText())

        for (name in list!!) {
            if (packageName.toString().contains(name)) {
                param.result = true
                return
            }
        }

        param.result = false    // Block from calling real method
    }

    @ExperimentalStdlibApi
    private fun readPackageListInternal(param: XC_MethodHook.MethodHookParam) {
        var jsonFile = File("/data/system/fuck_location_test/whiteList.json")

        val json: String = try {
            jsonFile.readText()
        } catch (e: FileNotFoundException) {
            Log.d("FL: whiteList.json not found. Trying to refresh File holder")
            try {
                jsonFile = File("/data/system/fuck_location_test/whiteList.json")
                jsonFile.readText()
                Log.d("FL: whiteList.json resumed.")
            } catch (e: FileNotFoundException) {
                Log.d("FL: not possible to refresh. Fallback to []")
            }
            "[]"
        }

        param.result = json
    }

    @ExperimentalStdlibApi
    private fun readFakeLocationInternal(param: XC_MethodHook.MethodHookParam) {
        var jsonFile = File("/data/system/fuck_location_test/fakeLocation.json")

        val json: String = try {
            jsonFile.readText()
        } catch (e: FileNotFoundException) {
            Log.d("FL: fakeLocation.json not found. Trying to refresh File holder")
            try {
                jsonFile = File("/data/system/fuck_location_test/fakeLocation.json")
                jsonFile.readText()
                Log.d("FL: fakeLocation.json resumed.")
            } catch (e: FileNotFoundException) {
                Log.d("FL: not possible to refresh. Fallback to {\"x\":0.0, \"y\":0.0}")
            }
            "{\"x\":0.0, \"y\":0.0}"
        }

        param.result = json
    }

    private fun writePackageListInternal(param: XC_MethodHook.MethodHookParam) {
        val jsonFile = File("/data/system/fuck_location_test/whiteList.json")
        jsonFile.writeText(param.args[0] as String)

        param.result = false    // Block from calling real method
    }

    private fun writeFakeLocationInternal(param: XC_MethodHook.MethodHookParam) {
        val jsonFile = File("/data/system/fuck_location_test/fakeLocation.json")
        jsonFile.writeText(param.args[0] as String)

        param.result = false    // Block from calling real method
    }

    private fun universalAPICaller(string: String, action: Int): Any? {
        val magicContext: Context = try {
            AndroidAppHelper.currentApplication().applicationContext // Calling from xposed hook
        } catch (e: NoClassDefFoundError) {
            customContext   // Calling from normal code
        }

        val activityManager =
            magicContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageManager =
            magicContext.packageManager

        return when (action) {
            2 -> HiddenApiBypass.invoke(
                packageManager.javaClass,
                packageManager,
                "getInstallerPackageName", magicNumber.toString()
            )
            4 -> HiddenApiBypass.invoke(
                packageManager.javaClass,
                packageManager,
                "getInstallerPackageName", magicNumberLocation.toString()
            )
            else -> HiddenApiBypass.invoke(
                activityManager.javaClass,
                activityManager,
                "setProcessMemoryTrimLevel", string, magicNumber, action
            )
        }
    }

    // For caller outside of framework
    @SuppressLint("PrivateApi")
    fun inWhitelist(packageName: String): Boolean {
        return universalAPICaller(packageName, 0) as Boolean
    }

    @ExperimentalStdlibApi
    fun readPackageList(): List<String>? {
        val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter()
        val json = universalAPICaller("null", 2) as String

        return jsonAdapter.fromJson(json)

    }

    @ExperimentalStdlibApi
    fun readFakeLocation(): FakeLocation? {
        val jsonAdapter: JsonAdapter<FakeLocation> = moshi.adapter()
        val json = universalAPICaller("null", 4) as String

        return jsonAdapter.fromJson(json)
    }

    @ExperimentalStdlibApi
    fun writePackageList(list: List<String>) {
        val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter()
        val json: String = jsonAdapter.toJson(list)

        universalAPICaller(json, 1)
    }

    @ExperimentalStdlibApi
    fun writeFakeLocation(x: Double, y: Double) {
        val newFakeLocation = FakeLocation(x, y)
        val jsonAdapter: JsonAdapter<FakeLocation> = moshi.adapter()

        val json: String = jsonAdapter.toJson(newFakeLocation)
        universalAPICaller(json, 3)
    }

    fun setCustomContext(context: Context) {
        customContext = context
    }

    // For converting CallerIdentity to packageName
    fun callerIdentityToPackageName(callerIdentity: Any): String {
        val fields = HiddenApiBypass.getInstanceFields(callerIdentity.javaClass)

        for (field in fields) {
            // TODO: Change this fu**ing stupid check
            if (field.toString() == "private final java.lang.String android.location.util.identity.CallerIdentity.mPackageName") {
                val targetField = field as Field
                targetField.isAccessible = true
                return targetField.get(callerIdentity) as String
            }
        }

        // Should always found a valid packageName. If not, we throw an exception.
        throw IllegalArgumentException("FL: Invalid CallerIdentity! This should never happen, please report to developer.")
    }
}