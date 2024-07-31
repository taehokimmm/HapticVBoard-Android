package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface Study1Dao {
    @Insert
    fun addTest(data: Study1TestAnswer)

    @Insert
    fun addTrainPhase3(data: Study1Phase3Answer)

    @Insert
    fun addTrainPhase2(data: Study1Phase2Answer)
}