package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Study2Metric::class, Study2TestLog::class],
    version = 1
)
abstract class Study2Database : RoomDatabase() {
    abstract fun getDao() : Study2Dao

    companion object{
        @Volatile private var INSTANCE: Study2Database? = null

        fun getInstance(context: Context, name: String): Study2Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Study2Database::class.java,
                    name
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.apply {
                if (isOpen) {
                    close()
                }
                INSTANCE = null
            }
        }
    }
}
