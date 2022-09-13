package fuck.location.data.model.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import fuck.location.R

sealed class MainNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : MainNavItem(
        route = Main.Home.route,
        titleResId = R.string.nav_home_title,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
    )

    object Profile : MainNavItem(
        route = Main.Profile.route,
        titleResId = R.string.nav_profile_title,
        icon = Icons.Outlined.Assignment,
        selectedIcon = Icons.Filled.Assignment
    )

    object Setting : MainNavItem(
        route = Main.Setting.route,
        titleResId = R.string.nav_setting_title,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )
}