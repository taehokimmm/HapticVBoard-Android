package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface Study1Dao {
    @Insert
    fun addVibrationTestAnswer(data: VibrationTestAnswer)

    @Insert
    fun addTypingTestAnswer(data: TypingTestAnswer)

    @Insert
    fun addTypingTestLog(data: TypingTestLog)
}