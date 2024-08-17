package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Study2TrainLog::class,
               Study2TrainAnswer::class],
    version = 1
)
abstract class Study2TrainDatabase : RoomDatabase() {
    abstract fun getDao() : Study2TrainDao

    companion object{
        @Volatile private var INSTANCE: Study2TrainDatabase? = null

        fun getInstance(context: Context, name: String): Study2TrainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Study2TrainDatabase::class.java,
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
