package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// INSERT DATA FOR STUDY1
fun <T> addData(context: Context, name:String, data:T, addFunction: (dao: StudyDao, data: T) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val dao = StudyDatabase.getInstance(context, name).getDao()
        addFunction(dao, data)
        withContext(Dispatchers.Main) {
            // You can update UI here if needed
        }
    }
}

fun closeStudyDatabase() {
    StudyDatabase.closeDatabase()
}

fun addVibrationTestAnswer(context: Context, subject: String, group: String, data: VibrationTestAnswer){
    addData(context, subject, data
    ) { dao, answer -> dao.addVibrationTestAnswer(answer) }
}

fun addTypingTestAnswer(context: Context, subject: String, data: TypingTestAnswer){
    addData(context, subject, data
    ) { dao, answer -> dao.addTypingTestAnswer(answer) }
}

fun addTypingTestLog(context: Context, name: String, data: TypingTestLog){
    addData(context, name, data
    ) { dao, answer -> dao.addTypingTestLog(answer) }
}

fun addTextEntryMetric(context: Context, subject: String, data: TextEntryMetric){
    addData(context, subject, data
    ) { dao, answer -> dao.addTextEntryMetric(answer) }
}

fun addTextEntryLog(context: Context, name: String, data: TextEntryLog){
    addData(context, name, data
    ) { dao, answer -> dao.addTextEntryLog(answer) }
}

fun <T : Any> addLog(context: Context, name: String, data:T, state: String, touchedKey: String, x: Float, y:Float) {
    when (data) {
        is TypingTestLog -> {
            // Handle TypingTestLog specific logic here
            data.x = x
            data.y = y
            data.state = state
            data.touchedKey = touchedKey
            data.timestamp = System.currentTimeMillis()
            data.date = System.currentTimeMillis().toFormattedDateString()
            addTypingTestLog(context, name, data)
        }
        is TextEntryLog -> {
            data.x = x
            data.y = y
            data.state = state
            data.touchedKey = touchedKey
            data.timestamp = System.currentTimeMillis()
            data.date = System.currentTimeMillis().toFormattedDateString()
            addTextEntryLog(context, name, data)
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
        StudyDatabase::class.java, databaseName
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
        StudyDatabase::class.java, databaseName
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