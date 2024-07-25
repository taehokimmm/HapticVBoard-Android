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

        var effect = VibrationEffect.DEFAULT_AMPLITUDE
        if (key == "Backspace") {
            effect = VibrationEffect.EFFECT_CLICK
        } else if(key == "Space"){
            effect = VibrationEffect.EFFECT_TICK
        } else {
            return
        }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(effect)
                )
            } else {
                // Deprecated in API 26
                vibrator.vibrate(100)
            }
        }
    }

}