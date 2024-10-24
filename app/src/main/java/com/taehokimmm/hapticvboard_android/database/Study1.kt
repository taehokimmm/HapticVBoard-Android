package com.taehokimmm.hapticvboard_android.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Entity("VibrationTestAnswer")
data class VibrationTestAnswer(
    val row: String,
    val answer: String,
    val perceived: String,
    val iter: Int,
    val block: Int,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}

@Entity("TypingTestAnswer")
data class TypingTestAnswer(
    val row: String,
    val answer: String,
    val perceived: String,
    val iter: Int,
    val block: Int,
    val duration: Long,
    val mode: Int,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

fun Long.toFormattedDateString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val date = Date(this)
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(date)
}

@Entity("TypingTestLog")
data class TypingTestLog(
    val row: String,
    val answer: String,
    val iter: Int,
    val block: Int,
    val mode: Int,
    var state: String = "", // DOWN, UP, MOVE
    var touchedKey: String = "",
    var x : Float = 0.0f,
    var y : Float = 0.0f,
    var timestamp: Long = System.currentTimeMillis(),
    var date: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}