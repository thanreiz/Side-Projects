package com.floapp.agriflo.domain.model

import java.time.LocalDate

/**
 * Domain model representing a farmer's crop cycle.
 * This is a pure Kotlin class with no Android dependencies.
 */
data class Crop(
    val id: String,
    val name: String,
    val variety: String,
    val landAreaHa: Double,
    val plantingDate: LocalDate,
    val expectedHarvestDate: LocalDate? = null,   // null = computed from stage durations
    val stageDurations: Map<CropStageType, Int> = emptyMap(), // stage → days; empty = use engine defaults
    val cropType: CropType,
    val notes: String = "",
    val isActive: Boolean = true,
    val isSynced: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CropType(val displayName: String) {
    RICE("Palay (Rice)"),
    CORN("Mais (Corn)"),
    VEGETABLE("Gulay (Vegetable)"),
    FRUIT("Prutas (Fruit)"),
    SUGARCANE("Tubo (Sugarcane)"),
    CASSAVA("Kamoteng-kahoy (Cassava)")
}
