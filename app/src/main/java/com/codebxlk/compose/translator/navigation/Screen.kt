package com.codebxlk.compose.translator.navigation

sealed class Screen(val route: String) {
    data object Home : Screen(route = "home_screen")
    data object Language : Screen(route = "language_screen")
}