package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// INSERT DATA FOR STUDY1
fun <T> addData(context: Context, name:String, data:T, addFunction: (dao: Study1Dao, data: T) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val dao = Study1Database.getInstance(context, name).getDao()
        addFunction(dao, data)
        withContext(Dispatchers.Main) {
            // You can update UI here if needed
        }
    }
}

fun addStudy1Answer(context: Context, subject: String, group: String, data: Study1TestAnswer){
    addData(context, subject + "_" + group.last(), data
    ) { dao, answer -> dao.addTest(answer) }
}

fun closeStudy1Database() {
    Study1Database.closeDatabase()
}

fun addStudy1TrainPhase3Answer(context: Context, subject: String, group: String, data: Study1Phase3Answer){
    addData(context, subject + "_" + group, data
    ) { dao, answer -> dao.addTrainPhase3(answer) }
}

fun addStudy1TrainPhase2Answer(context: Context, subject: String, group: String, data: Study1Phase2Answer){
    addData(context, subject+"_"+group, data
    ) { dao, answer -> dao.addTrainPhase2(answer) }
}

fun addStudy1TestLog(context: Context, name: String, data: Study1TestLog){
    addData(context, name, data
    ) { dao, answer -> dao.addTestLog(answer) }
}

fun addStudy1Phase3Log(context: Context, name: String, data: Study1Phase3Log){
    addData(context, name, data
    ) { dao, answer -> dao.addPhase3Log(answer) }
}

fun <T : Any> addLog(context: Context, name: String, data:T, state: String, touchedKey: String, x: Float, y:Float) {
    when (data) {
        is Study1TestLog -> {
            // Handle Study1TestLog specific logic here
            data.x = x
            data.y = y
            data.state = state
            data.touchedKey = touchedKey
            data.timestamp = System.currentTimeMillis()
            data.date = System.currentTimeMillis().toFormattedDateString()
            addStudy1TestLog(context, name, data)
        }
        is Study1Phase3Log -> {
            // Handle Study1TestLog specific logic here
            data.x = x
            data.y = y
            data.state = state
            data.touchedKey = touchedKey
            data.timestamp = System.currentTimeMillis()
            data.date = System.currentTimeMillis().toFormattedDateString()
            addStudy1Phase3Log(context, name, data)
        }
        is Study2TestLog -> {
            data.x = x
            data.y = y
            data.state = state
            data.touchedKey = touchedKey
            data.timestamp = System.currentTimeMillis()
            data.date = System.currentTimeMillis().toFormattedDateString()
            addStudy2Log(context, name, data)
        }
        else -> {
            // Handle unknown type
            println("Unknown log type")
        }
    }
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