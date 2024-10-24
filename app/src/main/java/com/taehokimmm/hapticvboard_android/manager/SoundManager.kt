package com.taehokimmm.hapticvboard_android.manager

import android.R.attr.text
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.delay
import java.io.File
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


class SoundManager(context: Context) {
    val context:Context = context
    var timer: Timer? = null
    var timerTask: TimerTask? = null
    private lateinit var tts: TextToSpeech
    private lateinit var ttsKor: TextToSpeech

    private lateinit var soundPool: SoundPool
    var correctId: Int = 0
    var wrongId: Int = 0
    private var mediaPlayer: MediaPlayer? = null

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
            tts?.addEarcon("beep", "com.taehokimmm.hapticvboard_android", R.raw.beep)
            tts?.addEarcon("silent", "com.taehokimmm.hapticvboard_android", R.raw.silent_quarter)
        }

        ttsKor = TextToSpeech(context) {
            if (it === TextToSpeech.SUCCESS) {

                // Set TTS Language
                val result = ttsKor.setLanguage(Locale.KOREAN)

                // Export TTS to wav file
                for (letter in 'a' .. 'z') {
                    // Output file
                    val outputFile: File = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        "tts_" + letter +".wav")
                    Log.d("TTSMANAGER", outputFile.absolutePath)
                    // Synthesizing to file
                    val params = Bundle()
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts1")
                    ttsKor.synthesizeToFile(letter.toString(), params, outputFile, "tts1")

                }


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

    fun playEarcon(earcon: String) {
        tts?.playEarcon(earcon, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun playMediaPlayer() {
        mediaPlayer?.setOnCompletionListener {
            releaseMediaPlayer()  // Release the MediaPlayer once the sound has finished playing
        }

        // Start playback
        mediaPlayer?.start()
    }
    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    /**
     * Speak out the input text
     *
     * @param text The text for which to play the sound.
     */
    fun stop() {
        tts.stop()
        ttsKor.stop()
    }
    fun speakOutKeyboard(key: String) {
        tts.setSpeechRate(2F)
        tts.speak(key, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakOutKeyboardPhoneme(key: String) {
        tts.setSpeechRate(2F)
        tts.speak(key, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakOut(text: String, mode:Int = TextToSpeech.QUEUE_FLUSH) {
        tts.speak(text, mode, null, null)
    }

    fun speakOutKor(text: String) {
        ttsKor.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakOutChar(key: String) {
        var keyToResource: Map<String, Int> = mapOf(
            "a" to R.raw.tts_a,
            "b" to R.raw.tts_b,
            "c" to R.raw.tts_c,
            "d" to R.raw.tts_d,
            "e" to R.raw.tts_e,
            "f" to R.raw.tts_f,
            "g" to R.raw.tts_g,
            "h" to R.raw.tts_h,
            "i" to R.raw.tts_i,
            "j" to R.raw.tts_j,
            "k" to R.raw.tts_k,
            "l" to R.raw.tts_l,
            "m" to R.raw.tts_m,
            "n" to R.raw.tts_n,
            "o" to R.raw.tts_o,
            "p" to R.raw.tts_p,
            "q" to R.raw.tts_q,
            "r" to R.raw.tts_r,
            "s" to R.raw.tts_s,
            "t" to R.raw.tts_t,
            "u" to R.raw.tts_u,
            "v" to R.raw.tts_v,
            "w" to R.raw.tts_w,
            "x" to R.raw.tts_x,
            "y" to R.raw.tts_y,
            "z" to R.raw.tts_z
        )

        var avaiation: Map<String, String> = mapOf(
            "a" to "apple",
            "b" to "boy",
            "c" to "cat",
            "d" to "dog",
            "e" to "engine",
            "f" to "five",
            "g" to "game",
            "h" to "hotel",
            "i" to "image",
            "j" to "juice",
            "k" to "korea",
            "l" to "lemon",
            "m" to "mike",
            "n" to "number",
            "o" to "ocean",
            "p" to "people",
            "q" to "queen",
            "r" to "red",
            "s" to "summer",
            "t" to "time",
            "u" to "unicorn",
            "v" to "video",
            "w" to "window",
            "x" to "x-ray",
            "y" to "yesterday",
            "z" to "zero",
        )
        speakOut(key, TextToSpeech.QUEUE_ADD)
        avaiation[key]?.let { speakOut(it, TextToSpeech.QUEUE_ADD) }
    }


    /**
     * Speak out the input word first, then each letter
     */
    fun speakWord(word: String){
        tts.setSpeechRate(0.5f)
        tts.speak(word, TextToSpeech.QUEUE_ADD, null, null)

        tts.setSpeechRate(1f)
        delay(
            {
                for (index in 0 until word.length) {
                    tts.speak(word[index].toString(), TextToSpeech.QUEUE_ADD, null, null)
                }
            },
            500
        )
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
        releaseMediaPlayer()
        if (isCorrect) {
            releaseMediaPlayer()
            mediaPlayer =
                MediaPlayer.create(context, R.raw.correct)
        } else {
            releaseMediaPlayer()
            mediaPlayer =
                MediaPlayer.create(context, R.raw.wrong)
        }
        playMediaPlayer()
    }

    @Synchronized
    fun playPhoneme(key: String) {
        var keyToResource: Map<String, Int> = mapOf(
            "a" to R.raw.phoneme_a,
            "b" to R.raw.phoneme_b,
            "c" to R.raw.phoneme_q,
            "d" to R.raw.phoneme_d,
            "e" to R.raw.phoneme_e,
            "f" to R.raw.phoneme_f,
            "g" to R.raw.phoneme_g,
            "h" to R.raw.phoneme_h,
            "i" to R.raw.phoneme_i,
            "j" to R.raw.phoneme_j,
            "k" to R.raw.phoneme_q,
            "l" to R.raw.phoneme_l,
            "m" to R.raw.phoneme_m,
            "n" to R.raw.phoneme_n,
            "o" to R.raw.phoneme_o,
            "p" to R.raw.phoneme_p,
            "q" to R.raw.phoneme_q,
            "r" to R.raw.phoneme_r,
            "s" to R.raw.phoneme_s,
            "t" to R.raw.phoneme_t,
            "u" to R.raw.phoneme_u,
            "v" to R.raw.phoneme_v,
            "w" to R.raw.phoneme_w,
            "x" to R.raw.phoneme_x,
            "y" to R.raw.phoneme_y,
            "z" to R.raw.phoneme_z
        )
        releaseMediaPlayer()
        mediaPlayer =
            keyToResource[key]?.let { MediaPlayer.create(context, it) }!!
        playMediaPlayer()
    }

    @Synchronized
    fun playLocation(key: String) {
        val phonemeGroups = listOf(
            listOf("p", "b", "f", "v", "m", "e", "i"),
            listOf("k", "c", "q", "g", "n", "r", "h", "a", "o", "u"),
            listOf("t", "d", "s", "z", "l", "x", "j"),
            listOf("w"),
            listOf("y")
        )
        val names = listOf("위", "아래", "위아래 동시에", "위에서 아래", "아래에서 위")
        phonemeGroups.forEachIndexed{index, group ->
            if (group.contains(key)) {
                speakOutKor(names[index])
            }
        }
    }
}
