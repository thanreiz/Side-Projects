package com.floapp.agriflo.ai.cloud

import com.floapp.agriflo.BuildConfig
import com.floapp.agriflo.ai.AIQueryType
import com.floapp.agriflo.ai.AIResponse
import com.floapp.agriflo.ai.AITier
import com.floapp.agriflo.data.remote.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tier 2: Cloud AI Engine using Gemini 1.5 Flash.
 *
 * Handles:
 * - Crop disease detection from images (multimodal)
 * - Complex financial forecasting explanations
 * - Detailed agronomic queries beyond local model capability
 *
 * API key is read from BuildConfig (injected from local.properties).
 * Never hardcoded.
 */
@Singleton
class CloudAIEngine @Inject constructor(
    private val geminiApiService: GeminiApiService
) {

    private val systemPromptText = """
        You are Flo, an AI agricultural advisor for smallholder farmers in the Philippines.
        Keep responses concise, practical, and actionable. 
        Use simple language suitable for farmers aged 40-65.
        When possible, include a brief Tagalog translation of key advice.
        Focus on Philippine agricultural conditions, DA-approved practices, and local crop varieties.
        Never use technical jargon without explanation.
    """.trimIndent()

    suspend fun query(
        prompt: String,
        queryType: AIQueryType,
        imageBase64: String? = null
    ): AIResponse = withContext(Dispatchers.IO) {
        val parts = mutableListOf<GeminiPart>()
        parts.add(GeminiPart(text = buildContextualPrompt(prompt, queryType)))

        if (imageBase64 != null) {
            parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", imageBase64)))
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = parts, role = "user")),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPromptText)),
                role = "user"
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = if (queryType == AIQueryType.DISEASE_DETECTION_IMAGE) 0.1 else 0.4,
                maxOutputTokens = if (queryType == AIQueryType.FINANCIAL_EXPLANATION) 1500 else 800
            )
        )

        val response = geminiApiService.generateContent(
            apiKey = BuildConfig.GEMINI_API_KEY,
            request = request
        )
        AIResponse(text = response.extractText(), tier = AITier.CLOUD)
    }

    private fun buildContextualPrompt(prompt: String, queryType: AIQueryType): String {
        return when (queryType) {
            AIQueryType.DISEASE_DETECTION_IMAGE ->
                "Analyze this crop image from a Philippine farm. Identify any visible diseases, pests, or nutrient deficiencies. " +
                "Provide: 1) Diagnosis, 2) Severity, 3) Immediate action steps, 4) DA-approved treatment options. " +
                "Farmer question: $prompt"
            AIQueryType.FINANCIAL_EXPLANATION ->
                "Explain this harvest forecast to a Filipino farmer in simple terms. " +
                "Focus on what affects their profit and what they can do to improve it. " +
                "Context: $prompt"
            else -> prompt
        }
    }
}
