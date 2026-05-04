package com.example.greeting.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
    object Preview : Screen("preview/{templateId}") {
        fun createRoute(templateId: String) = "preview/$templateId"
    }
    object Profile : Screen("profile")
}
