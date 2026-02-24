package com.floapp.agriflo.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.floapp.agriflo.ai.cloud.CloudAIEngine
import com.floapp.agriflo.ai.local.LocalAIEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI routing manager — the split-brain controller.
 *
 * Deterministically routes queries to:
 * - Tier 1 (LocalAIEngine): When offline, or for simple agronomic queries
 * - Tier 2 (CloudAIEngine): When online AND query requires advanced reasoning
 *   (disease detection from image, financial forecasting explanation)
 *
 * The routing decision is made BEFORE the query is dispatched.
 * If cloud AI fails mid-query, it falls back to Tier 1 automatically.
 */
@Singleton
class AIManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localAIEngine: LocalAIEngine,
    private val cloudAIEngine: CloudAIEngine
) {

    /**
     * Routes a text query to the appropriate AI tier.
     * Returns the AI response string.
     */
    suspend fun query(
        prompt: String,
        queryType: AIQueryType = AIQueryType.AGRONOMIC_TEXT,
        imageBase64: String? = null
    ): AIResponse {
        val isOnline = isNetworkAvailable()
        val requiresCloud = queryType.requiresCloudAI || imageBase64 != null

        return when {
            isOnline && requiresCloud -> {
                try {
                    cloudAIEngine.query(prompt, queryType, imageBase64)
                } catch (e: Exception) {
                    // Fallback to local on cloud failure
                    localAIEngine.query(prompt, queryType)
                }
            }
            else -> localAIEngine.query(prompt, queryType)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

enum class AIQueryType(val requiresCloudAI: Boolean) {
    AGRONOMIC_TEXT(requiresCloudAI = false),          // "When should I plant rice?" → Tier 1
    DISEASE_DETECTION_IMAGE(requiresCloudAI = true),  // Image analysis → Tier 2
    FINANCIAL_EXPLANATION(requiresCloudAI = true),     // "Why is my profit low?" → Tier 2
    PEST_IDENTIFICATION(requiresCloudAI = false),      // Common pests → Tier 1
    WEATHER_EXPLANATION(requiresCloudAI = false)       // Weather advisory detail → Tier 1
}

data class AIResponse(
    val text: String,
    val tier: AITier,
    val isFromCache: Boolean = false
)

enum class AITier { LOCAL, CLOUD }
