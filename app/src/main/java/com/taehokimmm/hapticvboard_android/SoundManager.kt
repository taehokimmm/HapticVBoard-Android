package com.taehokimmm.hapticvboard_android

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class SoundManager(context: Context) {
    var timer: Timer? = null
    var timerTask: TimerTask? = null
    private lateinit var tts: TextToSpeech

    init {
        // Init TTS
        tts = TextToSpeech(context) {
            if (it === TextToSpeech.SUCCESS) {
                // Set TTS Language
                val result = tts.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                    Log.e("TTS", "This Language is not supported")
                } else {
                    Log.e("TTS", "SUCCESS")
                }
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }
    }

    /**
     * Speak out the input text
     *
     * @param text The text for which to play the sound.
     */
    fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    /**
     * Speak out the input word first, then each letter
     */
    fun speakWord(word: String){
        speakOut(word)
        var index = 0
        timerTask = object: TimerTask() {
            override fun run() {
                if (index == word.length) {
                    timerTask?.cancel()
                    timer?.cancel()
                    timer?.purge()
                    return
                }
                speakOut(word[index++].toString())
            }
        }
        timer = Timer()
        timer?.schedule(timerTask, 0, 300)
    }

    /**
     * Speak out sentence first, then each word
     */
    fun speakSentence(sentence: String){
        speakOut(sentence)
        val words = sentence.split(" ")
        var index = 0
        timerTask = object: TimerTask() {
            override fun run() {
                if (index == words.size) {
                    timerTask?.cancel()
                    timer?.cancel()
                    timer?.purge()
                    return
                }
                speakOut(words[index++])
            }
        }
        timer = Timer()
        timer?.schedule(timerTask, 0, 500)
    }
}
