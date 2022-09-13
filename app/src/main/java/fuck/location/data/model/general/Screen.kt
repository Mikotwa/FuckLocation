package fuck.location.data.model.general

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object Scan : Screen("camera_scan")
}