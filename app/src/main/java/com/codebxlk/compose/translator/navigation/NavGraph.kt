package com.codebxlk.compose.translator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.codebxlk.compose.translator.ui.screen.HomeScreen
import com.codebxlk.compose.translator.ui.screen.LanguageScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController, startDestination) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(
            route = "${Screen.Language.route}/{selectedType}",
            arguments = listOf(navArgument("selectedType") { type = NavType.StringType })
        ) {
            it.arguments?.getString("selectedType")?.let { selectedType ->
                LanguageScreen(
                    navController = navController,
                    selectedType = selectedType
                )
            }
        }
    }
}