package com.floapp.agriflo.domain.engine

import com.floapp.agriflo.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HarvestPredictionEngineTest {

    private lateinit var engine: HarvestPredictionEngine
    private lateinit var weatherInterpreter: WeatherInterpreter

    @Before
    fun setup() {
        weatherInterpreter = WeatherInterpreter()
        engine = HarvestPredictionEngine(weatherInterpreter)
    }

    private fun makeCrop(
        cropType: CropType = CropType.RICE,
        landArea: Double = 1.0
    ) = Crop(
        id = "test-crop-1",
        name = "Palay Test",
        variety = "NSIC Rc222",
        cropType = cropType,
        landAreaHa = landArea,
        plantingDate = LocalDate.now().minusDays(90)
    )

    private fun makeWeather(rainfall: Double = 5.0, tempMax: Double = 29.0) = WeatherData(
        date = "2026-02-24", tempMinC = 22.0, tempMaxC = tempMax,
        rainfallMm = rainfall, humidityPct = 70, windSpeedKph = 10.0,
        uvIndex = 5.0, advisory = WeatherAdvisory.OPTIMAL_CONDITIONS, advisoryDetail = ""
    )

    @Test
    fun `computeForecast returns non-null forecast`() {
        val crop = makeCrop()
        val result = engine.computeForecast(crop, 1500.0, listOf(makeWeather()))
        assertNotNull(result)
    }

    @Test
    fun `computeForecast cropId matches input crop`() {
        val crop = makeCrop()
        val result = engine.computeForecast(crop, 1500.0, listOf(makeWeather()))
        assertEquals("test-crop-1", result.cropId)
    }

    @Test
    fun `projectedYieldKg is positive for 1ha rice`() {
        val crop = makeCrop(CropType.RICE, 1.0)
        val result = engine.computeForecast(crop, 0.0, listOf(makeWeather()))
        assertTrue("Yield must be positive: ${result.projectedYieldKg}", result.projectedYieldKg > 0)
    }

    @Test
    fun `projectedYieldKg scales with land area`() {
        val crop1ha = makeCrop(landArea = 1.0)
        val crop2ha = makeCrop(landArea = 2.0)
        val weather = listOf(makeWeather())
        val yield1 = engine.computeForecast(crop1ha, 0.0, weather).projectedYieldKg
        val yield2 = engine.computeForecast(crop2ha, 0.0, weather).projectedYieldKg
        assertTrue("2ha should yield roughly double 1ha: $yield1 vs $yield2", yield2 > yield1 * 1.5)
    }

    @Test
    fun `netProfitPhp equals revenue minus cost`() {
        val crop = makeCrop()
        val result = engine.computeForecast(crop, 2000.0, listOf(makeWeather()))
        val expectedNet = result.projectedRevenuePhp - result.projectedCostPhp
        assertEquals(expectedNet, result.netProfitPhp, 0.01)
    }

    @Test
    fun `riskScore is between 0 and 1`() {
        val crop = makeCrop()
        val result = engine.computeForecast(crop, 1500.0, listOf(makeWeather()))
        assertTrue(result.riskScore >= 0.0)
        assertTrue(result.riskScore <= 1.0)
    }

    @Test
    fun `riskLabel is HIGH when riskScore is above 0_66`() {
        val crop = makeCrop()
        val stormyWeather = List(7) {
            makeWeather(rainfall = 50.0, tempMax = 40.0)
        }
        val result = engine.computeForecast(crop, 0.0, stormyWeather)
        if (result.riskScore > 0.66) {
            assertEquals(RiskLabel.HIGH, result.riskLabel)
        }
    }

    @Test
    fun `assumptions contain correct crop type and land area`() {
        val crop = makeCrop(CropType.CORN, 0.75)
        val result = engine.computeForecast(crop, 1000.0, emptyList())
        assertEquals(CropType.CORN, result.assumptions.cropType)
        assertEquals(0.75, result.assumptions.landAreaHa, 0.001)
    }

    @Test
    fun `generatedAt timestamp is recent`() {
        val crop = makeCrop()
        val before = System.currentTimeMillis() - 5000
        val result = engine.computeForecast(crop, 1500.0, listOf(makeWeather()))
        assertTrue("Forecast timestamp should be recent: ${result.generatedAt}", result.generatedAt >= before)
    }
}
