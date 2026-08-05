package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.memory.MemoryManager
import com.example.utils.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiAiService(
    private val context: Context,
    var memoryManager: MemoryManager? = null
) {

    companion object {
        private const val TAG = "GeminiAiService"
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 2000L
    }

    val apiKeyManager = ApiKeyManager(context)
    private val apiMutex = Mutex()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemInstructionText = """
        You are JARVIS (Just A Rather Very Intelligent System), a highly advanced, ultra-intelligent AI Operating System Assistant inspired by futuristic HUD technology.
        Your tone is polite, crisp, professional, extremely helpful, addressing the user as "Boss" or "Sir/Ma'am".
        You fluently support multi-lingual conversation in English, Hindi, Urdu, Marathi, etc. Always reply in the same language or dialect as the user's input prompt.
        You have direct control over Mobile Skills, System Telemetry, Vision Analysis, and Automation Routines.
        When answering questions, provide precise, structured responses with markdown formatting or code snippets when helpful.
    """.trimIndent()

    suspend fun validateApiKey(testKey: String): Result<String> = withContext(Dispatchers.IO) {
        val keyToTest = testKey.trim()
        if (keyToTest.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank"))
        }

        try {
            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", "Ping")))
                    })
                }
                put("contents", contentsArray)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$keyToTest")
                .post(rootJson.toString().toRequestBody(mediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val respBodyStr = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Gemini API Key Verified Successfully")
            } else {
                val statusCode = response.code
                val isRateLimit = statusCode == 429 ||
                        respBodyStr.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                        respBodyStr.contains("QUOTA_EXCEEDED", ignoreCase = true)

                Log.w(TAG, "API Key Validation Failed [HTTP $statusCode]: $respBodyStr")

                val errorMsg = if (isRateLimit) {
                    "Key is valid, but rate limit / quota is currently exceeded (HTTP 429). You can still save this key."
                } else {
                    parseErrorMessage(respBodyStr)
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API key validation", e)
            Result.failure(Exception(e.localizedMessage ?: "Connection failure during key validation"))
        }
    }

    suspend fun generateResponse(
        prompt: String,
        bitmap: Bitmap? = null,
        history: List<Pair<String, String>> = emptyList(),
        memoryContext: String = ""
    ): String = apiMutex.withLock {
        withContext(Dispatchers.IO) {
            generateResponseInternal(prompt, bitmap, history, memoryContext)
        }
    }

    private suspend fun generateResponseInternal(
        prompt: String,
        bitmap: Bitmap? = null,
        history: List<Pair<String, String>> = emptyList(),
        memoryContext: String = ""
    ): String {
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isEmpty()) {
            return "JARVIS Core Alert: Gemini API Key is not configured. Please enter your key in Setup or Settings."
        }

        return try {
            val retrievedMemories = memoryManager?.prepareContext(prompt) ?: ""
            val effectivePrompt = if (retrievedMemories.isNotBlank()) {
                "$retrievedMemories\n\n$prompt"
            } else {
                prompt
            }

            val rootJson = JSONObject()
            val contentsArray = JSONArray()

            history.takeLast(6).forEach { (userMsg, aiMsg) ->
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userMsg)))
                })
                contentsArray.put(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", aiMsg)))
                })
            }

            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", effectivePrompt))

            if (bitmap != null) {
                val base64Image = bitmap.toBase64String()
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                }
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", currentParts)
            })
            rootJson.put("contents", contentsArray)

            val effectiveInstruction = systemInstructionText + memoryContext
            rootJson.put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", effectiveInstruction)))
            })

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(rootJson.toString().toRequestBody(mediaType))
                .build()

            executeCallWithRetry(request) { response, respBodyStr ->
                val aiResult = extractTextFromResponse(respBodyStr)
                if (aiResult.isNotEmpty() && !aiResult.startsWith("JARVIS Rate Limit Exceeded") && !aiResult.startsWith("JARVIS System Error")) {
                    memoryManager?.processPostResponse(userPrompt = prompt, aiResponse = aiResult)
                }
                aiResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in generateResponseInternal", e)
            "JARVIS Core Exception: ${e.localizedMessage ?: "Connection failure"}. Standing by for retry."
        }
    }

    fun streamResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        memoryContext: String = ""
    ): Flow<String> = flow {
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isEmpty()) {
            emit("JARVIS Core Alert: Gemini API Key is not configured. Please enter your key in Setup or Settings.")
            return@flow
        }

        apiMutex.withLock {
            try {
                val retrievedMemories = memoryManager?.prepareContext(prompt) ?: ""
                val effectivePrompt = if (retrievedMemories.isNotBlank()) {
                    "$retrievedMemories\n\n$prompt"
                } else {
                    prompt
                }

                val rootJson = JSONObject()
                val contentsArray = JSONArray()

                history.takeLast(6).forEach { (userMsg, aiMsg) ->
                    contentsArray.put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", userMsg)))
                    })
                    contentsArray.put(JSONObject().apply {
                        put("role", "model")
                        put("parts", JSONArray().put(JSONObject().put("text", aiMsg)))
                    })
                }

                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", effectivePrompt)))
                })

                rootJson.put("contents", contentsArray)
                val effectiveInstruction = systemInstructionText + memoryContext
                rootJson.put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", effectiveInstruction)))
                })

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?key=$apiKey")
                    .post(rootJson.toString().toRequestBody(mediaType))
                    .build()

                executeStreamWithRetry(request, prompt, history, memoryContext)
            } catch (e: Exception) {
                Log.e(TAG, "Exception in streamResponse", e)
                emit("JARVIS Stream Notice: Connection interrupted. Retrying automatically.")
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun executeCallWithRetry(
        request: Request,
        onSuccess: suspend (okhttp3.Response, String) -> String
    ): String {
        var attempt = 0
        var backoffMs = INITIAL_BACKOFF_MS

        while (attempt <= MAX_RETRIES) {
            try {
                val response = okHttpClient.newCall(request).execute()
                val statusCode = response.code

                if (response.isSuccessful) {
                    val respBodyStr = response.body?.string() ?: ""
                    return onSuccess(response, respBodyStr)
                }

                val respBodyStr = response.body?.string() ?: ""
                val isRateLimitOrQuota = statusCode == 429 ||
                        respBodyStr.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                        respBodyStr.contains("QUOTA_EXCEEDED", ignoreCase = true) ||
                        respBodyStr.contains("quota", ignoreCase = true)

                val isServerError = statusCode in 500..599

                Log.e(TAG, "Gemini API HTTP $statusCode on attempt ${attempt + 1}/${MAX_RETRIES + 1}: $respBodyStr")

                if ((isRateLimitOrQuota || isServerError) && attempt < MAX_RETRIES) {
                    Log.w(TAG, "Rate limit / Server busy (HTTP $statusCode). Applying exponential backoff delay of ${backoffMs}ms before attempt ${attempt + 2}")
                    delay(backoffMs)
                    attempt++
                    backoffMs *= 2 // Exponential backoff: 2s, 4s, 8s
                    continue
                }

                return when {
                    isRateLimitOrQuota -> {
                        "JARVIS Rate Limit Exceeded: Google Gemini API free quota limit reached for your key. Please try again in a few moments or check your API key quota at AI Studio."
                    }
                    statusCode == 401 || statusCode == 403 -> {
                        "JARVIS Authentication Alert: Invalid Gemini API key (HTTP $statusCode). Please update your API key in Settings."
                    }
                    statusCode in 500..599 -> {
                        "JARVIS Server Alert: Neural AI service is currently busy (HTTP $statusCode). Retrying automatically shortly."
                    }
                    else -> {
                        val msg = parseErrorMessage(respBodyStr)
                        "JARVIS Neural Network Notice [HTTP $statusCode]: $msg"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network Exception during Gemini API call attempt ${attempt + 1}/${MAX_RETRIES + 1}", e)
                if (attempt < MAX_RETRIES) {
                    delay(backoffMs)
                    attempt++
                    backoffMs *= 2
                } else {
                    return "JARVIS Network Alert: Connection interrupted (${e.localizedMessage ?: "Unknown network error"}). Standing by for reconnect."
                }
            }
        }
        return "JARVIS Rate Limit Exceeded: Quota limit reached. Standing by for retry."
    }

    private suspend fun FlowCollector<String>.executeStreamWithRetry(
        request: Request,
        prompt: String,
        history: List<Pair<String, String>>,
        memoryContext: String
    ) {
        var attempt = 0
        var backoffMs = INITIAL_BACKOFF_MS

        while (attempt <= MAX_RETRIES) {
            try {
                val response = okHttpClient.newCall(request).execute()
                val statusCode = response.code

                if (response.isSuccessful) {
                    val reader = response.body?.byteStream()?.bufferedReader()
                    var accumulatedText = ""

                    reader?.use { r ->
                        var line: String?
                        while (r.readLine().also { line = it } != null) {
                            val l = line?.trim() ?: continue
                            if (l.contains("\"text\"")) {
                                val chunk = extractTextFromResponse(l)
                                if (chunk.isNotEmpty() && !chunk.startsWith("JARVIS Rate Limit Exceeded") && !chunk.startsWith("JARVIS System Error")) {
                                    accumulatedText += chunk
                                    emit(accumulatedText)
                                }
                            }
                        }
                    }

                    if (accumulatedText.isEmpty()) {
                        val fallback = generateResponseInternal(prompt, null, history, memoryContext)
                        accumulatedText = fallback
                        emit(fallback)
                    } else {
                        memoryManager?.processPostResponse(userPrompt = prompt, aiResponse = accumulatedText)
                    }
                    return
                }

                val respBodyStr = response.body?.string() ?: ""
                val isRateLimitOrQuota = statusCode == 429 ||
                        respBodyStr.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                        respBodyStr.contains("QUOTA_EXCEEDED", ignoreCase = true) ||
                        respBodyStr.contains("quota", ignoreCase = true)

                val isServerError = statusCode in 500..599

                Log.e(TAG, "Gemini Stream API HTTP $statusCode on attempt ${attempt + 1}/${MAX_RETRIES + 1}: $respBodyStr")

                if ((isRateLimitOrQuota || isServerError) && attempt < MAX_RETRIES) {
                    Log.w(TAG, "Rate limit / Server busy (HTTP $statusCode). Applying exponential backoff delay of ${backoffMs}ms before attempt ${attempt + 2}")
                    emit("JARVIS Neural Engine is experiencing high demand (HTTP 429). Retrying automatically in ${backoffMs / 1000}s...")
                    delay(backoffMs)
                    attempt++
                    backoffMs *= 2
                    continue
                }

                val errorMsg = when {
                    isRateLimitOrQuota -> {
                        "JARVIS Rate Limit Exceeded: Google Gemini API quota limit reached for your key. Please try again in a few moments or check your API key quota at AI Studio."
                    }
                    statusCode == 401 || statusCode == 403 -> {
                        "JARVIS Authentication Alert: Invalid Gemini API key (HTTP $statusCode). Please update your API key in Settings."
                    }
                    statusCode in 500..599 -> {
                        "JARVIS Server Alert: Neural AI service is currently busy (HTTP $statusCode). Retrying automatically shortly."
                    }
                    else -> {
                        val msg = parseErrorMessage(respBodyStr)
                        "JARVIS Neural Network Notice [HTTP $statusCode]: $msg"
                    }
                }
                emit(errorMsg)
                return
            } catch (e: Exception) {
                Log.e(TAG, "Network Exception during Gemini Stream API call attempt ${attempt + 1}/${MAX_RETRIES + 1}", e)
                if (attempt < MAX_RETRIES) {
                    emit("JARVIS Network connection fluctuating. Re-establishing neural link...")
                    delay(backoffMs)
                    attempt++
                    backoffMs *= 2
                } else {
                    emit("JARVIS Network Alert: Connection interrupted (${e.localizedMessage ?: "Unknown network error"}). Standing by for reconnect.")
                    return
                }
            }
        }
    }

    private fun parseErrorMessage(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val errorObj = root.optJSONObject("error")
            errorObj?.optString("message") ?: "Request failed"
        } catch (e: Exception) {
            "Request failed"
        }
    }

    private fun extractTextFromResponse(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val text = part?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        sb.append(text)
                    }
                }
            }
            if (sb.isNotEmpty()) sb.toString() else "JARVIS Neural Engine standby. Message acknowledged."
        } catch (e: Exception) {
            val regex = """"text"\s*:\s*"((?:[^"\\]|\\.)*)"""".toRegex()
            val matches = regex.findAll(jsonStr)
            val sb = StringBuilder()
            matches.forEach { match ->
                val rawText = match.groupValues[1]
                val unescaped = rawText
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\r", "")
                    .replace("\\t", "\t")
                sb.append(unescaped)
            }
            if (sb.isNotEmpty()) sb.toString() else ""
        }
    }

    private fun Bitmap.toBase64String(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}

