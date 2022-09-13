package fuck.location.xposed.hook.utils

import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerE
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
class ConfigHelper private constructor(){
    private lateinit var dataDir: String

    companion object {
        private var instance: ConfigHelper? = null
            get() {
                if (field == null) {
                    field = ConfigHelper()
                }
                return field
            }

        fun get(): ConfigHelper {
            return instance!!
        }
    }

    fun addScopePackage(packageName: String) {
        val scopeList = getScoopList()
        scopeList!!.add(packageName)

        writeScopeList(scopeList)
    }

    fun removeScopePackage(packageName: String) {
        val scopeList = getScoopList()
        scopeList!!.remove(packageName)

        writeScopeList(scopeList)
    }

    fun isPackageInScope(packageName: String): Boolean {
        try {
            val list = getScoopList()

            return if (list != null) {
                if (packageName in list) {
                    loggerD(msg = "$packageName is existed in configs")
                    true
                } else {
                    loggerD(msg = "$packageName seems did not existed in configs")
                    false
                }
            } else {
                loggerE(msg = "Package list is null?! What")
                false
            }
        } catch (e: Exception) {
            loggerE(msg = "Some problem arouse in isPackageInScoop: $e")
            return false
        }
    }

    private fun getScoopList(): MutableList<String>? {
        val jsonFile = giveMeJsonFile()

        val jsonAdapterScope: JsonAdapter<List<String>> = Moshi.Builder().build().adapter()
        var scoopList: List<String>? = jsonAdapterScope.fromJson(jsonFile.readText())

        if (scoopList == null) {
            scoopList = mutableListOf()
        }

        return scoopList as MutableList<String>
    }

    private fun writeScopeList(list: List<String>) {
        val jsonFile = giveMeJsonFile()
        val jsonAdapterScope: JsonAdapter<List<String>> = Moshi.Builder().build().adapter()
        val json = jsonAdapterScope.toJson(list)

        jsonFile.writeText(json)
    }

    // 把获得配置文件引用的逻辑打包在一起
    private fun giveMeJsonFile(): File {
        val jsonFile = File("$dataDir/whiteList.json")

        if (!jsonFile.exists()) {
            val jsonFileDirectory = File("$dataDir/")
            jsonFileDirectory.mkdirs()

            try {
                jsonFile.createNewFile()
                jsonFile.writeText("[]")
            } catch (e: Exception) {
                loggerE(msg = "Cannot create config file! Please check: $e")
            }
        }

        return jsonFile
    }

    // 找到配置文件的路径，并设置
    fun setDataPath(){
        File("/data/system").list()?.forEach {  // Try to find the existing config
            if (it.equals("fuck_location_test")) {  // Migrate from older version
                val randomizedPath = "/data/system/fuck_location_${generateRandomAppendix()}"
                File("/data/system/$it").renameTo(File(randomizedPath))
                dataDir = randomizedPath
            } else if (it.startsWith("fuck_location")) {
                if (this::dataDir.isInitialized) File("/data/system/$it").deleteRecursively()
                else dataDir = "/data/system/$it"
            }
        }

        if (!this::dataDir.isInitialized) { // Not possible, we create a new config folder
            dataDir = "/data/system/fuck_location_${generateRandomAppendix()}"
        }
    }

    private fun generateRandomAppendix() : String {
        val chars = ('a'..'Z') + ('A'..'Z') + ('0'..'9')
        return List(16) { chars.random() }.joinToString("")
    }
}