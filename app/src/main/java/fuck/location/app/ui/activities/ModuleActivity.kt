package fuck.location.app.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idanatz.oneadapter.OneAdapter
import com.idanatz.oneadapter.external.modules.ItemModule
import fuck.location.R
import fuck.location.databinding.ActivitySelectAppsBinding
import android.widget.ImageView
import com.github.kyuubiran.ezxhelper.utils.runOnMainThread
import com.idanatz.oneadapter.external.event_hooks.ClickEventHook
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlin.concurrent.thread

import fuck.location.app.ui.models.AppListModel
import fuck.location.app.helpers.WhitelistPersistHelper
import fuck.location.xposed.helpers.ConfigGateway

@ExperimentalStdlibApi
class ModuleActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: RefreshLayout

    private lateinit var binding: ActivitySelectAppsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectAppsBinding.inflate(layoutInflater)
        refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener { refresh() }.autoRefresh()

        setContentView(binding.root)
    }

    private fun refresh() {
        thread {
            initAppListView()
            runOnMainThread { refreshLayout.finishRefresh() }
        }
    }

    private fun initAppListView() {
        recyclerView = binding.recycler
        val storedList = ConfigGateway().readPackageList()

        val packageInfos = this.packageManager.getInstalledPackages(0)

        var list = packageInfos.map {
            AppListModel(it.applicationInfo.loadLabel(packageManager).toString(),
                it.applicationInfo.packageName,
                it.applicationInfo.loadIcon(packageManager))
        }.toList()

        list.forEach { unit ->
            if (storedList != null) {
                if (storedList.contains(unit.packageName)) unit.icon = getDrawable(R.drawable.baseline_check_circle_24)!!
            }
        }

        runOnMainThread {
            val oneAdapter = OneAdapter(recyclerView) {
                itemModules += AppListModule()
            }
            oneAdapter.setItems(list)

            recyclerView = binding.recycler
            recyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    class AppListModule : ItemModule<AppListModel>() {
        init {
            val selectedAppsList: MutableList<String> = ConfigGateway().readPackageList() as MutableList<String>

            config {
                layoutResource = R.layout.app_list_model
            }
            onBind { model, viewBinder, metadata ->
                val title = viewBinder.findViewById<TextView>(R.id.app_list_module_title)
                val icon = viewBinder.findViewById<ImageView>(R.id.app_list_module_icon)
                title.text = model.title
                icon.setImageDrawable(model.icon)
            }
            eventHooks += ClickEventHook<AppListModel>().apply {
                onClick { model, viewBinder, metadata ->
                    val icon = viewBinder.findViewById<ImageView>(R.id.app_list_module_icon)
                    if (selectedAppsList.contains(model.packageName)) {
                        icon.setImageResource(R.drawable.baseline_radio_button_unchecked_24)
                        selectedAppsList.remove(model.packageName)
                    } else {
                        icon.setImageResource(R.drawable.baseline_check_circle_24)
                        selectedAppsList.add(model.packageName)
                    }

                    ConfigGateway().writePackageList(selectedAppsList.toList())
                }
            }
            onUnbind { model, viewBinder, metadata ->

            }
        }
    }
}