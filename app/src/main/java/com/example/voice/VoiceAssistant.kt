package com.example.voice

import com.example.data.repository.TransitRepository
import com.example.data.model.MetroStation
import com.example.localization.Localizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class VoiceAssistantResult(
    val replyText: String,
    val detectedSourceStation: MetroStation? = null,
    val detectedDestinationStation: MetroStation? = null,
    val isRouteQuery: Boolean = false
)

object VoiceAssistant {

    // Local, high-performance rule-based NLP parser (100% offline capable)
    fun parseVoiceQueryLocal(query: String): VoiceAssistantResult {
        val q = query.lowercase().trim()
        val stations = TransitRepository.stations

        // Identify any station mentioned in the text
        val matchedStations = mutableListOf<MetroStation>()
        
        // Try exact/phonetic matches for all stations
        for (station in stations) {
            val names = listOf(
                station.nameEn.lowercase(),
                station.nameHi.lowercase(),
                station.nameMr.lowercase(),
                station.id.lowercase(),
                station.code.lowercase()
            )
            if (names.any { q.contains(it) }) {
                matchedStations.add(station)
            }
        }

        // Check for specific landmark mentions and map to closest station
        var landmarkStation: MetroStation? = null
        val landmarks = TransitRepository.landmarks
        for (landmark in landmarks) {
            val names = listOf(landmark.nameEn.lowercase(), landmark.nameHi.lowercase(), landmark.nameMr.lowercase())
            if (names.any { q.contains(it) }) {
                landmarkStation = stations.find { it.id == landmark.nearestStationId }
                if (landmarkStation != null && landmarkStation !in matchedStations) {
                    matchedStations.add(landmarkStation)
                }
            }
        }

        // Check for "se" (from) or "to" / "tak" / "jaana" structure for code-switching
        // Examples: "Andheri se Ghatkopar", "Andheri to Ghatkopar", "अंधेरी से घाटकोपर", "अंधेरी ते घाटकोपर"
        var source: MetroStation? = null
        var dest: MetroStation? = null

        if (matchedStations.size >= 2) {
            // Find which one is first in the query text to determine from/to
            val firstStation = matchedStations.minByOrNull { q.indexOf(it.nameEn.lowercase()) }
            val secondStation = matchedStations.maxByOrNull { q.indexOf(it.nameEn.lowercase()) }

            if (firstStation != null && secondStation != null && firstStation != secondStation) {
                // If query contains "se" after the first station name, first is source
                // e.g., "andheri se ghatkopar" -> andheri is source, ghatkopar is dest
                val idxFirst = q.indexOf(firstStation.nameEn.lowercase())
                val idxSecond = q.indexOf(secondStation.nameEn.lowercase())
                val intermediate = q.substring(idxFirst + firstStation.nameEn.length, idxSecond)
                
                if (intermediate.contains("se") || intermediate.contains("से") || intermediate.contains("ते") || intermediate.contains("from")) {
                    source = firstStation
                    dest = secondStation
                } else {
                    source = firstStation
                    dest = secondStation
                }
            }
        } else if (matchedStations.size == 1) {
            // Just one station mentioned, check if it's destination
            // e.g. "How do I get to Ghatkopar?" / "घाटकोपरला कसं जायचं?"
            val isToQuery = q.contains("to") || q.contains("jaana") || q.contains("जाना") || q.contains("जायचं") || q.contains("how to get")
            if (isToQuery) {
                dest = matchedStations[0]
            } else {
                dest = matchedStations[0]
            }
        }

        if (source != null && dest != null) {
            val reply = "Planning your route from ${source.nameEn} to ${dest.nameEn}..."
            return VoiceAssistantResult(reply, source, dest, true)
        } else if (dest != null) {
            val reply = "Found station ${dest.nameEn}. Showing details."
            return VoiceAssistantResult(reply, null, dest, true)
        }

        // Generic friendly reply based on language
        val welcomeReply = when (Localizer.currentLanguage.value.code) {
            "hi" -> "नमस्ते! मैं आपकी कैसे सहायता कर सकता हूँ? आप पूछ सकते हैं: 'अंधेरी से घाटकोपर कैसे जाना है?'"
            "mr" -> "नमस्कार! मी तुमची काय मदत करू शकतो? तुम्ही विचारू शकता: 'घाटकोपरला कसं जायचं?'"
            else -> "Hello! How can I help you navigate Mumbai Metro? Say something like 'Andheri to Ghatkopar route'."
        }

        return VoiceAssistantResult(welcomeReply)
    }

    // Call server-side Gemini API (Direct REST fallback for smart queries)
    suspend fun queryGemini(query: String, apiKey: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey.startsWith("MY_GEMINI_API_KEY")) {
            return@withContext "API Key is missing or invalid. Use Local Mode."
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val jsonRequest = JSONObject().apply {
            put("contents", org.json.JSONArray().put(
                JSONObject().put("parts", org.json.JSONArray().put(
                    JSONObject().put("text", "You are the official Mumbai Metro Voice Assistant. Answer the user transit query in the language they ask. Be extremely concise. User query: $query")
                ))
            ))
        }

        val body = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: ${response.code}"
                }
                val responseBody = response.body?.string() ?: return@withContext "Empty response"
                val jsonObject = JSONObject(responseBody)
                val candidates = jsonObject.getJSONArray("candidates")
                val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
