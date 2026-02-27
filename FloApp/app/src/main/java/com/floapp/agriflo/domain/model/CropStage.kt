package com.floapp.agriflo.domain.model

/**
 * Represents the current biological stage of a crop cycle
 * as computed by [CropCycleEngine].
 *
 * Stages are sequential and biologically motivated — not calendar-based.
 */
enum class CropStageType(
    val displayNameEn: String,
    val defaultDurationDays: Int,
    val recommendedActions: List<String>
) {
    LAND_PREPARATION(
        displayNameEn = "Land Preparation",
        defaultDurationDays = 14,
        recommendedActions = listOf(
            "Plow and harrow the field",
            "Apply organic matter / compost",
            "Check soil pH (target 6.0–7.0)",
            "Level the field to retain water"
        )
    ),
    PLANTING(
        displayNameEn = "Planting / Seeding",
        defaultDurationDays = 7,
        recommendedActions = listOf(
            "Use certified seeds",
            "Proper spacing: 20cm x 20cm for rice",
            "Pre-germinate seeds 24–48 hours",
            "Log planting date immediately"
        )
    ),
    VEGETATIVE_GROWTH(
        displayNameEn = "Vegetative Growth",
        defaultDurationDays = 40,
        recommendedActions = listOf(
            "Apply first basal fertilizer (NPK)",
            "Monitor for early pest signs",
            "Maintain water level (rice: 5–7cm)",
            "Record any pest observations"
        )
    ),
    FERTILIZATION_WINDOW(
        displayNameEn = "Fertilization Window",
        defaultDurationDays = 14,
        recommendedActions = listOf(
            "Apply top-dressing fertilizer",
            "Check weather before applying (avoid rain)",
            "Photograph receipt for Digital Receipt",
            "Record quantity and product used"
        )
    ),
    REPRODUCTIVE(
        displayNameEn = "Reproductive Stage",
        defaultDurationDays = 30,
        recommendedActions = listOf(
            "Monitor for panicle blast (rice)",
            "Maintain irrigation during heading",
            "Avoid applying nitrogen fertilizer",
            "Watch for aphids and stem borers"
        )
    ),
    RIPENING(
        displayNameEn = "Ripening / Maturation",
        defaultDurationDays = 15,
        recommendedActions = listOf(
            "Drain field 2 weeks before harvest (rice)",
            "Check grain moisture content",
            "Prepare threshing equipment",
            "Record expected yield estimate"
        )
    ),
    HARVEST(
        displayNameEn = "Harvest Ready",
        defaultDurationDays = 7,
        recommendedActions = listOf(
            "Harvest at 20–25% grain moisture",
            "Log harvest weight in the app",
            "Store in sealed containers",
            "Generate harvest forecast report"
        )
    )
}

/**
 * Computed result from [CropCycleEngine] for a specific crop at a specific point in time.
 */
data class CropStage(
    val stageType: CropStageType,
    val daysInStage: Int,
    val daysRemainingInStage: Int,
    val totalDaysFromPlanting: Int,
    val progressFraction: Float,           // 0.0 to 1.0 within current stage
    val overallProgressFraction: Float,    // 0.0 to 1.0 across full crop cycle
    val recommendedActions: List<String>,
    val alerts: List<CropAlert>
)

data class CropAlert(
    val type: AlertType,
    val message: String,
    val urgency: AlertUrgency
)

enum class AlertType {
    UPCOMING_STAGE_CHANGE,
    FERTILIZATION_DUE,
    MISSED_LOG_REMINDER,
    HARVEST_APPROACHING,
    OVERDUE_STAGE
}

enum class AlertUrgency { LOW, MEDIUM, HIGH }
