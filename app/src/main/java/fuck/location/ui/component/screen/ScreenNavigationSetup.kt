package fuck.location.ui.component.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fuck.location.data.model.general.ScreenNavItem
import fuck.location.ui.component.main.MainNavigationBar
import fuck.location.ui.component.main.MainNavigationSetup

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenNavigationSetup(navController: NavHostController) {
    NavHost(navController = navController, startDestination = ScreenNavItem.Main.route) {
        composable(ScreenNavItem.Main.route) {
            val mainNavController = rememberNavController()

            Scaffold(
                bottomBar = { MainNavigationBar(navController = mainNavController) }
            ) {
                Column() {
                    MainNavigationSetup(navController = mainNavController)
                }
            }
        }
    }
}