package com.floapp.agriflo.ai.local

import android.content.Context
import com.floapp.agriflo.ai.AIQueryType
import com.floapp.agriflo.ai.AIResponse
import com.floapp.agriflo.ai.AITier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tier 1: Offline AI Engine.
 *
 * Designed to run a quantized LLM (4-bit) using ONNX Runtime Android bindings.
 * Model files are loaded lazily from the app's assets directory on first query.
 *
 * IMPORTANT: This engine is currently STUBBED with a rule-based response system
 * for the initial build. The ONNX binding can be dropped in once a model file
 * is available (e.g., Gemma 2B Q4 exported to ONNX format).
 *
 * To integrate a real model:
 * 1. Place the .onnx model file in app/src/main/assets/models/
 * 2. Replace the stub logic in [initializeModel] and [runInference]
 * 3. Wire the OrtSession calls with the appropriate tokenizer
 *
 * Memory budget: ~500MB for a 2B Q4 model — safe for 2GB+ RAM devices.
 * AI runs entirely on background Dispatcher.Default threads.
 */
@Singleton
class LocalAIEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var isInitialized = false
    private var modelSession: Any? = null // Placeholder for OrtSession

    /**
     * Lazy initialization — only loads model on first actual use.
     * Called on Dispatchers.Default to avoid blocking the main thread.
     */
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            withContext(Dispatchers.Default) {
                initializeModel()
                isInitialized = true
            }
        }
    }

    private fun initializeModel() {
        // ── STUB: Replace with ONNX Runtime initialization ──────────────────
        // val env = OrtEnvironment.getEnvironment()
        // val modelBytes = context.assets.open("models/agro-gemma-2b-q4.onnx").readBytes()
        // modelSession = env.createSession(modelBytes, OrtSession.SessionOptions())
        // ────────────────────────────────────────────────────────────────────
        android.util.Log.i("LocalAIEngine", "Stub mode: ONNX model not yet loaded")
    }

    /**
     * Runs inference for a given prompt.
     * Falls back to a rich rule-based knowledge base when model is not available.
     */
    suspend fun query(prompt: String, queryType: AIQueryType): AIResponse {
        ensureInitialized()
        return withContext(Dispatchers.Default) {
            val response = if (modelSession != null) {
                runInference(prompt)
            } else {
                generateRuleBasedResponse(prompt, queryType)
            }
            AIResponse(text = response, tier = AITier.LOCAL)
        }
    }

    private fun runInference(prompt: String): String {
        // ── STUB: Replace with actual ONNX tokenizer + inference ─────────────
        return generateRuleBasedResponse(prompt, AIQueryType.AGRONOMIC_TEXT)
    }

    /**
     * Rule-based agronomic knowledge base for Philippine smallholder farming.
     * Covers the most common farmer queries when the ONNX model is unavailable.
     */
    private fun generateRuleBasedResponse(prompt: String, queryType: AIQueryType): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("palay") || lowerPrompt.contains("rice") ->
                "Para sa palay: Itanim ang NSIC Rc222 o Rc216 variety. Espasyo: 20x20cm. " +
                "Mag-abono sa loob ng 14 araw pagkatapos magtanim. " +
                "For rice: Plant certified varieties. Spacing: 20x20cm. Apply basal fertilizer within 14 days of transplanting."
            lowerPrompt.contains("fertilizer") || lowerPrompt.contains("abono") ->
                "Inirerekomenda para sa palay: 14-14-14 NPK sa umpisa, pagkatapos ay Urea (46-0-0) bago mamukadkad. " +
                "Recommended for rice: Apply 14-14-14 NPK as basal, then Urea (46-0-0) at panicle initiation. " +
                "Always check weather before applying — avoid rain within 48 hours."
            lowerPrompt.contains("pest") || lowerPrompt.contains("peste") ->
                "Mga karaniwang peste ng palay: stem borer, brown planthopper, leaf folder. " +
                "Gumamit ng panlaban na kemikal na aprubado ng BPI. " +
                "Common rice pests: stem borer (pambubutas ng tangkay), BPH (kayumanggi na paruparo). " +
                "Use BPI-approved insecticides. Best control: maintain water level and remove weeds."
            lowerPrompt.contains("harvest") || lowerPrompt.contains("ani") ->
                "Ani ang palay kapag 80% ng butil ay ginto na. Karaniwang 105–125 araw pagkatapos ng pagtatanim. " +
                "Harvest rice when 80% of grains are golden. Usually 105–125 days after transplanting. " +
                "Grain moisture should be 20–25% at harvest for best quality."
            lowerPrompt.contains("weather") || lowerPrompt.contains("panahon") ->
                "Pinakamainam na temperatura para sa palay: 25–32°C. Kailangan ng 200mm/buwan ng ulan. " +
                "Ideal temperature for rice: 25–32°C. Needs about 200mm/month of rainfall. " +
                "Avoid fertilizer application when rain expected within 24–48 hours."
            lowerPrompt.contains("profit") || lowerPrompt.contains("kita") ->
                "Ang average na kita ng isang magsasaka sa palay ay PHP 15,000–25,000 bawat ektarya kada season. " +
                "Average net income for rice farming in Philippines: PHP 15,000–25,000/ha per season. " +
                "Key to profitability: certified seeds, correct fertilizer timing, and timely harvest."
            else ->
                "Salamat sa iyong katanungan. Ang Flo AI ay nagtatrabaho para tulungan kayo. " +
                "Para sa kumplikadong tanong, kumonekta sa internet para sa mas detalyadong sagot. " +
                "Thank you for your question. For complex queries, please connect to the internet for a more detailed response from our cloud AI. " +
                "Basic tip: Always log your farming activities in Flo for better harvest predictions."
        }
    }
}
