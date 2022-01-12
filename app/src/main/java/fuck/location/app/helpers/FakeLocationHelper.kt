package fuck.location.app.helpers

import com.github.kyuubiran.ezxhelper.utils.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fuck.location.app.ui.models.FakeLocation
import java.io.File

import java.io.FileNotFoundException

class FakeLocationHelper {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonPath = "/data/data/fuck.location/"
    private var file = File(jsonPath + "fakeLocation.json")

    companion object {
        private var instance: FakeLocationHelper? = null
            get() {
                if (field == null) {
                    field = FakeLocationHelper()
                }
                return field
            }
        fun get(): FakeLocationHelper {
            return instance!!
        }
    }

    @ExperimentalStdlibApi
    fun writeFakeLocation(x: Double, y: Double) {
        val newFakeLocation = FakeLocation(x, y)
        val jsonAdapter: JsonAdapter<FakeLocation> = moshi.adapter<FakeLocation>()

        val json: String = jsonAdapter.toJson(newFakeLocation)
        file.writeText(json)

        Runtime.getRuntime().exec("su -c mkdir -pv /data/system/fuck_location_test/")
        Runtime.getRuntime().exec("su -c cp $jsonPath/fakeLocation.json /data/system/fuck_location_test/")
        Runtime.getRuntime().exec("su -c chmod 604 /data/system/fuck_location_test/*")
    }

    @ExperimentalStdlibApi
    fun readFakeLocation(): FakeLocation? {
        val jsonAdapter: JsonAdapter<FakeLocation> = moshi.adapter<FakeLocation>()

        var json: String = try {
            file.readText()
        } catch (e: FileNotFoundException) {
            Log.d("FL: fakeLocation.json not found. Trying to refresh File holder")
            try {
                file = File(jsonPath + "fakeLocation.json")
                file.readText()
                Log.d("FL: fakeLocation.json resumed.")
            } catch (e: FileNotFoundException) {
                Log.d("FL: not possible to refresh. Fallback to []")
                "[]"
            }
            "[]"
        }

        return jsonAdapter.fromJson(json)
    }
}