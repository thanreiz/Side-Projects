package com.floapp.agriflo.data.remote.mock

import com.floapp.agriflo.domain.model.FertilizerGuideline
import com.floapp.agriflo.domain.model.LandData
import com.floapp.agriflo.domain.model.SoilProfile

/**
 * Mock data generator for the Land Analysis feature.
 *
 * Data is based on BSWM (Bureau of Soils and Water Management) official maps
 * and downloadables: https://www.bswm.da.gov.ph/bswm-maps/
 *
 * Also based on:
 * - BSWM Soil Fertility Maps for Nueva Ecija (1:100,000 scale)
 * - BSWM Fertilizer-Guide Maps for Key Rice Areas
 * - IRRI/DA/PhilRice "Quick Guide for Fertilizing Transplanted Rice in Nueva Ecija"
 * - IRRI NUTRIENT MANAGER / BSWM Nutrient Status Maps (Nitrogen, Phosphorous, Potassium, Zinc)
 *
 * NOTE: This is mock/demo data for development. Actual data should be fetched
 * from the BSWM API or downloaded datasets when an internet connection is available.
 */
object LandDataMockGenerator {

    /**
     * Returns the default data for the GPS-detected location.
     * Defaults to Cabanatuan area in Nueva Ecija as a representative municipality.
     */
    fun getDefaultGpsData(): LandData = getNuevaEcijaCabanatuan()

    /**
     * Returns available region options for the "Change Region" feature.
     * Each entry is a Pair of (display name, region key).
     */
    fun getAvailableRegions(): List<Pair<String, String>> = listOf(
        "Nueva Ecija – Cabanatuan Area" to "ne_cabanatuan",
        "Nueva Ecija – Science City of Muñoz" to "ne_munoz",
        "Nueva Ecija – Palayan City Area" to "ne_palayan",
        "Nueva Ecija – Gapan Area" to "ne_gapan",
        "Nueva Ecija – San Jose City Area" to "ne_sanjose"
    )

    /**
     * Returns the LandData for the given region key.
     */
    fun getDataForRegion(regionKey: String): LandData = when (regionKey) {
        "ne_munoz"   -> getNuevaEcijaMunoz()
        "ne_palayan" -> getNuevaEcijaItogon()
        "ne_gapan"   -> getNuevaEcijaGapan()
        "ne_sanjose" -> getNuevaEcijaSanJose()
        else         -> getNuevaEcijaCabanatuan()
    }

    // ─────────────────────────────────────────────────────────────────
    // Region Data
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cabanatuan City area – mostly Abnam Loam Clay and Quingua Silt Loam.
     * This is the heart of Nueva Ecija's rice bowl.
     */
    private fun getNuevaEcijaCabanatuan() = LandData(
        regionName = "Nueva Ecija (Central Luzon)",
        subRegion  = "Cabanatuan City Area",
        elevationRangeM = "15 – 75 m above sea level",
        isGpsDerived = true,
        soilProfiles = listOf(
            SoilProfile(
                seriesName    = "Abnam Loam Clay",
                texture       = "Clay Loam",
                phValue       = 6.2f,
                phRating      = "Slightly Acidic",
                sandPercent   = 20,
                siltPercent   = 35,
                clayPercent   = 45,
                organicMatter = "Medium (2.0 – 2.6%)",
                coveragePercent = 55,
                simpleTerms   = "This is dark, heavy soil that holds water very well — like a sponge that doesn't dry out easily. It is rich in nutrients and easy for rice and root crops to grow in. It becomes sticky when wet and hard when dry, so plowing at the right time is important.",
                suitableCrops = listOf("Rice (Wet & Dry Season)", "Banana", "Cassava", "Sweet Potato", "Sugarcane", "Eggplant", "Okra", "Citrus"),
                poorCrops     = listOf("Upland Sweet Corn (on very heavy patches)", "Root crops on poorly drained sub-areas")
            ),
            SoilProfile(
                seriesName    = "Quingua Silt Loam",
                texture       = "Silt Loam",
                phValue       = 6.5f,
                phRating      = "Near Neutral",
                sandPercent   = 15,
                siltPercent   = 60,
                clayPercent   = 25,
                organicMatter = "Medium-High (2.5 – 3.1%)",
                coveragePercent = 30,
                simpleTerms   = "Smooth, silky soil that is very easy to plow and plant in. It holds moisture but also drains well, so roots get both water and air. This is generally the most versatile farm soil in the province — it supports most vegetables, corn, and fruit trees.",
                suitableCrops = listOf("Rice", "Corn (Hybrid)", "Tomato", "Pepper", "Cabbage", "Mongo Bean", "Papaya", "Mango", "Banana", "Sitaw (String Bean)"),
                poorCrops     = listOf()
            ),
            SoilProfile(
                seriesName    = "Lipa Clay",
                texture       = "Heavy Clay",
                phValue       = 5.8f,
                phRating      = "Moderately Acidic",
                sandPercent   = 10,
                siltPercent   = 20,
                clayPercent   = 70,
                organicMatter = "Low-Medium (1.5 – 2.0%)",
                coveragePercent = 15,
                simpleTerms   = "Very thick, sticky soil that is hard to work with when wet and cracks when dry. It stays flooded longer, which rice loves, but it is too heavy for most vegetables. Lime treatment is recommended to correct the slightly acidic pH before planting anything other than rice.",
                suitableCrops = listOf("Lowland Rice", "Water Taro (Gabi)"),
                poorCrops     = listOf("Corn", "Vegetables (without raised beds)", "Root Crops", "Most Fruit Trees")
            )
        ),
        fertilizerGuidelines = cabanatuan_fertilizerGuidelines()
    )

