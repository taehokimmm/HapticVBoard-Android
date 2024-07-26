package com.taehokimmm.hapticvboard_android

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundMap: MutableMap<String, Int> = mutableMapOf()

    init {
        // Define audio attributes for the sound pool
        val audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        // Initialize the sound pool with the defined attributes
        soundPool =
            SoundPool.Builder().setMaxStreams(10).setAudioAttributes(audioAttributes).build()

        // Map keys to their respective sound resource IDs
        val keyToResourceMap = mapOf(
            'a' to R.raw.aa,
            'b' to R.raw.b,
            'c' to R.raw.k,
            'd' to R.raw.d,
            'e' to R.raw.eh,
            'f' to R.raw.f,
            'g' to R.raw.g,
            'h' to R.raw.hh,
            'i' to R.raw.iy,
            'j' to R.raw.g,
            'k' to R.raw.k,
            'l' to R.raw.l,
            'm' to R.raw.m,
            'n' to R.raw.n,
            'o' to R.raw.ow,
            'p' to R.raw.p,
            'q' to R.raw.k,
            'r' to R.raw.r,
            's' to R.raw.s,
            't' to R.raw.t,
            'u' to R.raw.uw,
            'v' to R.raw.v,
            'w' to R.raw.uw,
            'x' to R.raw.ks,
            'y' to R.raw.ey,
            'z' to R.raw.z,
        )

        // Load the sounds into the sound pool and map them by key
        for ((key, resourceId) in keyToResourceMap) {
            val soundId = soundPool.load(context, resourceId, 1)
            soundMap[key.toString()] = soundId
        }
    }

    /**
     * Plays the sound associated with the given key.
     *
     * @param key The key for which to play the sound.
     */
    @Synchronized
    fun playSoundForKey(key: String) {
        val soundId = soundMap[key]
        if (soundId == null) {
            Log.d("SoundManager", "No sound found for key: $key, skipping...")
            return
        }
        Log.d("SoundManager", "Playing sound for key: $key")
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }
}
