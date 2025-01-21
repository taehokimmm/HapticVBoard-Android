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
import androidx.compose.ui.text.toUpperCase
import androidx.core.os.postDelayed


class HapticManager(context: Context) {
    private val context: Context = context
    private val serialManager: SerialManager = SerialManager(context)
    private val soundManager: SoundManager = SoundManager(context)
    private var isFrontLeft: Boolean = true

    // Class-level variable to store the previous timestamp
    private var lastTime: Long = System.currentTimeMillis()
    private var runnable: Runnable? = null
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private var lastLetter: String = ""
    @Synchronized
    fun generateHaptic(key: String, hapticMode: HapticMode = HapticMode.NONE, isPress: Boolean = false) {
        if (hapticMode == HapticMode.NONE) return
        // Provide Speech Feedback
        if (hapticMode == HapticMode.VOICE ||
            hapticMode == HapticMode.VOICEPHONEME
        ) {
            if (isPress) soundManager.speakOutChar(key)
            else soundManager.speakOutKeyboard(key)
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

        var formattedKey = formatKey(key)

        if (formattedKey == null) {
            Log.d("HapticFeedback", "No haptic found for key: $key, skipping...")
            return
        }

        serialManager.read()

        val delta = System.currentTimeMillis() - lastTime
        lastTime = System.currentTimeMillis()
        Log.d("HapticFeedback", "$key, $delta ${delta<200}")

//        runnable?.let { handler?.removeCallbacks(it) }
//        if (delta < 300) {
//            runnable = delay({serialManager.write("q\n".toByteArray())}, 50)
//            runnable = delay({serialManager.write("P${formattedKey}WAV\n".toByteArray())}, 80)
//        } else {
        serialManager.write("P${formattedKey}WAV\n".toByteArray())
//        }
        lastLetter = key
    }

    fun formatKey(key:String): String {
        return key.uppercase().padEnd(8)
        val back = listOf("h", "g", "c", "k", "q", "a", "e", "i", "n")
        val front = listOf("f", "v", "b", "p", "m", "u", "s", "z")
        val both = listOf("t", "d", "j", "l", "r", "x", "o", "w", "y")

        var formattedKey = key.lowercase()
        if (back.contains(key)) {
            formattedKey = if (isFrontLeft) key+"_r" else key+"_l"
        } else if(front.contains(key)) {
            formattedKey = if (isFrontLeft) key+"_l" else key+"_r"
        }
        return formattedKey.uppercase().padEnd(8)
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

    fun changeMapping(isFrontLeft1: Boolean) {
        isFrontLeft = isFrontLeft1
    }

    fun getMapping(): String {
        return if (isFrontLeft) "left" else "right"
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