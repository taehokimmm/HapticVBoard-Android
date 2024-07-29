package com.taehokimmm.hapticvboard_android.database.study1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Study1Dao {
    @Insert
    fun add(data: Study1Answer)

    @Query("SELECT * FROM study1Answer")
    fun getAll() : List<Study1Answer>

    @Insert
    fun addMultiple(vararg data: Study1Answer)
}