package com.taehokimmm.hapticvboard_android

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi

class VibrationManager (context: Context){
    val context:Context = context
    fun vibratePhone(key:String) {
        if (key != "Backspace" && key != "Space") {
            return
        }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                var vibrate: VibrationEffect? = null
                Log.e("VIBRATION", key)
                if (key == "Backspace") {
                    vibrate = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                } else if(key == "Space"){
                    vibrate = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                }
                vibrator.vibrate(vibrate)
            } else {
                // Deprecated in API 26
                vibrator.vibrate(100)
            }
        }
    }

}