package fuck.location.xposed.helpers.workround

import android.os.Build
import android.text.TextUtils

/*
 * Such wow, very 💩 rom
 * Lots of incapable changes :(
 */

class Miui {
    fun isMIUI(): Boolean {
        val manufacturer = Build.MANUFACTURER

        return !TextUtils.isEmpty(manufacturer) && manufacturer == "Xiaomi"
    }
}