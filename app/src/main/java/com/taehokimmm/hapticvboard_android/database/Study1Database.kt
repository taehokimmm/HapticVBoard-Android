package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VibrationTestAnswer::class,
        TypingTestAnswer::class,
        TypingTestLog::class],
    version = 2
)
abstract class Study1Database : RoomDatabase() {
    abstract fun getDao() : Study1Dao

    companion object{
        @Volatile private var INSTANCE: Study1Database? = null

        fun getInstance(context: Context, name: String): Study1Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Study1Database::class.java,
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
