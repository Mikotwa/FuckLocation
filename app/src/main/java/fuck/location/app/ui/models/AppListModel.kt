package fuck.location.app.ui.models

import android.graphics.drawable.Drawable
import com.idanatz.oneadapter.external.interfaces.Diffable
import kotlin.random.Random

class AppListModel(val title: String, val packageName: String, var icon: Drawable) : Diffable {
    private val id: Long = Random.Default.nextLong()

    override fun areContentTheSame(other: Any): Boolean {
        return other is AppListModel && title == other.title
    }

    override val uniqueIdentifier: Long = id;
}