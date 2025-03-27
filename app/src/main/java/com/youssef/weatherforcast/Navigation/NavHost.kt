package com.youssef.weatherforcast.Navigation

import SettingsViewModel
import SettingsViewModelFactory
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.youssef.weatherforcast.Favourite.FavoriteScreen
import com.youssef.weatherforcast.Favourite.FavoriteViewModel
import com.youssef.weatherforcast.Favourite.MapScreen
import com.youssef.weatherforcast.Home.HomeScreen
import com.youssef.weatherforcast.Home.HomeViewModel
import com.youssef.weatherforcast.Model.Repo
import com.youssef.weatherforcast.Setting.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    repo: Repo,
    homeViewModel: HomeViewModel,
    favoriteViewModel: FavoriteViewModel,
    settingsViewModel: SettingsViewModel
) {

    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Home.route) {
            HomeScreen(homeViewModel, settingsViewModel)
        }
        composable(Screen.Favourite.route) {
            FavoriteScreen(navController = navController, repo = repo,favoriteViewModel)
        }
        composable(Screen.Alerts.route) { AlertsScreen() }
        composable(Screen.Settings.route) {
            SettingsScreen(settingsViewModel) }
        composable(Screen.Map.route) {
            MapScreen(navController, repo)
        }
    }
}


sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Favourite : Screen("favourite")
    object Alerts : Screen("alerts")
    object Settings : Screen("settings")
    object Map : Screen("map_screen")

}