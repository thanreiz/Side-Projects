package com.floapp.agriflo.data.local.mapper

import com.floapp.agriflo.data.local.entity.*
import com.floapp.agriflo.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Extension functions to map between DB entities and domain models.
 * Mappers are pure Kotlin functions with no Android framework dependencies.
 */

private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
private val mapAdapter = moshi.adapter<Map<String, Int>>(
    com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)
)

// ─── CropEntity ↔ Crop ──────────────────────────────────────────────────────

fun CropEntity.toDomain(): Crop {
    val stageDurationsRaw = mapAdapter.fromJson(stageDurationsJson) ?: emptyMap()
    val stageDurations = stageDurationsRaw.entries.associate { (key, value) ->
        CropStageType.valueOf(key) to value
    }
    return Crop(
        id = id,
        name = name,
        variety = variety,
        landAreaHa = landAreaHa,
        plantingDate = Instant.ofEpochMilli(plantingDate).atZone(ZoneId.systemDefault()).toLocalDate(),
        expectedHarvestDate = if (expectedHarvestDate > 0L)
            Instant.ofEpochMilli(expectedHarvestDate).atZone(ZoneId.systemDefault()).toLocalDate()
        else null,
        stageDurations = stageDurations,
        cropType = CropType.valueOf(cropType),
        notes = notes,
        isActive = isActive,
        isSynced = synced,
        updatedAt = updatedAt
    )
}

fun Crop.toEntity(): CropEntity {
    val stageDurationsRaw = stageDurations.entries.associate { (k, v) -> k.name to v }
    return CropEntity(
        id = id,
        name = name,
        variety = variety,
        landAreaHa = landAreaHa,
        plantingDate = plantingDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        expectedHarvestDate = expectedHarvestDate
            ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0L,
        stageDurationsJson = mapAdapter.toJson(stageDurationsRaw),
        cropType = cropType.name,
        notes = notes,
        isActive = isActive,
        synced = isSynced,
        updatedAt = updatedAt
    )
}

// ─── CropLogEntity ↔ CropLog ────────────────────────────────────────────────

fun CropLogEntity.toDomain(): CropLog = CropLog(
    id = id,
    cropId = cropId,
    logType = LogType.valueOf(logType),
    note = note,
    imageUri = imageUri,
    quantity = quantity,
    unit = unit,
    timestamp = timestamp,
    isDeleted = isDeleted
)

fun CropLog.toEntity(): CropLogEntity = CropLogEntity(
    id = id,
    cropId = cropId,
    logType = logType.name,
    note = note,
    imageUri = imageUri,
    quantity = quantity,
    unit = unit,
    timestamp = timestamp
)

// ─── WeatherCacheEntity ↔ WeatherData ───────────────────────────────────────

fun WeatherCacheEntity.toDomain(): WeatherData = WeatherData(
    date = date,
    tempMinC = tempMinC,
    tempMaxC = tempMaxC,
    rainfallMm = rainfallMm,
    humidityPct = humidityPct,
    windSpeedKph = windSpeedKph,
    uvIndex = uvIndex,
    advisory = WeatherAdvisory.valueOf(advisory),
    advisoryDetail = advisoryDetail
)

fun WeatherData.toEntity(id: String, forecastType: String = "DAILY_FORECAST"): WeatherCacheEntity =
    WeatherCacheEntity(
        id = id,
        date = date,
        tempMinC = tempMinC,
        tempMaxC = tempMaxC,
        rainfallMm = rainfallMm,
        humidityPct = humidityPct,
        windSpeedKph = windSpeedKph,
        uvIndex = uvIndex,
        advisory = advisory.name,
        advisoryDetail = advisoryDetail,
        forecastType = forecastType
    )

// ─── HarvestForecastEntity ↔ HarvestForecast ────────────────────────────────

private val assumptionsAdapter = moshi.adapter(ForecastAssumptions::class.java)

fun HarvestForecastEntity.toDomain(): HarvestForecast = HarvestForecast(
    id = id,
    cropId = cropId,
    projectedYieldKg = projectedYieldKg,
    projectedRevenuePhp = projectedRevenuePhp,
    projectedCostPhp = projectedCostPhp,
    netProfitPhp = netProfitPhp,
    riskScore = riskScore,
    riskLabel = RiskLabel.valueOf(riskLabel),
    assumptions = assumptionsAdapter.fromJson(assumptions)!!,
    generatedAt = generatedAt
)

fun HarvestForecast.toEntity(): HarvestForecastEntity = HarvestForecastEntity(
    id = id,
    cropId = cropId,
    projectedYieldKg = projectedYieldKg,
    projectedRevenuePhp = projectedRevenuePhp,
    projectedCostPhp = projectedCostPhp,
    netProfitPhp = netProfitPhp,
    riskScore = riskScore,
    riskLabel = riskLabel.name,
    assumptions = assumptionsAdapter.toJson(assumptions),
    generatedAt = generatedAt
)
