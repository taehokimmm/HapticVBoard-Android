//package com.taehokimmm.hapticvboard_android.database
//
//import android.content.Context
//import androidx.room.Dao
//import androidx.room.Database
//import androidx.room.Entity
//import androidx.room.Insert
//import androidx.room.PrimaryKey
//import androidx.room.Query
//import androidx.room.Room
//import androidx.room.RoomDatabase
//
//
//@Entity(tableName = "touch_metrics")
//data class TouchMetric(
//    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
//    val testName: String,
//    val iteration: Int,
//    val touchStartX: Int,
//    val touchStartY: Int,
//    val touchEndX: Int,
//    val touchEndY: Int,
//
//    val touchStart: Char,
//    val touchEnd: Char,
//
//    val timestamp: Long,
//    val touchDuration: Long
//)
//
//@Entity(tableName = "test_metrics")
//data class TestMetric(
//    @PrimaryKey(autoGenerate = true) val uid : Int = 0,
//    val testName: String,
//    val iteration: Int,
//    val wpm: Double,
//    val accuracy: Double,
//    val timestamp: Long,
//    val touchMetrics: List<TouchMetric>
//)
//
//@Dao
//interface TestMetricDao {
//    @Insert
//    suspend fun insert(metric: TestMetric)
//
//    @Query("SELECT * FROM test_metrics WHERE testName = :testName")
//    suspend fun getMetricsForTest(testName: String): List<TestMetric>
//}
//
//@Database(entities = [TestMetric::class], version = 1, exportSchema = false)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun testMetricDao(): TestMetricDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getDatabase(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "app_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}

fun calculateTouchDuration(startTime: Long, endTime: Long): Long {
    return endTime - startTime
}


fun calculateWPM(startTime: Long, endTime: Long, wordCount: Int): Double {
    val durationInMinutes = (endTime - startTime) / 1000.0 / 60.0
    return wordCount / durationInMinutes
}

fun calculateAccuracy(typedText: String, referenceText: String): Double {
    val typedWords = typedText.split("\\s+".toRegex())
    val referenceWords = referenceText.split("\\s+".toRegex())
    val correctWords = typedWords.zip(referenceWords).count { it.first == it.second }
    return (correctWords.toDouble() / referenceWords.size) * 100
}