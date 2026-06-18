package com.example.repository

import android.util.Log
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.database.MoodFitDao
import com.example.database.MoodLog
import com.example.database.SavedActivity
import com.example.model.ActivitySuggestion
import com.example.model.NewsArticle
import com.example.model.OfflineRecommendations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class MoodFitRepository(private val moodFitDao: MoodFitDao) {

    val allSavedActivities: Flow<List<SavedActivity>> = moodFitDao.getAllSavedActivities()
    val allMoodLogs: Flow<List<MoodLog>> = moodFitDao.getAllMoodLogs()

    suspend fun insertMoodLog(mood: String, energy: Int) = withContext(Dispatchers.IO) {
        val log = MoodLog(mood = mood, energyLevel = energy)
        moodFitDao.insertMoodLog(log)
    }

    suspend fun clearMoodHistory() = withContext(Dispatchers.IO) {
        moodFitDao.clearMoodLogs()
    }

    suspend fun saveActivity(activity: SavedActivity) = withContext(Dispatchers.IO) {
        moodFitDao.insertSavedActivity(activity)
    }

    suspend fun removeSavedActivityById(id: Int) = withContext(Dispatchers.IO) {
        moodFitDao.deleteSavedActivityById(id)
    }

    suspend fun removeSavedActivityByTitle(title: String) = withContext(Dispatchers.IO) {
        moodFitDao.deleteSavedActivityByTitle(title)
    }

    suspend fun isActivitySaved(title: String): Boolean = withContext(Dispatchers.IO) {
        moodFitDao.isActivitySaved(title)
    }

    suspend fun getRecommendations(
        mood: String,
        customMoodDescription: String? = null,
        healthData: String? = null,
        language: String = "English"
    ): List<ActivitySuggestion> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w("MoodFitRepository", "Gemini API key is placeholder or empty. Using offline fallback mode.")
            return@withContext OfflineRecommendations.getFallbackRecommendations(mood, language)
        }

        val hwContext = if (healthData != null) "The user's health tracker indicates: $healthData. Tailor the activities to this physical state." else ""
        val customContext = if (!customMoodDescription.isNullOrBlank()) {
            "The user specifically describes their current state/mood as: \"$customMoodDescription\". Prioritize this description when tailoring the activities."
        } else {
            ""
        }
        val seed = System.currentTimeMillis()

        val prompt = """
            Generate exactly 3 personalized activities for someone who is feeling '$mood'. $customContext $hwContext
            Each activity must be highly relevant, practical, and well-being-focused.
            
            The output fields "title" and "description" MUST be generated in the $language language.
            
            Return the output in a structured JSON Array of objects with NO other text.
            Each object MUST have these properties:
            - "title": a short, encouraging activity name
            - "description": a highly actionable and exciting description of what to do
            - "duration": a realistic duration (e.g., '10 mins', '30 mins')
            - "type": select exactly one of: 'Mental', 'Physical', 'Social', 'Creative'
            - "category": select exactly one of: 'Mindfulness', 'Fitness', 'Recreation', 'Productivity'

            Do not wrap in markdown ```json blocks. Return ONLY the raw JSON string array.
            Make it uplifting and specific to the mood '$mood'.
            Randomness seed: $seed. Make sure to generate unique and creative ideas that are fresh.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val cleanedText = cleanJsonString(jsonText)
                val jsonArray = JSONArray(cleanedText)
                val suggestions = mutableListOf<ActivitySuggestion>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    suggestions.add(
                        ActivitySuggestion(
                            title = obj.optString("title", "Wellness Action"),
                            description = obj.optString("description", "Take a breathing wellness break."),
                            duration = obj.optString("duration", "15 mins"),
                            type = obj.optString("type", "Mental"),
                            category = obj.optString("category", "Mindfulness")
                        )
                    )
                }
                if (suggestions.isNotEmpty()) {
                    return@withContext suggestions
                }
            }
            Log.e("MoodFitRepository", "JSON extraction failed, defaulting to offline recommendations.")
            OfflineRecommendations.getFallbackRecommendations(mood, language)
        } catch (e: Exception) {
            Log.e("MoodFitRepository", "Network or query error: ${e.message}. Defaulting to offline suggestions.", e)
            OfflineRecommendations.getFallbackRecommendations(mood, language)
        }
    }

    suspend fun translateActivities(activities: List<ActivitySuggestion>, targetLanguage: String): List<ActivitySuggestion> = withContext(Dispatchers.IO) {
        if (activities.isEmpty()) return@withContext activities
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return@withContext activities
        }

        val jsonArray = JSONArray()
        for (act in activities) {
            val obj = org.json.JSONObject()
            obj.put("title", act.title)
            obj.put("description", act.description)
            obj.put("duration", act.duration)
            obj.put("type", act.type)
            obj.put("category", act.category)
            jsonArray.put(obj)
        }

        val prompt = """
            Translate the following JSON array of activities into the $targetLanguage language.
            Translate only the "title" and "description" fields to $targetLanguage.
            Leave all other fields ("duration", "type", "category") unchanged.
            Return only the valid JSON array of objects, with no markdown formatting or extra text.
            
            Input JSON:
            ${jsonArray.toString()}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val cleanedText = cleanJsonString(jsonText)
                val resArray = JSONArray(cleanedText)
                val result = mutableListOf<ActivitySuggestion>()
                for (i in 0 until resArray.length()) {
                    val obj = resArray.getJSONObject(i)
                    val original = activities.getOrNull(i)
                    result.add(
                        ActivitySuggestion(
                            title = obj.optString("title", original?.title ?: ""),
                            description = obj.optString("description", original?.description ?: ""),
                            duration = obj.optString("duration", original?.duration ?: ""),
                            type = obj.optString("type", original?.type ?: ""),
                            category = obj.optString("category", original?.category ?: ""),
                            isSaved = original?.isSaved ?: false
                        )
                    )
                }
                if (result.isNotEmpty()) {
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.e("MoodFitRepository", "Translation failed: ${e.message}")
        }
        return@withContext activities
    }

    suspend fun translateNewsArticles(articles: List<NewsArticle>, targetLanguage: String): List<NewsArticle> = withContext(Dispatchers.IO) {
        if (articles.isEmpty() || targetLanguage.lowercase() == "english") return@withContext articles
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return@withContext articles
        }

        // Translate only the first 5 articles to keep the request size and latency reasonable
        val articlesToTranslate = articles.take(5)
        val remainingArticles = articles.drop(5)

        val jsonArray = JSONArray()
        for (art in articlesToTranslate) {
            val obj = org.json.JSONObject()
            obj.put("title", art.title)
            obj.put("description", art.description)
            obj.put("publishedAt", art.publishedAt)
            obj.put("url", art.url)
            obj.put("urlToImage", art.urlToImage)
            jsonArray.put(obj)
        }

        val prompt = """
            Translate the following JSON array of news articles into the $targetLanguage language.
            Translate only the "title" and "description" fields to $targetLanguage.
            Leave all other fields ("publishedAt", "url", "urlToImage") unchanged.
            Return only the valid JSON array of objects, with no markdown formatting or extra text.
            
            Input JSON:
            ${jsonArray.toString()}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.2f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val cleanedText = cleanJsonString(jsonText)
                val resArray = JSONArray(cleanedText)
                val result = mutableListOf<NewsArticle>()
                for (i in 0 until resArray.length()) {
                    val obj = resArray.getJSONObject(i)
                    val original = articlesToTranslate.getOrNull(i)
                    result.add(
                        NewsArticle(
                            title = obj.optString("title", original?.title ?: ""),
                            description = obj.optString("description", original?.description ?: ""),
                            publishedAt = obj.optString("publishedAt", original?.publishedAt ?: ""),
                            url = obj.optString("url", original?.url ?: ""),
                            urlToImage = obj.optString("urlToImage", original?.urlToImage ?: "")
                        )
                    )
                }
                if (result.isNotEmpty()) {
                    return@withContext result + remainingArticles
                }
            }
        } catch (e: Exception) {
            Log.e("MoodFitRepository", "News translation failed: ${e.message}")
        }
        return@withContext articles
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }
}
