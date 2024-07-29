package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.util.Log
import android.os.VibrationEffect
import android.os.Vibrator
import com.taehokimmm.hapticvboard_android.HapticMode


class HapticManager(context: Context) {
    private val context: Context = context
    private val serialManager: SerialManager = SerialManager(context)
    private val soundManager: SoundManager = SoundManager(context)

    fun generateHaptic(key: String, hapticMode: HapticMode = HapticMode.NONE){
        if (hapticMode == HapticMode.NONE) return
        // Provide Speech Feedback
        if (hapticMode == HapticMode.VOICE ||
            hapticMode == HapticMode.VOICEPHONEME ||
            hapticMode == HapticMode.VOICETICK
        ) {
            soundManager.speakOut(key)
        }

        if (hapticMode == HapticMode.VOICE) return
        if (hapticMode == HapticMode.TICK ||
            hapticMode == HapticMode.VOICETICK
        ) {
            generateVibration(key)
            return
        }

        // Haptic Tick for Special Keys
        if (key == "Backspace" || key == "Space") {
            generateVibration(key)
            return
        }
        // Provide Phoneme Feedback
        val keyToResourceMap = mapOf(
            'a' to "aa",
            'b' to "b",
            'c' to "k",
            'd' to "d",
            'e' to "eh",
            'f' to "f",
            'g' to "g",
            'h' to "hh",
            'i' to "iy",
            'j' to "g",
            'k' to "k",
            'l' to "l",
            'm' to "m",
            'n' to "n",
            'o' to "ow",
            'p' to "p",
            'q' to "k",
            'r' to "r",
            's' to "s",
            't' to "t",
            'u' to "uw",
            'v' to "v",
            'w' to "uw",
            'x' to "ks",
            'y' to "ey",
            'z' to "z",
        )

        val formattedKey = keyToResourceMap[key[0]]?.uppercase()?.padEnd(8)

        if (formattedKey == null) {
            Log.d("HapticFeedback", "No haptic found for key: $key, skipping...")
            return
        }
        Log.d("HapticFeedback", "Sending haptic for key: $key over serial")
        Log.d("HapticFeedback", "P${formattedKey}WAV")
        serialManager.write("P${formattedKey}WAV\n".toByteArray())

    }

    fun generateVibration(key: String) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                var vibrate: VibrationEffect? = null
                if (key == "Backspace") {
                    vibrate = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                } else if(key == "Space"){
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                } else {
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
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
}