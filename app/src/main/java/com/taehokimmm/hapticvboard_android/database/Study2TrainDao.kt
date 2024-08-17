package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface Study2TrainDao {
    @Insert
    fun add2TrainMetric(data: Study2TrainAnswer)

    @Insert
    fun add2TrainLog(data: Study2TrainLog)
}