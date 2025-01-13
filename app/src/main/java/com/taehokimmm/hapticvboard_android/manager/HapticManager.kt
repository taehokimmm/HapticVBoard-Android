package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.delay
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.os.postDelayed


class HapticManager(context: Context) {
    private val context: Context = context
    private val serialManager: SerialManager = SerialManager(context)
    private val soundManager: SoundManager = SoundManager(context)
    val handler = Handler(Looper.getMainLooper())

    @Synchronized
    fun generateHaptic(key: String, hapticMode: HapticMode = HapticMode.NONE) {
        if (hapticMode == HapticMode.NONE) return
        // Provide Speech Feedback
        if (hapticMode == HapticMode.VOICE ||
            hapticMode == HapticMode.VOICEPHONEME
        ) {
            soundManager.speakOutKeyboard(key)
        }

        if (hapticMode == HapticMode.VOICE
        ) {
            generateVibration(key, true)
            return
        }

        // Haptic Tick for Special Keys
        if (key == "Shift"||key == "delete"||key == "Space") {
            generateVibration(key)
            return

        }

        var formattedKey = key[0]?.uppercase()?.padEnd(8)

        if (formattedKey == null) {
            Log.d("HapticFeedback", "No haptic found for key: $key, skipping...")
            return
        }
        Log.d("HapticFeedback", "$key, $formattedKey")

        serialManager.write("q\n".toByteArray())
        serialManager.write("P${formattedKey}WAV\n".toByteArray())
    }

    fun getRow(key: String): Int {
        val phonemeGroups = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m", "Shift", "delete"),
            listOf("Space")
        )
        var idx = -1
        phonemeGroups.forEachIndexed( {index, group ->
            if (group.contains(key)) idx = index
        })
        return idx
    }

    fun generateVibration(key: String, isAllTick: Boolean = false) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                if (isAllTick) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    var vibrate: VibrationEffect? = null
                    if (key == "Space"){
                        vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    }else if(key == "delete"){
                        vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    } else if (key == "Out of Bounds") {
                        vibrate = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                    } else {
                        vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    }
                    vibrator.vibrate(vibrate)
                }

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