package com.taehokimmm.hapticvboard_android.database.study1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Study1Dao {
    @Insert
    fun add(data: Study1)

    @Query("SELECT * FROM study1")
    fun getAll() : List<Study1>

    @Insert
    fun addMultiple(vararg data: Study1)
}