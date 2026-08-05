package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.automation.AutomationEngine
import com.example.database.AppDatabase
import com.example.database.JarvisRepository
import com.example.memory.*
import com.example.mobile.MobileSkillsManager
import com.example.models.*
import com.example.utils.SystemMonitor
import com.example.voice.VoiceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NavigationScreen {
    SPLASH, API_KEY_SETUP, HOME, CHAT, VOICE, VISION, SKILLS, ROUTINES, DASHBOARD, SECURITY, SETTINGS
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = JarvisRepository(db)
    val memoryRepository = MemoryRepository(db.memoryDao())
    val memoryManager = MemoryManager(memoryRepository)
    val aiService = GeminiAiService(application, memoryManager)
    val voiceEngine = VoiceEngine(application)
    val skillsManager = MobileSkillsManager(application)
    val systemMonitor = SystemMonitor(application)
    val automationEngine = AutomationEngine(application, repository, skillsManager, systemMonitor)

    // API Key Management
    fun hasApiKey(): Boolean = aiService.apiKeyManager.hasApiKey()
    fun getApiKey(): String = aiService.apiKeyManager.getApiKey()
    fun getMaskedApiKey(): String = aiService.apiKeyManager.getMaskedApiKey()

    fun validateAndSaveApiKey(apiKey: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = aiService.validateApiKey(apiKey)
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    aiService.apiKeyManager.saveApiKey(apiKey)
                    onSuccess()
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Invalid API Key"
                    onError(err)
                }
            }
        }
    }

    fun deleteApiKey() {
        aiService.apiKeyManager.clearApiKey()
    }

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(NavigationScreen.SPLASH)
    val currentScreen: StateFlow<NavigationScreen> = _currentScreen

    // Long-Term Memories Flow
    val memories: StateFlow<List<MemoryEntity>> = memoryManager.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // System Metrics
    val systemMetrics: StateFlow<SystemMetrics> = systemMonitor.getMetricsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), systemMonitor.getMetrics())

    // Database Flows
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<Routine>> = repository.routines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskItem>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analyticsLogs: StateFlow<List<SystemAnalyticsLog>> = repository.analyticsLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vision Analysis State
    private val _visionResult = MutableStateFlow(VisionAnalysisResult())
    val visionResult: StateFlow<VisionAnalysisResult> = _visionResult

    // Active Streaming Chat Buffer
    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing

    init {
        // Pre-populate default JARVIS routines if empty
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.routines.first()
            if (existing.isEmpty()) {
                repository.addRoutine(
                    Routine(
                        title = "Low Battery Power Saver",
                        triggerType = TriggerType.BATTERY,
                        triggerCondition = "Battery < 15%",
                        actionCommand = "Dim Brightness & Enable Power Saver Mode"
                    )
                )
                repository.addRoutine(
                    Routine(
                        title = "Morning AI Briefing",
                        triggerType = TriggerType.TIME,
                        triggerCondition = "08:00 AM",
                        actionCommand = "Read Weather, Agenda & System Telemetry"
                    )
                )
            }
        }

        // Listen to voice engine speech output to auto-trigger JARVIS prompt
        viewModelScope.launch {
            voiceEngine.speechText.collect { recognized ->
                if (recognized.isNotBlank()) {
                    sendUserMessage(recognized)
                    voiceEngine.clearSpeechText()
                }
            }
        }
    }

    private var lastSentText: String = ""
    private var lastSentTime: Long = 0L

    fun navigateTo(screen: NavigationScreen) {
        _currentScreen.value = screen
    }

    fun sendUserMessage(text: String, imageBitmap: Bitmap? = null) {
        val cleanText = text.trim()
        if (cleanText.isBlank() && imageBitmap == null) return

        val now = System.currentTimeMillis()
        if (cleanText == lastSentText && (now - lastSentTime) < 2000L) {
            android.util.Log.d("JarvisViewModel", "Ignoring duplicate rapid message: $cleanText")
            return
        }
        lastSentText = cleanText
        lastSentTime = now

        viewModelScope.launch(Dispatchers.IO) {
            _isAiProcessing.value = true

            try {
                // Save user message
                repository.sendChatMessage(
                    ChatMessage(sender = Sender.USER, content = cleanText)
                )

                // Check if user is asking for a mobile skill action (e.g., "flashlight", "wifi", "call")
                val skillResponse = handleSkillCommandsIfMatch(cleanText)
                if (skillResponse != null) {
                    repository.sendChatMessage(
                        ChatMessage(sender = Sender.JARVIS, content = skillResponse)
                    )
                    voiceEngine.speak(skillResponse)
                    return@launch
                }

                // Otherwise, route through Gemini Neural AI Engine
                val historyPairs = chatMessages.value.map {
                    (if (it.sender == Sender.USER) "User: " else "JARVIS: ") to it.content
                }

                // Step 1: Long-term memory retrieval for input prompt
                val memoryContext = memoryManager.prepareContext(cleanText)

                _streamingText.value = "JARVIS Core Retrieving Memories & Analyzing..."

                // Step 2: Generate response with injected memory context
                aiService.streamResponse(prompt = cleanText, history = historyPairs, memoryContext = memoryContext).collect { chunk ->
                    _streamingText.value = chunk
                }

                val finalReply = _streamingText.value.ifEmpty { "JARVIS Core standing by. Request processed." }

                repository.sendChatMessage(
                    ChatMessage(sender = Sender.JARVIS, content = finalReply)
                )

                // Step 3: Score and store important new long-term memories
                memoryManager.processPostResponse(userPrompt = cleanText, aiResponse = finalReply)

                _streamingText.value = ""

                // Speak response if voice active or screen is VOICE
                if (voiceEngine.isListening.value || currentScreen.value == NavigationScreen.VOICE) {
                    voiceEngine.speak(finalReply)
                }
            } catch (e: Exception) {
                android.util.Log.e("JarvisViewModel", "Error in sendUserMessage", e)
                val errorMsg = "JARVIS Notice: Request interrupted. (${e.localizedMessage ?: "Unknown error"})"
                repository.sendChatMessage(
                    ChatMessage(sender = Sender.JARVIS, content = errorMsg)
                )
                voiceEngine.speak("JARVIS notice: Request interrupted. Standing by.")
            } finally {
                _isAiProcessing.value = false
            }
        }
    }

    private fun handleSkillCommandsIfMatch(prompt: String): String? {
        val lower = prompt.lowercase()
        return when {
            lower.contains("flashlight") || lower.contains("torch") -> skillsManager.executeSkill("flashlight")
            lower.contains("wifi") || lower.contains("wi-fi") -> skillsManager.executeSkill("wifi")
            lower.contains("bluetooth") -> skillsManager.executeSkill("bluetooth")
            lower.contains("hotspot") -> skillsManager.executeSkill("hotspot")
            lower.contains("volume") || lower.contains("mute") -> skillsManager.executeSkill("volume")
            lower.contains("brightness") -> skillsManager.executeSkill("brightness")
            lower.contains("calculator") -> skillsManager.executeSkill("calculator")
            lower.contains("calendar") -> skillsManager.executeSkill("calendar")
            lower.contains("gallery") || lower.contains("photos") -> skillsManager.executeSkill("gallery")
            lower.contains("files") || lower.contains("downloads") -> skillsManager.executeSkill("files")
            lower.startsWith("open ") -> skillsManager.executeSkill("app_launcher", prompt.removePrefix("open ").trim())
            lower.startsWith("call ") -> skillsManager.executeSkill("phone", prompt.removePrefix("call ").trim())
            lower.startsWith("map ") || lower.startsWith("navigate to ") -> skillsManager.executeSkill("maps", prompt.substringAfter("to "))
            else -> null
        }
    }

    fun analyzeImageWithGemini(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAiProcessing.value = true
            val prompt = "Analyze this image thoroughly in high-tech JARVIS HUD style. Identify key objects, text, OCR data, and provide an actionable AI summary."
            val memoryContext = memoryManager.prepareContext("vision camera image analysis")
            val response = aiService.generateResponse(prompt, bitmap, memoryContext = memoryContext)

            _visionResult.value = VisionAnalysisResult(
                timestamp = System.currentTimeMillis(),
                recognizedText = "OCR Scan Complete",
                labels = listOf("HUD Target Acquired", "High Confidence Match", "Neural Matrix Ready"),
                summary = response
            )
            memoryManager.processPostResponse(userPrompt = "Vision camera scan analysis", aiResponse = response)
            _isAiProcessing.value = false
        }
    }

    fun addRoutine(title: String, triggerType: TriggerType, condition: String, action: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRoutine(Routine(title = title, triggerType = triggerType, triggerCondition = condition, actionCommand = action))
        }
    }

    fun toggleRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) { repository.toggleRoutine(routine) }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteRoutine(routine) }
    }

    fun addTask(title: String, priority: TaskPriority) {
        viewModelScope.launch(Dispatchers.IO) { repository.addTask(TaskItem(title = title, priority = priority)) }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch(Dispatchers.IO) { repository.toggleTaskCompleted(task) }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteTask(taskId) }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) { repository.clearChatHistory() }
    }

    fun addMemory(content: String, category: String = "USER_PREFERENCE", importanceScore: Float = 0.8f) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryManager.addMemory(content = content, category = category, importanceScore = importanceScore)
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            memoryManager.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch(Dispatchers.IO) {
            memoryManager.clearAllMemories()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}
