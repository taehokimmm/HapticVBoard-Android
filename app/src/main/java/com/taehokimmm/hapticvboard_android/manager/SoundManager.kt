package com.taehokimmm.hapticvboard_android.manager

import android.R
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


class SoundManager(context: Context) {
    val context:Context = context
    var timer: Timer? = null
    var timerTask: TimerTask? = null
    private lateinit var tts: TextToSpeech

    private lateinit var soundPool: SoundPool
    var correctId: Int = 0
    var wrongId: Int = 0

    init {

        // Define audio attributes for the sound pool
        val audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        // Initialize the sound pool with the defined attributes


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
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(s: String) {
                Log.e("phase3", "on start")
            }

            override fun onDone(s: String) {
                Log.e("phase3", "on done")
            }

            override fun onError(s: String) {
            }
        })
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

    /**
     * Plays the sound for correct answer.
     */
    @Synchronized
    fun playSound(isCorrect: Boolean) {
        var mediaPlayer: MediaPlayer
        if (isCorrect) {
            mediaPlayer =
                MediaPlayer.create(context, com.taehokimmm.hapticvboard_android.R.raw.correct)
        } else {
            mediaPlayer =
                MediaPlayer.create(context, com.taehokimmm.hapticvboard_android.R.raw.wrong)
        }
        mediaPlayer.start()
    }
}
