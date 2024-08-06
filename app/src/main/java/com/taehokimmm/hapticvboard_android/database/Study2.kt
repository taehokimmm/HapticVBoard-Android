package com.taehokimmm.hapticvboard_android.database


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("touch_metrics")
data class TouchMetric(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val testName: String,
    val iteration: Int,
    val touchStartX: Int,
    val touchStartY: Int,
    val touchEndX: Int,
    val touchEndY: Int,

    val touchStart: Char,
    val touchEnd: Char,

    val timestamp: Long,
    val touchDuration: Long
)

@Entity(tableName = "study2Metric")
data class Study2Metric(
    val block: Int,
    val iteration: Int,
    val wpm: Double,
    val iki: Double,
    val uer: Double,
    val keyEff: Double,
    val target: String,
    val input: String,
    var timestamp: String = System.currentTimeMillis().toFormattedDateString()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
