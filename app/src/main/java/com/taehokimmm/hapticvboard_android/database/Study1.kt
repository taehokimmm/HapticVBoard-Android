package com.taehokimmm.hapticvboard_android.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Entity("study1TestAnswer")
data class Study1TestAnswer(
    val answer: String,
    val perceived: String,
    val iter: Int,
    val block: Int,
    val duration: Long,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}

@Entity("study1Phase3Answer")
data class Study1Phase3Answer(
    val answer: String,
    val perceived: String,
    val iter: Int,
    val block: Int,
    val duration: Long,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity("study1Phase2Answer")
data class Study1Phase2Answer(
    val answer: String,
    val perceived: String,
    val iter: Int,
    val block: Int,
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

@Entity("study1TestLog")
data class Study1TestLog(
    val answer: String,
    val iter: Int,
    val block: Int,
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

@Entity("study1Phase3Log")
data class Study1Phase3Log(
    val answer: String,
    val iter: Int,
    val block: Int,
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