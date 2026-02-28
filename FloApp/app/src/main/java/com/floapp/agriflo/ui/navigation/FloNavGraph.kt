package com.floapp.agriflo.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.floapp.agriflo.ui.screens.*
import com.floapp.agriflo.ui.theme.LocalLanguage
import com.floapp.agriflo.ui.theme.strings
import com.floapp.agriflo.ui.viewmodel.AppViewModel

/**
 * Root navigation graph.
 *
 * Language state flow:
 *   LanguageRepositoryImpl (singleton MutableStateFlow)
 *     → AppViewModel.language (StateFlow, Activity-scoped)
 *     → collectAsStateWithLifecycle()   ← recomposes this function on every emit
 *     → CompositionLocalProvider        ← pushes value to the ENTIRE subtree
 *     → every tab reads LocalLanguage.current and recomposes simultaneously
 *
 * Because [hiltViewModel()] is called here at the NavGraph root (inside MainActivity),
 * AppViewModel lives for the lifetime of the Activity — it survives all tab switches.
 */
@Composable
fun FloNavGraph(
    appViewModel: AppViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // ── Single source of truth collected from StateFlow ───────────────────────
    // Any call to languageRepository.setLanguage() (e.g. from SettingsViewModel)
    // emits a new value here, causing this composable AND its entire subtree
    // (all tab screens) to recompose with the new language on the very next frame.
    val language by appViewModel.language.collectAsStateWithLifecycle()

    // Hide the bottom bar on non-root destinations
    val showBottomBar = currentDestination?.route !in listOf(
        FloDestination.Settings.route,
        FloDestination.AddCrop.route,
        FloDestination.CropWheel.route,
        FloDestination.Logging.route,
        FloDestination.HarvestForecast.route,
    )

    CompositionLocalProvider(LocalLanguage provides language) {

        val str = language.strings()

        val navLabels = mapOf(
            FloDestination.Home.route          to str.navHome,
            FloDestination.Weather.route       to str.navWeather,
            FloDestination.DigitalResibo.route to str.navResibo,
            FloDestination.AIAssistant.route   to str.navAI,
            FloDestination.LandData.route      to str.navLandData
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = navLabels[item.destination.route]
                                    )
                                },
                                label = {
                                    Text(
                                        navLabels[item.destination.route] ?: item.labelEn,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                selected = currentDestination
                                    ?.hierarchy
                                    ?.any { it.route == item.destination.route } == true,
                                onClick = {
                                    navController.navigate(item.destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
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
                        onAddCrop      = { navController.navigate(FloDestination.AddCrop.route) },
                        onOpenSettings = { navController.navigate(FloDestination.Settings.route) }
                    )
                }
                composable(FloDestination.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(FloDestination.AddCrop.route) {
                    AddCropScreen(onCropCreated = { navController.popBackStack() })
                }
                composable(FloDestination.CropWheel.route) { backStack ->
                    val cropId = backStack.arguments?.getString("cropId") ?: return@composable
                    CropWheelScreen(
                        cropId = cropId,
                        onLogActivity  = { navController.navigate(FloDestination.Logging.createRoute(cropId)) },
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
                composable(FloDestination.LandData.route) {
                    LandDataScreen()
                }
            }
        }
    }
}
