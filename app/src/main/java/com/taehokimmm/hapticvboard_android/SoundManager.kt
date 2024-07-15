package com.taehokimmm.hapticvboard_android

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap: MutableMap<String, Int> = mutableMapOf()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        val keyToResourceMap = mapOf<Char, Int>(
            'a' to R.raw.a,
            'b' to R.raw.b,
            'c' to R.raw.c,
            'd' to R.raw.d,
            'e' to R.raw.e,
            'f' to R.raw.f,
            'g' to R.raw.g,
            'h' to R.raw.h,
            'i' to R.raw.i,
            'j' to R.raw.j,
            'k' to R.raw.k,
            'l' to R.raw.l,
            'm' to R.raw.m,
            'n' to R.raw.n,
            'o' to R.raw.o,
            'p' to R.raw.p,
            'q' to R.raw.k,
            'r' to R.raw.r,
            's' to R.raw.s,
            't' to R.raw.t,
            'u' to R.raw.u,
            'v' to R.raw.v,
            'w' to R.raw.w,
            'x' to R.raw.x,
            'y' to R.raw.y,
            'z' to R.raw.z
        )

        for ((key, resourceId) in keyToResourceMap) {
            soundMap[key.toString()] = soundPool.load(context, resourceId, 1)
        }
    }

    fun playSoundForKey(key: String) {
        val soundId = soundMap[key]
        if (soundId == null) {
            println("No sound found for key: $key, skipping...")
            return
        }
        println("Playing sound for key: $key")
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }
}