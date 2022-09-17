package fuck.location

import android.app.Application
import rikka.material.app.LocaleDelegate
import java.util.*

class App : Application() {
    @JvmField
    var isHooked = false

    fun getLocale(tag: String): Locale {
        return if (tag == "SYSTEM") LocaleDelegate.systemLocale
        else Locale.forLanguageTag(tag)
    }
}