    /** Science City of Muñoz area – slightly more upland, loam-dominant. */
    private fun getNuevaEcijaMunoz() = LandData(
        regionName = "Nueva Ecija (Central Luzon)",
        subRegion  = "Science City of Muñoz",
        elevationRangeM = "60 – 200 m above sea level",
        isGpsDerived = false,
        soilProfiles = listOf(
            SoilProfile(
                seriesName    = "Munoz Clay Loam",
                texture       = "Clay Loam",
                phValue       = 6.4f,
                phRating      = "Slightly Acidic to Neutral",
                sandPercent   = 25,
                siltPercent   = 40,
                clayPercent   = 35,
                organicMatter = "Medium (2.1 – 2.8%)",
                coveragePercent = 60,
                simpleTerms   = "Well-balanced soil — not too heavy, not too sandy. It drains well after rain yet stays moist underneath. Ideal for diversified farming including vegetables, corn, and rice.",
                suitableCrops = listOf("Rice", "Corn", "Mongo Bean", "Peanut", "Tomato", "Onion", "Garlic", "Watermelon"),
                poorCrops     = listOf()
            ),
            SoilProfile(
                seriesName    = "Candaba Silt Clay",
                texture       = "Silty Clay",
                phValue       = 6.0f,
                phRating      = "Slightly Acidic",
                sandPercent   = 10,
                siltPercent   = 50,
                clayPercent   = 40,
                organicMatter = "Medium (1.9 – 2.4%)",
                coveragePercent = 40,
                simpleTerms   = "Soft, fine-grained soil with a slightly acidic character. Great for rice farming. Vegetable growers should add compost or lime to improve performance.",
                suitableCrops = listOf("Rice", "Kangkong", "Banana", "Sugarcane"),
                poorCrops     = listOf("Carrot", "Radish", "Most Root Vegetables")
            )
        ),
        fertilizerGuidelines = munoz_fertilizerGuidelines()
    )

    /** Palayan City area – upland transitional, gravelly loam phases present. */
    private fun getNuevaEcijaItogon() = LandData(
        regionName = "Nueva Ecija (Central Luzon)",
        subRegion  = "Palayan City Area",
        elevationRangeM = "100 – 350 m above sea level",
        isGpsDerived = false,
        soilProfiles = listOf(
            SoilProfile(
                seriesName    = "Abnam Loam (Gravelly Phase)",
                texture       = "Gravelly Loam",
                phValue       = 6.3f,
                phRating      = "Slightly Acidic",
                sandPercent   = 40,
                siltPercent   = 35,
                clayPercent   = 25,
                organicMatter = "Low-Medium (1.5 – 2.2%)",
                coveragePercent = 65,
                simpleTerms   = "Lighter, gritty soil with small stones mixed in. Drains very quickly after rain, making it unsuitable for lowland rice but excellent for fruits, bananas, corn, and root crops that need good drainage.",
                suitableCrops = listOf("Banana", "Mango", "Corn", "Cassava", "Papaya", "Citrus", "Sweet Potato"),
                poorCrops     = listOf("Lowland Rice", "Kangkong", "Water-intensive Vegetables")
            )
        ),
        fertilizerGuidelines = palayan_fertilizerGuidelines()
    )

