package fuck.location.ui.component.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.fragment.app.FragmentManager
import fuck.location.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(fragmentManager: FragmentManager) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.nav_setting_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }, content = { innerPadding ->
            FragmentContainer(
                modifier = Modifier.padding(innerPadding),
                fragmentManager = fragmentManager,
                commit = {add(it, SettingsFragment())}
            )
        })
}

