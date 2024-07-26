package com.taehokimmm.hapticvboard_android

import android.content.Context
import android.util.Log

class HapticManager(context: Context) {

    private val serialManager:SerialManager = SerialManager(context)

    fun generateHaptic(key: String){
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

    fun connect(){
        serialManager.connect()
    }

    fun isOpen(): Boolean {
        return serialManager.isOpen()
    }
}