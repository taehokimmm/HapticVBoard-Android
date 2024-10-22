package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.HapticGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.R
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay


class HapticManager(context: Context) {
    private val context: Context = context
    private val serialManager: SerialManager = SerialManager(context)
    private val soundManager: SoundManager = SoundManager(context)

    @Synchronized
    fun generateHaptic(key: String, hapticMode: HapticMode = HapticMode.NONE) {
        if (hapticMode == HapticMode.NONE) return
        // Provide Speech Feedback
        if (hapticMode == HapticMode.VOICE ||
            hapticMode == HapticMode.VOICEPHONEME ||
            hapticMode == HapticMode.VOICETICK ||
            hapticMode == HapticMode.VOICEPHONEMETICK
        ) {
            soundManager.speakOutKeyboard(key)
        }

        if (hapticMode == HapticMode.VOICE) return
        if (hapticMode == HapticMode.TICK ||
            hapticMode == HapticMode.VOICETICK
        ) {
            generateVibration(key)
            return
        }
        // Haptic Tick for Special Keys
        if (key == "Backspace" || key == "Space" || key == "Replay") {
            generateVibration(key)
            return
        }
        val formattedKey = key[0]?.uppercase()?.padEnd(8)

        if (formattedKey == null) {
            Log.d("HapticFeedback", "No haptic found for key: $key, skipping...")
            return
        }
        Log.d("HapticFeedback", "Sending haptic for key: $key over serial")
        Log.d("HapticFeedback", "P${formattedKey}WAV")

        if (hapticMode == HapticMode.VOICEPHONEMETICK && getRow(key) == 1) {
            generateVibration(key)
            delay(
                {serialManager.write("P${formattedKey}WAV\n".toByteArray())}, 50)
        } else {
            serialManager.write("P${formattedKey}WAV\n".toByteArray())

        }
    }


    fun getRow(key: String): Int {
        val phonemeGroups = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m", "Shift", "Backspace"),
            listOf("Space")
        )
        var idx = -1
        phonemeGroups.forEachIndexed( {index, group ->
            if (group.contains(key)) idx = index
        })
        return idx
    }

    fun generateVibration(key: String) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                var vibrate: VibrationEffect? = null
                if (key == "Backspace"){
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else if(key == "Space"){
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else if(key == "Replay"){
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                } else if (key == "Out of Bounds") {
                    vibrate = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                } else if (key == "rowchanged") {
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                } else {
                    vibrate = VibrationEffect.createOneShot(10, 50)
                }
                vibrator.vibrate(vibrate)


            } else {
                // Deprecated in API 26
                vibrator.vibrate(100)
            }
        }
    }

    fun connect(){
        serialManager.connect()
    }

    fun isOpen(): Boolean {
        return serialManager.isOpen()
    }

    fun setVolumeUp() {
        serialManager.write("+\n".toByteArray())
    }

    fun setVolumeDown() {
        serialManager.write("-\n".toByteArray())
    }


}