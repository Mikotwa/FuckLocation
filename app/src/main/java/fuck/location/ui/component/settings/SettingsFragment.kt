package fuck.location.ui.component.settings

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.ui.text.intl.Locale.Companion.current
import androidx.compose.ui.text.intl.LocaleList.Companion.current
import androidx.core.text.HtmlCompat
import androidx.preference.PreferenceFragmentCompat

import fuck.location.R
import fuck.location.util.Locales
import rikka.material.app.LocaleDelegate
import rikka.preference.SimpleMenuPreference
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        findPreference<SimpleMenuPreference>("language")?.let {
            val entries = buildList {
                for (lang in Locales.LOCALES) {
                    if (lang == "SYSTEM") add(getString(rikka.core.R.string.follow_system))
                    else {
                        val locale = Locale.forLanguageTag(lang)
                        add(HtmlCompat.fromHtml(locale.getDisplayName(locale), HtmlCompat.FROM_HTML_MODE_LEGACY))
                    }
                }
            }
            it.entries = entries.toTypedArray()
            it.entryValues = Locales.LOCALES
            if (it.value == "SYSTEM") {
                it.summary = getString(rikka.core.R.string.follow_system)
            } else {
                val locale = Locale.forLanguageTag(it.value)
                //it.summary = if (!TextUtils.isEmpty(locale.script)) locale.getDisplayScript(userLocale) else locale.getDisplayName(userLocale)
            }
        }
    }
}