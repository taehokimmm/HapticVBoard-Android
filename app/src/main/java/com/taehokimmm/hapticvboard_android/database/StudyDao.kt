package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface StudyDao {
    @Insert
    fun addVibrationTestAnswer(data: VibrationTestAnswer)

    @Insert
    fun addTypingTestAnswer(data: TypingTestAnswer)

    @Insert
    fun addTypingTestLog(data: TypingTestLog)

    @Insert
    fun addTypingTest2Answer(data: TypingTest2Answer)

    @Insert
    fun addTypingTest2Log(data: TypingTest2Log)

    @Insert
    fun addTextEntryMetric(data: TextEntryMetric)

    @Insert
    fun addTextEntryLog(data: TextEntryLog)
}