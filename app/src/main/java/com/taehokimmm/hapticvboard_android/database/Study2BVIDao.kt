package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface Study2BVIDao {
    @Insert
    fun addMetric(data: Study2BVITestAnswer)

    @Insert
    fun addLog(data: Study2BVITestLog)
}