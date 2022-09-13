package fuck.location.ui.component.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fuck.location.Greeting
import fuck.location.R
import fuck.location.data.model.main.MainNavItem
import fuck.location.ui.component.home.Home
import fuck.location.ui.component.settings.Settings

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationSetup(navController: NavHostController) {
    // TODO: Reuse duplicate codebase.
    NavHost(navController, startDestination = MainNavItem.Home.route) {
        composable(MainNavItem.Home.route) {
            Home()
        }
        composable(MainNavItem.Profile.route) {
            Scaffold(topBar = {
                TopAppBar(title = { Text(stringResource(id = R.string.nav_profile_title)) })
            }) {
                Column(modifier = Modifier.padding(it)) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Greeting(name = stringResource(id = R.string.nav_profile_title))
                }
            }
        }
        composable(MainNavItem.Setting.route) {
            Settings()
        }
    }
}