    /** Gapan area – lowland, highly irrigated rice lands. */
    private fun getNuevaEcijaGapan() = LandData(
        regionName = "Nueva Ecija (Central Luzon)",
        subRegion  = "Gapan City Area",
        elevationRangeM = "10 – 50 m above sea level",
        isGpsDerived = false,
        soilProfiles = listOf(
            SoilProfile(
                seriesName    = "Quingua Silt Loam (Irrigated)",
                texture       = "Silt Loam",
                phValue       = 6.6f,
                phRating      = "Near Neutral",
                sandPercent   = 15,
                siltPercent   = 62,
                clayPercent   = 23,
                organicMatter = "High (2.8 – 3.5%)",
                coveragePercent = 75,
                simpleTerms   = "Deep, silky, fertile irrigated soil. One of the most productive lowland soils in the province. Very well-suited for double cropping (two rice harvests per year). High in organic matter from years of flooding cycles.",
                suitableCrops = listOf("Rice (Double Crop)", "Mongo Bean", "Tomato", "Pepper", "Onion (off-season)"),
                poorCrops     = listOf()
            )
        ),
        fertilizerGuidelines = gapan_fertilizerGuidelines()
    )

    /** San Jose City area – eastern foothills, mixed upland-lowland. */
    private fun getNuevaEcijaSanJose() = LandData(
        regionName = "Nueva Ecija (Central Luzon)",
        subRegion  = "San Jose City Area",
        elevationRangeM = "50 – 250 m above sea level",
        isGpsDerived = false,
        soilProfiles = listOf(
            SoilProfile(
                seriesName    = "Matias Sandy Loam",
                texture       = "Sandy Loam",
                phValue       = 6.1f,
                phRating      = "Slightly Acidic",
                sandPercent   = 55,
                siltPercent   = 30,
                clayPercent   = 15,
                organicMatter = "Low (1.2 – 1.8%)",
                coveragePercent = 50,
                simpleTerms   = "Loose, sandy soil that drains very fast. Water flows through quickly so it dries out fast — crops need more frequent watering. It is easier to plow but less naturally fertile. Organic matter (compost or manure) should be added regularly to maintain soil health.",
                suitableCrops = listOf("Peanut", "Sweet Potato", "Watermelon", "Ampalaya", "Corn", "Cassava"),
                poorCrops     = listOf("Rice (without reliable irrigation)", "Heavy-feeding Vegetables", "Banana (on very dry patches)")
            ),
            SoilProfile(
                seriesName    = "Abnam Loam Clay",
                texture       = "Clay Loam",
                phValue       = 6.2f,
                phRating      = "Slightly Acidic",
                sandPercent   = 20,
                siltPercent   = 35,
                clayPercent   = 45,
                organicMatter = "Medium (2.0 – 2.4%)",
                coveragePercent = 50,
                simpleTerms   = "Heavier clay mix in the lowland portions. Good moisture retention. Ideal for rice and other water-tolerant crops. Benefits from organic amendments to reduce compaction.",
                suitableCrops = listOf("Rice", "Banana", "Sugarcane", "Eggplant"),
                poorCrops     = listOf("Carrot", "Radish")
            )
        ),
        fertilizerGuidelines = sanjose_fertilizerGuidelines()
    )

    // ─────────────────────────────────────────────────────────────────
    // Fertilizer Guidelines (per region)
    // Based on: BSWM Fertilizer-Guide Maps + IRRI/DA Quick Guide
    // ─────────────────────────────────────────────────────────────────

