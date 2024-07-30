package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.room.Room
import com.taehokimmm.hapticvboard_android.database.study1.Study1Answer
import com.taehokimmm.hapticvboard_android.database.study1.Study1Database
import com.taehokimmm.hapticvboard_android.database.study1.Study1Logging
import com.taehokimmm.hapticvboard_android.database.study1.Study1TrainPhase2Answer
import com.taehokimmm.hapticvboard_android.database.study1.Study1TrainPhase3Answer
import java.io.File

// INSERT DATA
fun addStudy1Answer(context: Context, subject: String, group: String, data: Study1Answer){
    class SaveData : AsyncTask<Void, Void, Void>(){
        override fun doInBackground(vararg p0: Void?): Void? {
            Study1Database(context, subject + "_" + group).getDao().add(data)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }
    SaveData().execute()
}

fun addStudy1TrainPhase3Answer(context: Context, subject: String, group: String, data: Study1TrainPhase3Answer){
    class SaveData : AsyncTask<Void, Void, Void>(){
        override fun doInBackground(vararg p0: Void?): Void? {
            Study1Database(context, subject + "_" + group).getDao().addTrainPhase3(data)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }
    SaveData().execute()
}

fun addStudy1TrainPhase2Answer(context: Context, subject: String, group: String, data: Study1TrainPhase2Answer){
    class SaveData : AsyncTask<Void, Void, Void>(){
        override fun doInBackground(vararg p0: Void?): Void? {
            Study1Database(context, subject + "_" + group).getDao().addTrainPhase2(data)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }
    SaveData().execute()
}

// DELETE DATABASE TO RESET
fun deleteDatabaseByName(context: Context, databaseName: String) {
    var myDatabase = Room.databaseBuilder(
        context,
        Study1Database::class.java, databaseName
    ).build()

    // Example usage: Delete the database when a certain condition is met
    // This could be triggered by a button click or some other event
    deleteDatabase(context, databaseName)
}

fun resetData(context: Context, subject: String, group: String) {
    // Initialize the Room database
    val databaseName = subject+"_"+group
    var myDatabase = Room.databaseBuilder(
        context,
        Study1Database::class.java, databaseName
    ).build()

    // Example usage: Delete the database when a certain condition is met
    // This could be triggered by a button click or some other event
    deleteDatabase(context, databaseName)
}

private fun deleteDatabase(context: Context, databaseName: String) {
    val databasesDir = context.getDatabasePath(databaseName).parentFile
    val databaseFile = context.getDatabasePath(databaseName)

    if (databaseFile.exists()) {
        // Delete the main database file
        databaseFile.delete()

        // Delete any associated database files
        val suffixes = arrayOf("-shm", "-wal")
        for (suffix in suffixes) {
            val associatedFile = File(databaseFile.absolutePath + suffix)
            if (associatedFile.exists()) {
                associatedFile.delete()
            }
        }
    }
}