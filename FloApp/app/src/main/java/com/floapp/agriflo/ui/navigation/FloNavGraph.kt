package com.floapp.agriflo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.floapp.agriflo.ui.screens.*

@Composable
fun FloNavGraph() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.labelEn) },
                        label = { Text("${item.labelTl}\n${item.labelEn}",
                            style = MaterialTheme.typography.labelSmall) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.destination.route } == true,
                        onClick = {
                            navController.navigate(item.destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FloDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(FloDestination.Home.route) {
                HomeScreen(
                    onCropSelected = { cropId ->
                        navController.navigate(FloDestination.CropWheel.createRoute(cropId))
                    },
                    onAddCrop = { navController.navigate(FloDestination.AddCrop.route) }
                )
            }
            composable(FloDestination.AddCrop.route) {
                AddCropScreen(onCropCreated = { navController.popBackStack() })
            }
            composable(FloDestination.CropWheel.route) { backStack ->
                val cropId = backStack.arguments?.getString("cropId") ?: return@composable
                CropWheelScreen(
                    cropId = cropId,
                    onLogActivity = { navController.navigate(FloDestination.Logging.createRoute(cropId)) },
                    onViewForecast = { navController.navigate(FloDestination.HarvestForecast.createRoute(cropId)) }
                )
            }
            composable(FloDestination.Logging.route) { backStack ->
                val cropId = backStack.arguments?.getString("cropId") ?: return@composable
                LoggingScreen(cropId = cropId, onBack = { navController.popBackStack() })
            }
            composable(FloDestination.Weather.route) {
                WeatherScreen()
            }
            composable(FloDestination.HarvestForecast.route) { backStack ->
                val cropId = backStack.arguments?.getString("cropId") ?: return@composable
                HarvestForecastScreen(cropId = cropId, onBack = { navController.popBackStack() })
            }
            composable(FloDestination.DigitalResibo.route) {
                DigitalResiboScreen()
            }
            composable(FloDestination.AIAssistant.route) {
                AIAssistantScreen()
            }
        }
    }
}
