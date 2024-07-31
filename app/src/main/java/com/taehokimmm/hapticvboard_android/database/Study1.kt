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
    val duration: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}

@Entity("study1Log")
data class Study1Logging(
    val answer: String,
    val touched: String,
    val iter: Int,
    val block: Int,
    val timestamp: Long,
    val state: String, // Touch, Move, Release
    val x : Int,
    val y : Int
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
