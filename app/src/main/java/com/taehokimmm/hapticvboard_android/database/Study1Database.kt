package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Study1TestAnswer::class, Study1Phase2Answer::class, Study1Phase3Answer::class],
    version = 1
)
abstract class Study1Database : RoomDatabase() {
    abstract fun getDao() : Study1Dao

    companion object{
        @Volatile private var instance: Study1Database? = null
        private val LOCK = Any()

        operator fun invoke(context: Context, name:String) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context, name).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context, name:String) = Room.databaseBuilder(
            context.applicationContext,
            Study1Database::class.java,
            name
        ).build()
    }
}