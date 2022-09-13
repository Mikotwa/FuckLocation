package io.github.mikotwa.yucklocation.hook.utils

import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi

class WifiHelper private constructor(){
    companion object {
        private var instance: WifiHelper? = null
            get() {
                if (field == null) {
                    field = WifiHelper()
                }
                return field
            }

        fun get(): WifiHelper {
            return instance!!
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun buildCustomWifiInfo(): WifiInfo {
        return WifiInfo.Builder()
            .setBssid("")
            .setSsid("Android-AP".toByteArray())
            .setRssi(-1)
            .setNetworkId(0)
            .build()
    }

}