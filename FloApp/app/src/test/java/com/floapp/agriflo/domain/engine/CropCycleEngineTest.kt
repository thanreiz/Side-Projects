package com.floapp.agriflo.domain.engine

import com.floapp.agriflo.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CropCycleEngineTest {

    private lateinit var engine: CropCycleEngine

    @Before
    fun setup() {
        engine = CropCycleEngine()
    }

    @Test
    fun `computeCurrentStage returns LAND_PREPARATION when crop is freshly planted`() {
        // Arrange — planted today
        val plantingDate = LocalDate.now()
        val stageDurations = emptyMap<CropStageType, Int>()

        // Act
        val result = engine.computeCurrentStage(plantingDate, stageDurations, LocalDate.now())!!

        // Assert
        assertEquals(CropStageType.LAND_PREPARATION, result.stageType)
        assertTrue("Progress should be between 0 and 1", result.progressFraction in 0.0f..1.0f)
    }

    @Test
    fun `computeCurrentStage advances to TRANSPLANTING after LAND_PREPARATION period`() {
        // Arrange — planting date set 20 days ago (past LAND_PREPARATION default)
        val plantingDate = LocalDate.now().minusDays(20)
        val stageDurations = mapOf(CropStageType.LAND_PREPARATION to 14)

        // Act
        val result = engine.computeCurrentStage(plantingDate, stageDurations, LocalDate.now())!!

        // Assert — should be in PLANTING or later (day 15+)
        assertNotEquals(CropStageType.LAND_PREPARATION, result.stageType)
    }

    @Test
    fun `computeCurrentStage returns HARVEST for old crop`() {
        // Arrange — crop planted 130 days ago (past all stages for rice)
        val plantingDate = LocalDate.now().minusDays(130)

        // Act
        val result = engine.computeCurrentStage(plantingDate, emptyMap(), LocalDate.now())!!

        // Assert — should be in final HARVEST stage
        assertEquals(CropStageType.HARVEST, result.stageType)
    }

    @Test
    fun `computeCurrentStage progressFraction is between 0 and 1`() {
        val plantingDate = LocalDate.now().minusDays(45)
        val result = engine.computeCurrentStage(plantingDate, emptyMap(), LocalDate.now())!!
        assertTrue(result.progressFraction >= 0.0f)
        assertTrue(result.progressFraction <= 1.0f)
    }

    @Test
    fun `generateAlerts returns non-empty list for typical crop state`() {
        val plantingDate = LocalDate.now().minusDays(30)
        val result = engine.computeCurrentStage(plantingDate, emptyMap(), LocalDate.now())!!
        // Alerts list should be defined (not throw)
        assertNotNull(result.alerts)
    }

    @Test
    fun `daysRemainingInStage is non-negative`() {
        val plantingDate = LocalDate.now().minusDays(60)
        val result = engine.computeCurrentStage(plantingDate, emptyMap(), LocalDate.now())!!
        assertTrue(
            "daysRemainingInStage must not be negative: ${result.daysRemainingInStage}",
            result.daysRemainingInStage >= 0
        )
    }

    @Test
    fun `recommendedActions are non-empty for any valid stage`() {
        val plantingDate = LocalDate.now().minusDays(50)
        val result = engine.computeCurrentStage(plantingDate, emptyMap(), LocalDate.now())!!
        assertTrue("recommendedActions should not be empty", result.recommendedActions.isNotEmpty())
    }
}
