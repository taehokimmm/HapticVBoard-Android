package com.taehokimmm.hapticvboard_android.database.study2


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

@Entity(tableName = "test_metrics")
data class TestMetric(
    @PrimaryKey(autoGenerate = true) val uid : Int = 0,
    val testName: String,
    val iteration: Int,
    val wpm: Double,
    val accuracy: Double,
    val timestamp: Long,
    val touchMetrics: List<TouchMetric>
)