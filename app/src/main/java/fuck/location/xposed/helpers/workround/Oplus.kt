package fuck.location.xposed.helpers.workround

import android.os.Build
import android.text.TextUtils

/*
 * Pretty good, clean ROM :)
 */

class Oplus {
    fun isOplus(): Boolean {
        val manufacturer = Build.MANUFACTURER

        return !TextUtils.isEmpty(manufacturer) && manufacturer == "oplus"
    }
}