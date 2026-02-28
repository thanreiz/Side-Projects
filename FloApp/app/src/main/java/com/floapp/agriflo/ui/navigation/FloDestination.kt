package com.floapp.agriflo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class FloDestination(val route: String) {
    object Home : FloDestination("home")
    object CropWheel : FloDestination("crop_wheel/{cropId}") {
        fun createRoute(cropId: String) = "crop_wheel/$cropId"
    }
    object Logging : FloDestination("logging/{cropId}") {
        fun createRoute(cropId: String) = "logging/$cropId"
    }
    object Weather : FloDestination("weather")
    object HarvestForecast : FloDestination("harvest_forecast/{cropId}") {
        fun createRoute(cropId: String) = "harvest_forecast/$cropId"
    }
    object DigitalResibo : FloDestination("digital_resibo")
    object AIAssistant : FloDestination("ai_assistant")
    object LandData : FloDestination("land_data")
    object AddCrop : FloDestination("add_crop")
    object Settings : FloDestination("settings")
}

data class BottomNavItem(
    val destination: FloDestination,
    val icon: ImageVector,
    val labelEn: String,
    val labelTl: String
)

val bottomNavItems = listOf(
    BottomNavItem(FloDestination.DigitalResibo, Icons.Filled.Receipt, "Resibo", "Resibo"),
    BottomNavItem(FloDestination.Weather, Icons.Filled.Cloud, "Weather", "Panahon"),
    BottomNavItem(FloDestination.Home, Icons.Filled.Home, "Home", "Tahanan"),
    BottomNavItem(FloDestination.AIAssistant, Icons.Filled.SmartToy, "Flo AI", "Flo AI"),
    BottomNavItem(FloDestination.LandData, Icons.Filled.Landscape, "Land Data", "Lupain")
)
