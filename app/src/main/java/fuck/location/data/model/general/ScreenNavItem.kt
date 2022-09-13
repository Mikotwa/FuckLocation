package fuck.location.data.model.general

sealed class ScreenNavItem(
    val route: String,
) {
    object Main : ScreenNavItem(
        route = Screen.Main.route
    )
    object Scan : ScreenNavItem(
        route = Screen.Scan.route
    )
}