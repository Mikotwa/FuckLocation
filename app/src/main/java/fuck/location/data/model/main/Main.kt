package fuck.location.data.model.main

sealed class Main(val route: String) {
    object Home : Main("home_view")
    object Profile : Main("profile_view")
    object Setting : Main("setting_view")
}