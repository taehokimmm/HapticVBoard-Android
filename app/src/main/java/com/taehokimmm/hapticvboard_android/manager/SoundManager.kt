package com.taehokimmm.hapticvboard_android.manager

import android.R.attr.text
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.delay
import java.io.File
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import android.os.Handler
import com.taehokimmm.hapticvboard_android.layout.intro.Location
import com.taehokimmm.hapticvboard_android.layout.intro.PhonemeGroup
import com.taehokimmm.hapticvboard_android.layout.intro.getLocation
import com.taehokimmm.hapticvboard_android.layout.intro.getPhonemeGroup

class SoundManager(context: Context) {
    val context:Context = context
    var timer: Timer? = null
    var timerTask: TimerTask? = null
    private lateinit var tts: TextToSpeech
    private lateinit var ttsKor: TextToSpeech
    private var isFrontLeft: Boolean = true

    private lateinit var soundPool: SoundPool
    var correctId: Int = 0
    var wrongId: Int = 0
    private var mediaPlayer: MediaPlayer? = null
    private var runnable: Runnable? = null
    private var handler: Handler = Handler(Looper.getMainLooper())

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

    fun changeMapping(isFrontLeft1: Boolean) {
        isFrontLeft = isFrontLeft1
    }

    fun speakOutKeyboard(key: String) {
        tts.setSpeechRate(1F)
        tts.setLanguage(Locale.ENGLISH)
        tts.speak(key, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakOutPhonemeInfo(key: String) {
        val loc: Location = getLocation(key)
        val group: PhonemeGroup = getPhonemeGroup(key)
        speakOutKor(group.group)
        speakOutKor(loc.location,TextToSpeech.QUEUE_ADD)
    }

    fun speakOut(text: String, mode:Int = TextToSpeech.QUEUE_FLUSH, rate:Float = 1.0f) {
        tts.speak(text, mode, null, null)
    }

    fun speakOutKor(text: String, mode:Int = TextToSpeech.QUEUE_FLUSH) {
        ttsKor.speak(text, mode, null, null)
    }

    fun speakOutChar(key: String) {

        var avaiation: Map<String, String> = mapOf(
            "a" to "apple",
            "b" to "boy",
            "c" to "cat",
            "d" to "dog",
            "e" to "engine",
            "f" to "front",
            "g" to "game",
            "h" to "hotel",
            "i" to "inside",
            "j" to "juice",
            "k" to "korea",
            "l" to "lemon",
            "m" to "mouth",
            "n" to "nose",
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
        speakOut(key, TextToSpeech.QUEUE_FLUSH)
        speakOut("", TextToSpeech.QUEUE_ADD)
        speakOut("", TextToSpeech.QUEUE_ADD)
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
            "a" to if(isFrontLeft) R.raw.phoneme_a else R.raw.phoneme_e_reverse,
            "b" to if(isFrontLeft) R.raw.phoneme_b else R.raw.phoneme_b_reverse,
            "c" to if(isFrontLeft) R.raw.phoneme_c else R.raw.phoneme_c_reverse,
            "d" to if(isFrontLeft) R.raw.phoneme_d else R.raw.phoneme_d_reverse,
            "e" to if(isFrontLeft) R.raw.phoneme_e else R.raw.phoneme_e_reverse,
            "f" to if(isFrontLeft) R.raw.phoneme_f else R.raw.phoneme_f_reverse,
            "g" to if(isFrontLeft) R.raw.phoneme_g else R.raw.phoneme_g_reverse,
            "h" to if(isFrontLeft) R.raw.phoneme_h else R.raw.phoneme_h_reverse,
            "i" to if(isFrontLeft) R.raw.phoneme_i else R.raw.phoneme_i_reverse,
            "j" to if(isFrontLeft) R.raw.phoneme_j else R.raw.phoneme_j_reverse,
            "k" to if(isFrontLeft) R.raw.phoneme_k else R.raw.phoneme_k_reverse,
            "l" to if(isFrontLeft) R.raw.phoneme_l else R.raw.phoneme_l_reverse,
            "m" to if(isFrontLeft) R.raw.phoneme_m else R.raw.phoneme_m_reverse,
            "n" to if(isFrontLeft) R.raw.phoneme_n else R.raw.phoneme_n_reverse,
            "o" to if(isFrontLeft) R.raw.phoneme_o else R.raw.phoneme_o_reverse,
            "p" to if(isFrontLeft) R.raw.phoneme_p else R.raw.phoneme_p_reverse,
            "q" to if(isFrontLeft) R.raw.phoneme_q else R.raw.phoneme_q_reverse,
            "r" to if(isFrontLeft) R.raw.phoneme_r else R.raw.phoneme_r_reverse,
            "s" to if(isFrontLeft) R.raw.phoneme_s else R.raw.phoneme_s_reverse,
            "t" to if(isFrontLeft) R.raw.phoneme_t else R.raw.phoneme_t_reverse,
            "u" to if(isFrontLeft) R.raw.phoneme_u else R.raw.phoneme_u_reverse,
            "v" to if(isFrontLeft) R.raw.phoneme_v else R.raw.phoneme_v_reverse,
            "w" to if(isFrontLeft) R.raw.phoneme_w else R.raw.phoneme_w_reverse,
            "x" to if(isFrontLeft) R.raw.phoneme_x else R.raw.phoneme_x_reverse,
            "y" to if(isFrontLeft) R.raw.phoneme_y else R.raw.phoneme_y_reverse,
            "z" to if(isFrontLeft) R.raw.phoneme_z else R.raw.phoneme_z_reverse,

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
