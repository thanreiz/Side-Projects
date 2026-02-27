package com.floapp.agriflo.domain.model

/**
 * Represents a logged farmer activity (icon tap in the Logging Screen).
 * No text entry is required — log type is determined by icon selection.
 */
data class CropLog(
    val id: String,
    val cropId: String,
    val logType: LogType,
    val note: String = "",
    val imageUri: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val timestamp: Long,
    val isDeleted: Boolean = false
)

enum class LogType(
    val displayNameEn: String,
    val iconRes: String          // Reference to drawable resource name
) {
    FERTILIZER("Fertilizer Applied", "ic_log_fertilizer"),
    PEST("Pest Detected",           "ic_log_pest"),
    RAINFALL("Rainfall Observed",   "ic_log_rainfall"),
    IRRIGATION("Irrigation Done",   "ic_log_irrigation"),
    HARVEST("Harvest Completed",    "ic_log_harvest"),
    PESTICIDE("Pesticide Applied",  "ic_log_pesticide"),
    WEEDING("Weeding Done",         "ic_log_weeding"),
    OTHER("Other Activity",         "ic_log_other")
}
