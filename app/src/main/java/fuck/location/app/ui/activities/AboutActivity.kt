package fuck.location.app.ui.activities

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Card
import com.drakeet.about.Category
import com.drakeet.about.Contributor
import fuck.location.BuildConfig
import fuck.location.R
import com.drakeet.about.License




class AboutActivity: AbsAboutActivity() {
    @SuppressLint("SetTextI18n")    // version.txt doesn't need translation
    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(R.mipmap.ic_launcher)
        slogan.setText(R.string.about_slogan)
        version.text = "v" + BuildConfig.VERSION_NAME
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.add(Category(getString(R.string.about_introduction_title)))
        items.add(Card(getString(R.string.about_introduction_content)))

        items.add(Category(getString(R.string.about_legal_title)))
        items.add(Card(getString(R.string.about_legal_content)))

        items.add(Category(getString(R.string.about_developer_title)))
        items.add(Contributor(R.drawable.avatar_mogu, getString(R.string.about_developer_name_mogu), getString(R.string.about_developer_description_mogu)))

        items.add(Category(getString(R.string.about_opensource_title)))
        items.add(
            License(
                "MultiType",
                "drakeet",
                License.APACHE_2,
                "https://github.com/drakeet/MultiType"
            )
        )
        items.add(License(
                "about-page",
                "drakeet",
                License.APACHE_2,
                "https://github.com/drakeet/about-page"
            )
        )
        items.add(License("SmartRefreshLayout", "scwang90", License.APACHE_2, "https://github.com/scwang90/SmartRefreshLayout"))
        items.add(License("EzXHelper", "KyuubiRan", License.APACHE_2, "https://github.com/KyuubiRan/EzXHelper"))
        items.add(License("Moshi", "square", License.APACHE_2, "https://github.com/square/moshi"))
        items.add(License("OneAdapter", "idanatz", License.MIT, "https://github.com/ironSource/OneAdapter"))
        items.add(License("Material Dialogs", "afollestad", License.APACHE_2, "https://github.com/afollestad/material-dialogs/"))

    }
}