package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceEngine"
    }

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText

    private val _audioWaveLevel = MutableStateFlow(0.2f)
    val audioWaveLevel: StateFlow<Float> = _audioWaveLevel

    private val _continuousWakeWord = MutableStateFlow(true)
    val continuousWakeWord: StateFlow<Boolean> = _continuousWakeWord

    init {
        tts = TextToSpeech(context, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            configureMaleVoiceAndMultilingual()
            tts?.setPitch(0.88f) // Deep confident JARVIS male tone
            tts?.setSpeechRate(1.02f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _audioWaveLevel.value = 0.8f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveLevel.value = 0.2f
                    if (_continuousWakeWord.value) {
                        mainHandler.postDelayed({
                            startListening()
                        }, 300)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveLevel.value = 0.2f
                    if (_continuousWakeWord.value) {
                        mainHandler.postDelayed({
                            startListening()
                        }, 500)
                    }
                }
            })
        }
    }

    private fun configureMaleVoiceAndMultilingual() {
        try {
            val voices = tts?.voices ?: return
            var chosenVoice: Voice? = null

            // Search for best natural male voice
            for (v in voices) {
                val nameLower = v.name.lowercase()
                if (nameLower.contains("male") && !nameLower.contains("female")) {
                    chosenVoice = v
                    break
                }
            }

            // Fallback to high quality English or Indian male voice candidate
            if (chosenVoice == null) {
                for (v in voices) {
                    val nameLower = v.name.lowercase()
                    if (nameLower.contains("en-us") || nameLower.contains("en-in") || nameLower.contains("hi-in")) {
                        if (!nameLower.contains("female") && !nameLower.contains("network")) {
                            chosenVoice = v
                            break
                        }
                    }
                }
            }

            if (chosenVoice != null) {
                tts?.voice = chosenVoice
                Log.d(TAG, "JARVIS Selected Natural Male Voice: ${chosenVoice.name}")
            } else {
                tts?.language = Locale.US
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring male voice: ${e.localizedMessage}")
            tts?.language = Locale.US
        }
    }

    private fun detectAndSetLocaleForText(text: String) {
        val targetLocale = when {
            // Devanagari script (Hindi & Marathi)
            text.any { it.code in 0x0900..0x097F } -> Locale("hi", "IN")
            // Arabic script (Urdu)
            text.any { it.code in 0x0600..0x06FF } -> Locale("ur", "PK")
            else -> Locale("en", "US")
        }

        try {
            tts?.language = targetLocale
        } catch (e: Exception) {
            tts?.language = Locale.US
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _audioWaveLevel.value = 0.7f
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = (rmsdB / 12f).coerceIn(0.1f, 1.0f)
                        _audioWaveLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioWaveLevel.value = 0.2f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioWaveLevel.value = 0.2f
                        Log.d(TAG, "SpeechRecognizer Error code: $error")

                        // Auto restart listening if hands-free wake word mode is active
                        if (_continuousWakeWord.value && !_isSpeaking.value) {
                            mainHandler.postDelayed({
                                startListening()
                            }, 500)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioWaveLevel.value = 0.2f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            _speechText.value = recognized
                        } else if (_continuousWakeWord.value && !_isSpeaking.value) {
                            mainHandler.postDelayed({ startListening() }, 500)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _speechText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun speak(text: String) {
        if (tts != null && text.isNotBlank()) {
            val cleanText = text.replace(Regex("[*#_`]"), "").trim()
            detectAndSetLocaleForText(cleanText)
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_VOICE_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        _audioWaveLevel.value = 0.2f
    }

    fun startListening() {
        if (_isSpeaking.value) return // Don't interrupt TTS output
        if (speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                // Silence thresholds for better accuracy and noise filtering
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "hi-IN", "mr-IN", "ur-PK"))
            }
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
                Log.e(TAG, "Failed to start speech recognition: ${e.localizedMessage}")
            }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        _isListening.value = false
        _audioWaveLevel.value = 0.2f
    }

    fun toggleWakeWord(enabled: Boolean) {
        _continuousWakeWord.value = enabled
        if (enabled && !_isListening.value && !_isSpeaking.value) {
            startListening()
        }
    }

    fun clearSpeechText() {
        _speechText.value = ""
    }

    fun release() {
        mainHandler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}

