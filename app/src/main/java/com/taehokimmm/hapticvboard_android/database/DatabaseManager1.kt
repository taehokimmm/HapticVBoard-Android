package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import java.io.File

// INSERT DATA FOR STUDY1
fun <T> addData(context: Context, name:String, data:T, addFunction: (dao: Study1Dao, data: T) -> Unit) {
    class SaveData : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            val dao = Study1Database(context, name).getDao()
            addFunction(dao, data)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }
    SaveData().execute()
}

fun addStudy1Answer(context: Context, subject: String, group: String, data: Study1TestAnswer){
    addData(context, subject + "_" + group, data
    ) { dao, answer -> dao.addTest(answer) }
}

fun addStudy1TrainPhase3Answer(context: Context, subject: String, group: String, data: Study1Phase3Answer){
    addData(context, subject + "_" + group, data
    ) { dao, answer -> dao.addTrainPhase3(answer) }
}

fun addStudy1TrainPhase2Answer(context: Context, subject: String, group: String, data: Study1Phase2Answer){
    addData(context, subject+"_"+group, data
    ) { dao, answer -> dao.addTrainPhase2(answer) }
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