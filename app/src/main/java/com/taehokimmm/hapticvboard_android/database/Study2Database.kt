package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Study2Metric::class],
    version = 1
)
abstract class Study2Database : RoomDatabase() {
    abstract fun getDao() : Study2Dao

    companion object{
        @Volatile private var instance: Study2Database? = null
        private val LOCK = Any()

        operator fun invoke(context: Context, name:String) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context, name).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context, name:String) = Room.databaseBuilder(
            context.applicationContext,
            Study2Database::class.java,
            name
        ).build()

        fun closeDatabase() {
            instance?.apply {
                if (isOpen) {
                    close()
                }
                instance = null
            }
        }
    }
}
