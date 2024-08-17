package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Study2BVITestAnswer::class, Study2BVITestLog::class],
    version = 1
)
abstract class Study2BVIDatabase : RoomDatabase() {
    abstract fun getDao() : Study2BVIDao

    companion object{
        @Volatile private var INSTANCE: Study2BVIDatabase? = null

        fun getInstance(context: Context, name: String): Study2BVIDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Study2BVIDatabase::class.java,
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
