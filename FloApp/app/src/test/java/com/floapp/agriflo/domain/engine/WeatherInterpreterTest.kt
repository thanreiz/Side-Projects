package com.floapp.agriflo.domain.engine

import com.floapp.agriflo.domain.model.WeatherAdvisory
import com.floapp.agriflo.domain.model.WeatherData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeatherInterpreterTest {

    private lateinit var interpreter: WeatherInterpreter

    @Before
    fun setup() {
        interpreter = WeatherInterpreter()
    }

    private fun makeWeather(
        rainfall: Double = 0.0,
        tempMax: Double = 30.0,
        tempMin: Double = 24.0,
        wind: Double = 10.0,
        humidity: Int = 70,
        uvIndex: Double = 5.0
    ) = WeatherData(
        date = "2026-02-24",
        tempMinC = tempMin,
        tempMaxC = tempMax,
        rainfallMm = rainfall,
        humidityPct = humidity,
        windSpeedKph = wind,
        uvIndex = uvIndex,
        advisory = WeatherAdvisory.OPTIMAL_CONDITIONS,
        advisoryDetail = ""
    )

    @Test
    fun `interpret returns STRONG_WIND_WARNING when wind exceeds 60 kph`() {
        val result = interpreter.interpret(makeWeather(wind = 65.0))
        assertEquals(WeatherAdvisory.STRONG_WIND_WARNING, result)
    }

    @Test
    fun `interpret returns HIGH_RAIN_RISK when rainfall exceeds 20mm`() {
        val result = interpreter.interpret(makeWeather(rainfall = 25.0))
        assertEquals(WeatherAdvisory.HIGH_RAIN_RISK, result)
    }

    @Test
    fun `interpret returns DELAY_FERTILIZATION when rainfall is between 10 and 20mm`() {
        val result = interpreter.interpret(makeWeather(rainfall = 15.0))
        assertEquals(WeatherAdvisory.DELAY_FERTILIZATION, result)
    }

    @Test
    fun `interpret returns DROUGHT_RISK when no rain and high temperature`() {
        val result = interpreter.interpret(makeWeather(rainfall = 0.0, tempMax = 36.0))
        assertEquals(WeatherAdvisory.DROUGHT_RISK, result)
    }

    @Test
    fun `interpret returns PEST_RISK_HIGH when hot and humid`() {
        val result = interpreter.interpret(makeWeather(tempMax = 39.0, humidity = 85))
        assertEquals(WeatherAdvisory.PEST_RISK_HIGH, result)
    }

    @Test
    fun `interpret returns OPTIMAL_CONDITIONS for normal Philippine weather`() {
        val result = interpreter.interpret(makeWeather(rainfall = 5.0, tempMax = 29.0, humidity = 70))
        assertEquals(WeatherAdvisory.OPTIMAL_CONDITIONS, result)
    }

    @Test
    fun `computeWeatherRiskFactor returns 0_3 when no forecasts provided`() {
        val result = interpreter.computeWeatherRiskFactor(emptyList())
        assertEquals(0.3, result, 0.001)
    }

    @Test
    fun `computeWeatherRiskFactor returns high risk for stormy conditions`() {
        val stormy = makeWeather(wind = 70.0, rainfall = 30.0)
        val result = interpreter.computeWeatherRiskFactor(listOf(stormy, stormy, stormy))
        assertTrue("Storm risk should be > 0.6", result > 0.6)
    }

    @Test
    fun `computeWeatherRiskFactor returns low risk for optimal conditions`() {
        val sunny = makeWeather(rainfall = 3.0, tempMax = 28.0)
        val result = interpreter.computeWeatherRiskFactor(listOf(sunny, sunny, sunny))
        assertTrue("Optimal conditions risk should be < 0.3", result < 0.3)
    }

    @Test
    fun `getMostCriticalAdvisory returns highest priority advisory`() {
        val goodDay = makeWeather(rainfall = 3.0, tempMax = 28.0)
        val stormDay = makeWeather(wind = 70.0)
        val result = interpreter.getMostCriticalAdvisory(listOf(goodDay, stormDay))
        assertEquals(WeatherAdvisory.STRONG_WIND_WARNING, result)
    }

    @Test
    fun `generateAdvisoryDetail returns non-empty string for all advisory types`() {
        WeatherAdvisory.values().forEach { advisory ->
            val detail = interpreter.generateAdvisoryDetail(advisory, makeWeather())
            assertTrue("Advisory detail should not be empty for $advisory", detail.isNotEmpty())
        }
    }

    @Test
    fun `computeWeatherRiskFactor result is bounded between 0 and 1`() {
        val allStorm = List(10) { makeWeather(wind = 100.0, rainfall = 100.0) }
        val result = interpreter.computeWeatherRiskFactor(allStorm)
        assertTrue(result <= 1.0)
        assertTrue(result >= 0.0)
    }
}
