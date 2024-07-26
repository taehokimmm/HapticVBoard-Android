package com.taehokimmm.hapticvboard_android.database.study2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TestMetricDao {
    @Insert
    suspend fun insert(metric: TestMetric)

    @Query("SELECT * FROM test_metrics WHERE testName = :testName")
    suspend fun getMetricsForTest(testName: String): List<TestMetric>
}