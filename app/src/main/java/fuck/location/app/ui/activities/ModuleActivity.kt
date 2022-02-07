package fuck.location.app.ui.activities

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idanatz.oneadapter.OneAdapter
import com.idanatz.oneadapter.external.modules.ItemModule
import fuck.location.R
import fuck.location.databinding.ActivitySelectAppsBinding
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import com.github.kyuubiran.ezxhelper.utils.runOnMainThread
import com.idanatz.oneadapter.external.event_hooks.ClickEventHook
import com.idanatz.oneadapter.external.modules.EmptinessModule
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlin.concurrent.thread

import fuck.location.app.ui.models.AppListModel
import fuck.location.xposed.helpers.ConfigGateway
import java.util.stream.Collectors

@ExperimentalStdlibApi
class ModuleActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: RefreshLayout

    private lateinit var binding: ActivitySelectAppsBinding
    private lateinit var oneAdapter: OneAdapter
    private var packageInfos: List<AppListModel> = arrayListOf()   // Prevent from search crash

    private var searchKeyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConfigGateway.get().setCustomContext(applicationContext)

        binding = ActivitySelectAppsBinding.inflate(layoutInflater)
        recyclerView = binding.recycler
        oneAdapter = OneAdapter(recyclerView) {
            itemModules += AppListModule(this@ModuleActivity)
            emptinessModule = EmptyListModule()
        }

        recyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)

        refreshLayout = binding.refreshLayout
        refreshLayout.setOnRefreshListener { refresh() }.autoRefresh()

        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val searchView = menu?.findItem(R.id.menu_search)?.actionView as SearchView?
            ?: return super.onPrepareOptionsMenu(menu)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchKeyword = newText?.lowercase() ?: ""

                thread {
                    updateSearchResult(searchKeyword)
                }

                return true
            }
        })

        searchView.findViewById<View>(
            androidx.appcompat.R.id.search_edit_frame).layoutDirection = View.LAYOUT_DIRECTION_INHERIT

        return super.onPrepareOptionsMenu(menu)
    }

    private fun refresh() {
        thread {
            initAppListView()
            runOnMainThread { refreshLayout.finishRefresh() }
        }
    }

    private fun initAppListView() {
        updateInstalledPackages()
        updateSearchResult(searchKeyword)
    }

    private fun updateInstalledPackages() {
        val storedList = ConfigGateway.get().readPackageList()
        val checkCircle = AppCompatResources.getDrawable(this, R.drawable.baseline_check_circle_24)!!
        val displayNameComparator = ApplicationInfo.DisplayNameComparator(this.packageManager)

        packageInfos = this.packageManager.getInstalledPackages(0)
            .parallelStream().sorted { lhs, rhs ->
                if (storedList != null) {
                    val lChecked = storedList.contains(lhs.packageName)
                    val rChecked = storedList.contains(rhs.packageName)
                    when {
                        lChecked == rChecked ->
                            displayNameComparator.compare(lhs.applicationInfo, rhs.applicationInfo)
                        lChecked -> -1
                        else -> 1
                    }
                } else {
                    displayNameComparator.compare(lhs.applicationInfo, rhs.applicationInfo)
                }
            }.map {
                val packageName = it.applicationInfo.packageName
                val icon = if (storedList?.contains(packageName) == true) checkCircle else it.applicationInfo.loadIcon(packageManager)

                AppListModel(it.applicationInfo.loadLabel(packageManager).toString(),
                    packageName,
                    icon)
            }.collect(Collectors.toList())
    }

    private fun updateSearchResult(keyword: String) {

        val searchResult = if (keyword.isNotEmpty()) {
            packageInfos.parallelStream().filter {
                it.title.lowercase().contains(keyword)
            }.collect(Collectors.toList())
        } else {
            packageInfos
        }

        runOnMainThread {
            oneAdapter.setItems(searchResult)
        }
    }

    class AppListModule(context: Context) : ItemModule<AppListModel>() {
        init {
            val selectedAppsList: MutableList<String> = ConfigGateway.get().readPackageList() as MutableList<String>

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
                        model.icon = AppCompatResources.getDrawable(context, R.drawable.baseline_radio_button_unchecked_24)!!
                        selectedAppsList.remove(model.packageName)
                    } else {
                        icon.setImageResource(R.drawable.baseline_check_circle_24)
                        model.icon = AppCompatResources.getDrawable(context, R.drawable.baseline_check_circle_24)!!
                        selectedAppsList.add(model.packageName)
                    }

                    ConfigGateway.get().writePackageList(selectedAppsList.toList())
                }
            }
            onUnbind { model, viewBinder, metadata ->

            }
        }
    }

    class EmptyListModule : EmptinessModule() {
        init {
            config {
                layoutResource = R.layout.empty_app_list
            }
        }
    }
}