package com.taehokimmm.hapticvboard_android.database


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "study2TrainMetric")
data class Study2TrainAnswer(
    val block: Int,
    val iteration: Int,
    val mode: Int,
    val answer: String,
    val perceived: String,
    val duration: Long,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


@Entity("study2TrainLog")
data class Study2TrainLog(
    val block: Int,
    val iteration: Int,
    val mode: Int,
    val answer: String,
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


@Entity(tableName = "study2Metric")
data class Study2Metric(
    val block: Int,
    val iteration: Int,
    val wpm: Double,
    val pressDuration: Double,
    val uer: Double,
    val keyEff: Double,
    val target: String,
    val input: String,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity("study2Log")
data class Study2TestLog(
    val block: Int,
    val iteration: Int,
    val targetText: String,
    val inputText: String,
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