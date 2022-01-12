package fuck.location.app.helpers

import com.github.kyuubiran.ezxhelper.utils.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter

import java.io.File
import java.io.FileNotFoundException

class WhitelistPersistHelper private constructor(){
    private val moshi = Moshi.Builder().build()
    private val jsonPath = "/data/data/fuck.location/"
    private var file = File(jsonPath + "whiteList.json")

    // 单例，防止写入与读取过程出现竞争
    companion object {
        private var instance: WhitelistPersistHelper? = null
            get() {
                if (field == null) {
                    field = WhitelistPersistHelper()
                }
                return field
            }
        fun get(): WhitelistPersistHelper {
            return instance!!
        }
    }

    @ExperimentalStdlibApi
    fun writePackageList(list: List<String>) {
        val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter<List<String>>()
        val json: String = jsonAdapter.toJson(list)
        Log.d(json)
        file.writeText(json)

        Runtime.getRuntime().exec("su -c mkdir -pv /data/system/fuck_location_test/")
        Runtime.getRuntime().exec("su -c cp $jsonPath/whiteList.json /data/system/fuck_location_test/")
        Runtime.getRuntime().exec("su -c chmod 604 /data/system/fuck_location_test/*")
    }

    @ExperimentalStdlibApi
    fun readPackageList(): List<String>? {
        val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter<List<String>>()

        var json: String = try {
            file.readText()
        } catch (e: FileNotFoundException) {
            Log.d("FL: whiteList.json not found. Trying to refresh File holder")
            try {
                file = File(jsonPath + "whiteList.json")
                file.readText()
                Log.d("FL: whiteList.json resumed.")
            } catch (e: FileNotFoundException) {
                Log.d("FL: not possible to refresh. Fallback to []")
                "[]"
            }
            "[]"
        }

        return jsonAdapter.fromJson(json)
    }
}