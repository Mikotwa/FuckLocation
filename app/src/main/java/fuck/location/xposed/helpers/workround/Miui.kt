package fuck.location.xposed.helpers.workround

import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import fuck.location.xposed.helpers.ConfigGateway
import java.io.File
import java.io.FileInputStream
import java.util.*

/*
 * Such wow, very 💩 rom
 * Lots of incapable changes :(
 */

class Miui{
    fun isMIUI(): Boolean {
        val manufacturer = Build.MANUFACTURER

        return !TextUtils.isEmpty(manufacturer) && manufacturer == "Xiaomi"
    }
}