    private fun cabanatuan_fertilizerGuidelines() = listOf(
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Rice (Transplanted, Irrigated)",
            nitrogenKgPerHa  = "80 – 100 kg N/ha",
            phosphorusKgPerHa= "30 – 40 kg P₂O₅/ha",
            potassiumKgPerHa = "30 – 40 kg K₂O/ha",
            zincKgPerHa      = "5 kg ZnSO₄/ha (if Zn deficiency detected)",
            applicationNotes = "Split nitrogen into 3 applications: (1) Basal at transplanting — apply ½ P & ½ K + ⅓ N. (2) Active tillering (14–21 DAT) — apply ⅓ N. (3) Panicle initiation (45–50 DAT) — apply ⅓ N + ½ K.",
            source           = "BSWM Fertilizer-Guide Map, Nueva Ecija Key Rice Areas; IRRI/DA/PhilRice Quick Guide"
        ),
        FertilizerGuideline(
            season           = "Dry Season (Dec – May)",
            targetCrop       = "Rice (Transplanted, Irrigated)",
            nitrogenKgPerHa  = "90 – 120 kg N/ha",
            phosphorusKgPerHa= "30 – 40 kg P₂O₅/ha",
            potassiumKgPerHa = "30 – 40 kg K₂O/ha",
            applicationNotes = "Dry season crops typically need 10–20% more nitrogen due to higher solar radiation and faster crop growth. Three-split application scheme same as wet season. Ensure irrigated water supply before basal application.",
            source           = "BSWM Fertilizer-Guide Map; DA-Nueva Ecija Provincial Agriculture Office"
        ),
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Corn (Hybrid)",
            nitrogenKgPerHa  = "120 – 150 kg N/ha",
            phosphorusKgPerHa= "60 – 80 kg P₂O₅/ha",
            potassiumKgPerHa = "60 kg K₂O/ha",
            applicationNotes = "Apply ½ of N + all P + all K as basal at planting. Side-dress remaining ½ N at 30–35 days after emergence (knee-high). Use 14-14-14 complete fertilizer as basal option.",
            source           = "BSWM Nutrient Status Maps (N, P, K); Bureau of Plant Industry (BPI) Corn Guide"
        ),
        FertilizerGuideline(
            season           = "Year-round",
            targetCrop       = "Banana (Cardava / Lakatan)",
            nitrogenKgPerHa  = "200 – 250 kg N/ha/year",
            phosphorusKgPerHa= "100 kg P₂O₅/ha/year",
            potassiumKgPerHa = "300 – 400 kg K₂O/ha/year",
            applicationNotes = "Bananas are heavy potassium feeders. Divide annual fertilizer into 4 quarterly applications. Mix with compost or vermicast (2–3 tons/ha/year) for best results on clay-loam soils.",
            source           = "BSWM Soil Fertility Map; PhilFIDA Banana Production Guide"
        )
    )

    private fun munoz_fertilizerGuidelines() = listOf(
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Rice (Transplanted)",
            nitrogenKgPerHa  = "80 – 100 kg N/ha",
            phosphorusKgPerHa= "30 – 40 kg P₂O₅/ha",
            potassiumKgPerHa = "30 kg K₂O/ha",
            applicationNotes = "Three-split N application. Slightly lower K than Cabanatuan due to naturally higher K in Munoz clay loam soils. Soil test recommended every 2 seasons.",
            source           = "BSWM Fertilizer-Guide Map; PhilRice"
        ),
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Peanut",
            nitrogenKgPerHa  = "15 – 20 kg N/ha",
            phosphorusKgPerHa= "40 – 60 kg P₂O₅/ha",
            potassiumKgPerHa = "40 – 60 kg K₂O/ha",
            applicationNotes = "Peanuts fix atmospheric nitrogen — low N application only. Focus on P and K. Apply lime at 0.5–1 ton/ha if pH < 6.0 before planting.",
            source           = "BSWM Nutrient Status Map; BPI"
        ),
        FertilizerGuideline(
            season           = "Dry Season (Dec – May)",
            targetCrop       = "Onion (Bulb)",
            nitrogenKgPerHa  = "120 – 150 kg N/ha",
            phosphorusKgPerHa= "100 – 120 kg P₂O₅/ha",
            potassiumKgPerHa = "100 – 120 kg K₂O/ha",
            applicationNotes = "Onions require high P for bulb development. Apply all P as basal. Split N into 4 applications throughout the growing period (45–60 day crop).",
            source           = "BSWM; PhilVeg Onion Production Guide"
        )
    )

    private fun palayan_fertilizerGuidelines() = listOf(
        FertilizerGuideline(
            season           = "Year-round",
            targetCrop       = "Banana (Saba / Cardava)",
            nitrogenKgPerHa  = "180 – 220 kg N/ha/year",
            phosphorusKgPerHa= "80 – 100 kg P₂O₅/ha/year",
            potassiumKgPerHa = "250 – 350 kg K₂O/ha/year",
            applicationNotes = "Gravelly loam soils drain fast — nutrients leach down quickly. Increase frequency of smaller fertilizer doses (6 times a year instead of 4). Mulch around banana plants to conserve moisture.",
            source           = "BSWM; PhilFIDA"
        ),
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Corn (Hybrid)",
            nitrogenKgPerHa  = "120 – 140 kg N/ha",
            phosphorusKgPerHa= "60 – 80 kg P₂O₅/ha",
            potassiumKgPerHa = "40 – 60 kg K₂O/ha",
            applicationNotes = "On gravelly upland soils, use smaller, more frequent fertilizer applications to reduce nutrient runoff. Add 2–3 tons/ha of compost as basal organic amendment.",
            source           = "BSWM Nutrient Status Maps; BPI"
        )
    )

    private fun gapan_fertilizerGuidelines() = listOf(
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Rice (Double Crop, Transplanted)",
            nitrogenKgPerHa  = "90 – 110 kg N/ha",
            phosphorusKgPerHa= "30 – 40 kg P₂O₅/ha",
            potassiumKgPerHa = "30 – 40 kg K₂O/ha",
            zincKgPerHa      = "5 kg ZnSO₄/ha",
            applicationNotes = "High organic matter soils in Gapan's irrigated lowlands can supply significant nitrogen. Consider reducing basal N application by 10–15% in fields with heavy history of green manure use. Full Zn application recommended in first season.",
            source           = "BSWM Fertilizer-Guide Map; IRRI Nutrient Manager"
        ),
        FertilizerGuideline(
            season           = "Dry Season (Dec – May)",
            targetCrop       = "Mongo Bean (After Rice)",
            nitrogenKgPerHa  = "10 – 15 kg N/ha",
            phosphorusKgPerHa= "40 – 60 kg P₂O₅/ha",
            potassiumKgPerHa = "40 – 50 kg K₂O/ha",
            applicationNotes = "Mongo fixes nitrogen from the air. Apply only starter N fertilizer. Residual P and K from the previous rice crop can reduce requirements by 20–30%.",
            source           = "BSWM; PhilVeg"
        )
    )

    private fun sanjose_fertilizerGuidelines() = listOf(
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Peanut",
            nitrogenKgPerHa  = "10 – 15 kg N/ha",
            phosphorusKgPerHa= "40 – 60 kg P₂O₅/ha",
            potassiumKgPerHa = "40 – 60 kg K₂O/ha",
            applicationNotes = "Sandy loam soils are ideal for peanuts. Very low nitrogen needed — peanuts fix their own. Focus on P for pod fill and K for drought tolerance. Apply lime if pH < 6.0.",
            source           = "BSWM; BPI"
        ),
        FertilizerGuideline(
            season           = "Year-round",
            targetCrop       = "Sweet Potato",
            nitrogenKgPerHa  = "40 – 60 kg N/ha",
            phosphorusKgPerHa= "40 – 60 kg P₂O₅/ha",
            potassiumKgPerHa = "80 – 120 kg K₂O/ha",
            applicationNotes = "Sweet potatoes thrive in the loose sandy loam of San Jose. High K is critical for root development. Apply basal all P and K + ½ N, side-dress remaining ½ N at 30 days. Add organic compost to improve moisture retention in fast-draining sandy patches.",
            source           = "BSWM; PhilRootCrop"
        ),
        FertilizerGuideline(
            season           = "Wet Season (Jun – Nov)",
            targetCrop       = "Watermelon",
            nitrogenKgPerHa  = "60 – 80 kg N/ha",
            phosphorusKgPerHa= "40 – 60 kg P₂O₅/ha",
            potassiumKgPerHa = "80 – 100 kg K₂O/ha",
            applicationNotes = "Sandy loam is excellent for watermelon (good drainage, warm soil temp). Apply basal at transplanting. Foliar fertilizer sprays (K-rich) are effective near fruit-setting stage.",
            source           = "BSWM; OpenAg Watermelon Guide"
        )
    )
}
