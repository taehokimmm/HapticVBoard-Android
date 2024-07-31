package com.taehokimmm.hapticvboard_android.database

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface Study2Dao {
    @Insert
    fun add2Metric(data: Study2Metric)
}