package com.floapp.agriflo.domain.model

/**
 * Represents the soil analysis and land data for a specific region.
 *
 * Sourced from Bureau of Soils and Water Management (BSWM) maps and guidelines.
 * https://www.bswm.da.gov.ph/bswm-maps/
 */
data class LandData(
    /** Human-readable region name, e.g., "Nueva Ecija (Central Luzon)" */
    val regionName: String,

    /** Province or sub-region for finer detail, e.g., "Cabanatuan Area" */
    val subRegion: String,

    /** Approximate elevation range in meters above sea level */
    val elevationRangeM: String,

    /** List of soil profiles found in this region */
    val soilProfiles: List<SoilProfile>,

    /**
     * Season-specific fertilizer guidelines for this region.
     * Keyed by season name (e.g., "Wet Season", "Dry Season").
     */
    val fertilizerGuidelines: List<FertilizerGuideline>,

    /** Whether this data was loaded from GPS (true) or manually selected (false) */
    val isGpsDerived: Boolean = true
)

/**
 * Describes a single soil profile/series found within a region.
 *
 * Based on BSWM soil series classification and simplified for farmer use.
 */
data class SoilProfile(
    /** Soil series name as classified by BSWM, e.g., "Abnam Loam Clay" */
    val seriesName: String,

    /** Texture classification, e.g., "Clay Loam", "Silt Loam" */
    val texture: String,

    /** Average pH value */
    val phValue: Float,

    /** pH rating description, e.g., "Slightly Acidic", "Neutral" */
    val phRating: String,

    /** Sand percentage (0–100) */
    val sandPercent: Int,

    /** Silt percentage (0–100) */
    val siltPercent: Int,

    /** Clay percentage (0–100) */
    val clayPercent: Int,

    /** Organic matter level, e.g., "Medium (2.1%)" */
    val organicMatter: String,

    /**
     * Layman explanation of what this soil means for a farmer.
     * Uses simple language, no jargon.
     */
    val simpleTerms: String,

    /** Approximate percentage of the region this profile covers */
    val coveragePercent: Int,

    /** Crops that benefit positively from this soil */
    val suitableCrops: List<String>,

    /**
     * Crops that will have difficulty growing in this soil.
     * Empty if no significant limitations for common crops.
     */
    val poorCrops: List<String> = emptyList()
)

/**
 * Season-specific fertilizer recommendation for the region.
 *
 * Based on BSWM Fertilizer-Guide Maps and IRRI/DA/PhilRice quick guides.
 */
data class FertilizerGuideline(
    /** Season name, e.g., "Wet Season (Jun–Nov)", "Dry Season (Dec–May)" */
    val season: String,

    /** Target crop for this guideline, e.g., "Rice (Transplanted)" */
    val targetCrop: String,

    /** Nitrogen recommendation in kg per hectare */
    val nitrogenKgPerHa: String,

    /** Phosphorus recommendation in kg per hectare */
    val phosphorusKgPerHa: String,

    /** Potassium recommendation in kg per hectare */
    val potassiumKgPerHa: String,

    /** Zinc recommendation, if applicable */
    val zincKgPerHa: String? = null,

    /** Application schedule notes */
    val applicationNotes: String,

    /** Source attribution */
    val source: String = "BSWM / DA-PhilRice / IRRI"
)
