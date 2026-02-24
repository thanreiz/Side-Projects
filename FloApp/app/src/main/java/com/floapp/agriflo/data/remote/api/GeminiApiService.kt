package com.floapp.agriflo.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Gemini API service for cloud AI (Tier 2).
 * Uses generateContent endpoint with the gemini-1.5-flash model for fast, cost-effective inference.
 *
 * API key is injected via BuildConfig and never hardcoded.
 */
interface GeminiApiService {

    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String = "gemini-1.5-flash",
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    val data: String // Base64-encoded image
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Double = 0.3,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int = 1024,
    @Json(name = "topP") val topP: Double = 0.8
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
) {
    fun extractText(): String =
        candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
}

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent,
    @Json(name = "finishReason") val finishReason: String?
)
