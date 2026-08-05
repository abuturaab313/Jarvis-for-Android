package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText

    private val _audioWaveLevel = MutableStateFlow(0.2f)
    val audioWaveLevel: StateFlow<Float> = _audioWaveLevel

    private val _continuousWakeWord = MutableStateFlow(false)
    val continuousWakeWord: StateFlow<Boolean> = _continuousWakeWord

    init {
        tts = TextToSpeech(context, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setPitch(0.95f) // Deep futuristic voice pitch
            tts?.setSpeechRate(1.05f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _audioWaveLevel.value = 0.8f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveLevel.value = 0.2f
                    if (_continuousWakeWord.value) {
                        startListening()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _audioWaveLevel.value = 0.2f
                }
            })
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
                        _audioWaveLevel.value = 0.6f
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
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioWaveLevel.value = 0.2f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            _speechText.value = recognized
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
        if (tts != null) {
            val cleanText = text.replace(Regex("[*#_`]"), "") // Remove markdown format for clean TTS
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_VOICE_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        _audioWaveLevel.value = 0.2f
    }

    fun startListening() {
        if (speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
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
    }

    fun clearSpeechText() {
        _speechText.value = ""
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
