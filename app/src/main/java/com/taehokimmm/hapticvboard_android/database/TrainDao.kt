package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrainDao {
    @Insert
    fun add(data: Train)

    @Query("SELECT * FROM train")
    fun getAll() : List<Train>

    @Insert
    fun addMultiple(vararg data: Train)
}