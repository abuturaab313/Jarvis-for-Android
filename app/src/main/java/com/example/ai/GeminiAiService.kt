package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiAiService(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemInstructionText = """
        You are JARVIS (Just A Rather Very Intelligent System), a highly advanced, ultra-intelligent AI Operating System Assistant inspired by futuristic HUD technology.
        Your tone is polite, crisp, professional, extremely helpful, and slightly humorous when appropriate, addressing the user as "Boss" or "Sir/Ma'am".
        You have direct control over Mobile Skills, System Telemetry, Vision Analysis, and Automation Routines.
        When answering questions, provide precise, structured responses with markdown formatting or code snippets when helpful.
    """.trimIndent()

    suspend fun generateResponse(
        prompt: String,
        bitmap: Bitmap? = null,
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "JARVIS Core Alert: Gemini API key is not configured in Secrets panel. Please set GEMINI_API_KEY to enable neural speech and response engine."
        }

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()

            // Append history
            history.takeLast(6).forEach { (userMsg, aiMsg) ->
                val userContent = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userMsg)))
                }
                val modelContent = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", aiMsg)))
                }
                contentsArray.put(userContent)
                contentsArray.put(modelContent)
            }

            // Current user prompt parts
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))

            if (bitmap != null) {
                val base64Image = bitmap.toBase64String()
                val inlineData = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                }
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }

            val currentContent = JSONObject().apply {
                put("role", "user")
                put("parts", currentParts)
            }
            contentsArray.put(currentContent)
            rootJson.put("contents", contentsArray)

            // System Instruction
            val sysInstruction = JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            }
            rootJson.put("systemInstruction", sysInstruction)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(rootJson.toString().toRequestBody(mediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val respBodyStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "JARVIS System Error [HTTP ${response.code}]: Unable to reach neural network. ${response.message}"
            }

            extractTextFromResponse(respBodyStr)
        } catch (e: Exception) {
            "JARVIS Core Exception: ${e.localizedMessage ?: "Connection failure"}. Standing by for retry."
        }
    }

    fun streamResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList()
    ): Flow<String> = flow {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            emit("JARVIS Core Alert: Gemini API Key required. Standby for user authentication.")
            return@flow
        }

        try {
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
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            })

            rootJson.put("contents", contentsArray)
            rootJson.put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            })

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?key=$apiKey")
                .post(rootJson.toString().toRequestBody(mediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val reader = response.body?.byteStream()?.bufferedReader()
            var accumulatedText = ""

            reader?.use { r ->
                var line: String?
                while (r.readLine().also { line = it } != null) {
                    val l = line?.trim() ?: continue
                    if (l.contains("\"text\"")) {
                        val chunk = extractTextFromResponse(l)
                        if (chunk.isNotEmpty() && !chunk.startsWith("JARVIS System Error")) {
                            accumulatedText += chunk
                            emit(accumulatedText)
                        }
                    }
                }
            }

            if (accumulatedText.isEmpty()) {
                val fallback = generateResponse(prompt = prompt, history = history)
                emit(fallback)
            }
        } catch (e: Exception) {
            emit("JARVIS Stream Error: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)

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
            // RegEx fallback for stream chunk fragments
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
