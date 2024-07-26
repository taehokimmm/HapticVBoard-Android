package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Train::class],
    version = 1
)
abstract class TrainDatabase : RoomDatabase(){

    abstract fun getTrainDao() : TrainDao

    companion object{
        @Volatile private var instance: TrainDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context, name: String) = instance ?: synchronized(LOCK){
            instance ?:buildDatabase(context, name).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context, name: String) = Room.databaseBuilder(
            context.applicationContext,
            TrainDatabase::class.java,
            "traindatabase" + name
        ).build()
    }
}
