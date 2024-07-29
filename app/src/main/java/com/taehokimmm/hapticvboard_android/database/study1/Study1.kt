package com.taehokimmm.hapticvboard_android.database.study1

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("study1Answer")
data class Study1Answer(
    val answer: String,
    val perceived: String,
    val iter: Int,
    val block: Int
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