package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VibrationTestAnswer::class,
        TypingTestAnswer::class,
        TypingTestLog::class,
        TextEntryLog::class,
        TextEntryMetric::class],
    version = 1
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun getDao() : StudyDao

    companion object{
        @Volatile private var INSTANCE: StudyDatabase? = null

        fun getInstance(context: Context, name: String): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
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
