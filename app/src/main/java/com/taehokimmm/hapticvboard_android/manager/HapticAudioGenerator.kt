package com.taehokimmm.hapticvboard_android.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.MediaPlayer
import android.media.audiofx.HapticGenerator
import android.os.Build
import androidx.annotation.RequiresApi
import com.taehokimmm.hapticvboard_android.R


//
//class HapticAudioGenerator(context: Context) {
//    private val context: Context = context
//    private var audioTrack: AudioTrack? = null
//    private lateinit var vibrator: Vibrator
//
//    fun generateHapticFeedback(resourceId: Int): VibrationEffect? {
//        Log.d("HapticAudio", resourceId.toString())
//        val (samples, sampleRate) = readWavFile(resourceId)
//        Log.d("HapticAudio", sampleRate.toString())
//        val (timings, amplitudes) = generateVibrationPattern(samples, sampleRate)
//        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
//        return effect
//    }
//
//    fun readWavFile(resourceId: Int?): Pair<List<Int>, Int> {
//        val `is` = context.resources.openRawResource(resourceId!!)
//        val header = ByteArray(44)
//        `is`.read(header, 0, 44)
//
//        val sampleRate = ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt()
//        val bitsPerSample =
//            ByteBuffer.wrap(header, 34, 2).order(ByteOrder.LITTLE_ENDIAN).getShort().toInt()
//        val bytesPerSample = bitsPerSample / 8
//        val samples: MutableList<Int> = ArrayList()
//
//        val buffer = ByteArray(bytesPerSample)
//        while (`is`.read(buffer) != -1) {
//            val sample = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort().toInt()
//            samples.add(sample)
//        }
//
//        `is`.close()
//        return Pair(samples, sampleRate)
//    }
//
//    private fun generateVibrationPattern(samples: List<Int>, sampleRate: Int): Pair<LongArray, IntArray> {
//        val segmentDuration = 10
//        val segmentSize = (segmentDuration * sampleRate) / 1000 // Convert segment duration to sample size
//        val timings = mutableListOf<Long>()
//        val amplitudes = mutableListOf<Int>()
//        Log.d("HapticAudio", "Sample Size" + samples.size.toString())
//        for (i in samples.indices step segmentSize) {
//            val segmentEnd = minOf(i + segmentSize, samples.size)
//            var sum = 0L
//            for (j in i until segmentEnd) {
//                sum += abs(samples[j])
//            }
//            val averageAmplitude = sum / (segmentEnd - i)
//
//            timings.add(segmentDuration.toLong())
//            amplitudes.add((averageAmplitude * 255 / 32767).toInt()) // Normalize amplitude to 0-255 range
//        }
//
//        return Pair(timings.toLongArray(), amplitudes.toIntArray())
//    }
//
//}



@RequiresApi(Build.VERSION_CODES.S)
class HapticAudioGenerator(context: Context) {
    private var context: Context = context
    private var hapticGenerator: HapticGenerator? = null

//    var keyToResource: Map<String, Int> = mapOf(
//        "a" to R.raw.a,
//        "b" to R.raw.b,
//        "c" to R.raw.k,
//        "d" to R.raw.d,
//        "e" to R.raw.e,
//        "f" to R.raw.f,
//        "g" to R.raw.g,
//        "h" to R.raw.hh,
//        "i" to R.raw.i,
//        "j" to R.raw.j,
//        "k" to R.raw.k,
//        "l" to R.raw.l,
//        "m" to R.raw.m,
//        "n" to R.raw.n,
//        "o" to R.raw.o,
//        "p" to R.raw.p,
//        "q" to R.raw.k,
//        "r" to R.raw.r,
//        "s" to R.raw.s,
//        "t" to R.raw.t,
//        "u" to R.raw.u,
//        "v" to R.raw.v,
//        "w" to R.raw.w,
//        "x" to R.raw.ks,
//        "y" to R.raw.y,
//        "z" to R.raw.z
//    )

    var keyToResource: Map<String, Int> = mapOf(
        "a" to R.raw.phoneme_a,
        "b" to R.raw.phoneme_b,
        "c" to R.raw.phoneme_k,
        "d" to R.raw.phoneme_d,
        "e" to R.raw.phoneme_e,
        "f" to R.raw.phoneme_f,
        "g" to R.raw.phoneme_g,
        "h" to R.raw.phoneme_h,
        "i" to R.raw.phoneme_i,
        "j" to R.raw.phoneme_j,
        "k" to R.raw.phoneme_k,
        "l" to R.raw.phoneme_l,
        "m" to R.raw.phoneme_m,
        "n" to R.raw.phoneme_n,
        "o" to R.raw.phoneme_o,
        "p" to R.raw.phoneme_p,
        "q" to R.raw.phoneme_q,
        "r" to R.raw.phoneme_r,
        "s" to R.raw.phoneme_s,
        "t" to R.raw.phoneme_t,
        "u" to R.raw.phoneme_u,
        "v" to R.raw.phoneme_v,
        "w" to R.raw.phoneme_w,
        "x" to R.raw.phoneme_x,
        "y" to R.raw.phoneme_y,
        "z" to R.raw.phoneme_z
    )

    private var player: MediaPlayer? = null
    private fun releasePlayer() {
        player?.release()
        hapticGenerator?.release()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun play(key: String) {
        releasePlayer()
        val resourceId = keyToResource[key]

        player = resourceId?.let { MediaPlayer.create(context, it) }

        // Create AudioAttributes with the desired channel mask
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioFormat.CHANNEL_OUT_MONO)
            .setHapticChannelsMuted(false)
            .build()
        player!!.setAudioAttributes(audioAttributes)
        player!!.setVolume(0f, 0f)
        if (HapticGenerator.isAvailable()) {
            hapticGenerator = HapticGenerator.create(player!!.audioSessionId)
            hapticGenerator!!.setEnabled(true)
        }
        player!!.start()
    }
}