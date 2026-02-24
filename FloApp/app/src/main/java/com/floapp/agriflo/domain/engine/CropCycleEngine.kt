package com.floapp.agriflo.domain.engine

import com.floapp.agriflo.domain.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Crop Cycle Engine — the biological brain of Flo.
 *
 * Computes the current crop stage, days remaining, progress fractions,
 * recommended actions, and alerts purely from planting date and stage durations.
 *
 * IMPORTANT: This class has ZERO Android framework dependencies.
 * It is fully unit-testable with plain JUnit.
 */
@Singleton
class CropCycleEngine @Inject constructor() {

    /**
     * Computes the current [CropStage] for a crop given its planting date,
     * stage duration map, and an observation date (defaults to today).
     *
     * Returns null if the current date is before the planting date.
     */
    fun computeCurrentStage(
        plantingDate: LocalDate,
        stageDurations: Map<CropStageType, Int>,
        observationDate: LocalDate = LocalDate.now()
    ): CropStage? {
        if (observationDate.isBefore(plantingDate)) return null

        val totalDaysElapsed = ChronoUnit.DAYS.between(plantingDate, observationDate).toInt()
        val orderedStages = CropStageType.values().toList()

        // Use provided durations or fall back to biological defaults
        val resolvedDurations = orderedStages.associateWith { stage ->
            stageDurations[stage] ?: stage.defaultDurationDays
        }
        val totalCycleDays = resolvedDurations.values.sum()

        var daysAccumulated = 0
        for (stage in orderedStages) {
            val stageDuration = resolvedDurations[stage] ?: stage.defaultDurationDays
            val stageEnd = daysAccumulated + stageDuration

            if (totalDaysElapsed < stageEnd || stage == orderedStages.last()) {
                val daysInStage = (totalDaysElapsed - daysAccumulated).coerceAtLeast(0)
                val daysRemaining = (stageEnd - totalDaysElapsed).coerceAtLeast(0)
                val progressInStage = (daysInStage.toFloat() / stageDuration).coerceIn(0f, 1f)
                val overallProgress = (totalDaysElapsed.toFloat() / totalCycleDays).coerceIn(0f, 1f)

                return CropStage(
                    stageType = stage,
                    daysInStage = daysInStage,
                    daysRemainingInStage = daysRemaining,
                    totalDaysFromPlanting = totalDaysElapsed,
                    progressFraction = progressInStage,
                    overallProgressFraction = overallProgress,
                    recommendedActions = stage.recommendedActions,
                    alerts = generateAlerts(
                        currentStage = stage,
                        daysRemainingInStage = daysRemaining,
                        totalDaysElapsed = totalDaysElapsed,
                        totalCycleDays = totalCycleDays
                    )
                )
            }
            daysAccumulated += stageDuration
        }
        return null
    }

    /**
     * Generates a list of [CropAlert] for a crop based on current stage context.
     * All alert logic is deterministic and offline.
     */
    fun generateAlerts(
        currentStage: CropStageType,
        daysRemainingInStage: Int,
        totalDaysElapsed: Int,
        totalCycleDays: Int
    ): List<CropAlert> {
        val alerts = mutableListOf<CropAlert>()

        // Upcoming stage change alert (within 3 days)
        if (daysRemainingInStage in 1..3 && currentStage != CropStageType.HARVEST) {
            alerts.add(
                CropAlert(
                    type = AlertType.UPCOMING_STAGE_CHANGE,
                    message = "Stage changing in $daysRemainingInStage day(s). Prepare for next activities.",
                    urgency = AlertUrgency.MEDIUM
                )
            )
        }

        // Fertilization window alert
        if (currentStage == CropStageType.FERTILIZATION_WINDOW) {
            if (daysRemainingInStage <= 5) {
                alerts.add(
                    CropAlert(
                        type = AlertType.FERTILIZATION_DUE,
                        message = "Fertilization window closing in $daysRemainingInStage day(s). Apply now if conditions allow.",
                        urgency = AlertUrgency.HIGH
                    )
                )
            } else {
                alerts.add(
                    CropAlert(
                        type = AlertType.FERTILIZATION_DUE,
                        message = "You are in the fertilization window. Check weather before applying.",
                        urgency = AlertUrgency.MEDIUM
                    )
                )
            }
        }

        // Harvest approaching
        if (currentStage == CropStageType.RIPENING && daysRemainingInStage <= 7) {
            alerts.add(
                CropAlert(
                    type = AlertType.HARVEST_APPROACHING,
                    message = "Harvest in approximately $daysRemainingInStage day(s). Prepare equipment.",
                    urgency = AlertUrgency.HIGH
                )
            )
        }
        if (currentStage == CropStageType.HARVEST) {
            alerts.add(
                CropAlert(
                    type = AlertType.HARVEST_APPROACHING,
                    message = "Your crop is ready for harvest! Log your harvest soon.",
                    urgency = AlertUrgency.HIGH
                )
            )
        }

        // Overdue stage (crop exceeded expected cycle duration)
        if (totalDaysElapsed > totalCycleDays + 7) {
            alerts.add(
                CropAlert(
                    type = AlertType.OVERDUE_STAGE,
                    message = "Crop cycle is overdue by ${totalDaysElapsed - totalCycleDays} days. Update your crop record.",
                    urgency = AlertUrgency.MEDIUM
                )
            )
        }

        return alerts
    }

    /**
     * Computes expected harvest date from planting date and stage durations.
     */
    fun computeExpectedHarvestDate(
        plantingDate: LocalDate,
        stageDurations: Map<CropStageType, Int>
    ): LocalDate {
        val totalDays = CropStageType.values().sumOf { stage ->
            stageDurations[stage] ?: stage.defaultDurationDays
        }
        return plantingDate.plusDays(totalDays.toLong())
    }

    /**
     * Returns default stage durations for a given [CropType].
     * These are calibrated for Philippine agricultural conditions.
     */
    fun getDefaultStageDurations(cropType: CropType): Map<CropStageType, Int> {
        return when (cropType) {
            CropType.RICE -> mapOf(
                CropStageType.LAND_PREPARATION to 14,
                CropStageType.PLANTING to 7,
                CropStageType.VEGETATIVE_GROWTH to 50,
                CropStageType.FERTILIZATION_WINDOW to 14,
                CropStageType.REPRODUCTIVE to 35,
                CropStageType.RIPENING to 20,
                CropStageType.HARVEST to 7
            )
            CropType.CORN -> mapOf(
                CropStageType.LAND_PREPARATION to 10,
                CropStageType.PLANTING to 5,
                CropStageType.VEGETATIVE_GROWTH to 40,
                CropStageType.FERTILIZATION_WINDOW to 10,
                CropStageType.REPRODUCTIVE to 25,
                CropStageType.RIPENING to 15,
                CropStageType.HARVEST to 7
            )
            CropType.VEGETABLE -> mapOf(
                CropStageType.LAND_PREPARATION to 7,
                CropStageType.PLANTING to 3,
                CropStageType.VEGETATIVE_GROWTH to 21,
                CropStageType.FERTILIZATION_WINDOW to 7,
                CropStageType.REPRODUCTIVE to 14,
                CropStageType.RIPENING to 7,
                CropStageType.HARVEST to 5
            )
            else -> CropStageType.values().associate { it to it.defaultDurationDays }
        }
    }
}
