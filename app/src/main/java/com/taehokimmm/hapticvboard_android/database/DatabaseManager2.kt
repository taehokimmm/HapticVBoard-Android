package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import com.taehokimmm.hapticvboard_android.HapticMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// INSERT DATA FOR STUDY2 Test - Sighted
fun <T : Any> addData2(context: Context, name:String, data:T, addFunction: (dao: Study2Dao, data: T) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val dao = Study2Database.getInstance(context, name).getDao()
        addFunction(dao, data)
        withContext(Dispatchers.Main) {
            // You can update UI here if needed
        }
    }
}

fun addStudy2Metric(context: Context, name: String, data: Study2Metric){
    addData2(context, name, data) { dao, answer -> dao.add2Metric(answer) }
}

fun addStudy2Log(context: Context, name: String, data: Study2TestLog){
    addData2(context, name, data
    ) { dao, answer -> dao.addLog(answer) }
}

fun closeStudy2Database() {
    Study2Database.closeDatabase()
}

// INSERT DATA FOR STUDY2 Test - BVI
fun <T : Any> addDataBVI(context: Context, name:String, data:T, addFunction: (dao: Study2BVIDao, data: T) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val dao = Study2BVIDatabase.getInstance(context, name).getDao()
        addFunction(dao, data)
        withContext(Dispatchers.Main) {
            // You can update UI here if needed
        }
    }
}

fun addStudy2BVIMetric(context: Context, name: String, data: Study2BVITestAnswer){
    addDataBVI(context, name, data) { dao, answer -> dao.addMetric(answer) }
}

fun addStudy2BVILog(context: Context, name: String, data: Study2BVITestLog){
    addDataBVI(context, name, data
    ) { dao, answer -> dao.addLog(answer) }
}

fun closeStudy2BVIDatabase() {
    Study2BVIDatabase.closeDatabase()
}

// INSERT DATA FOR STUDY2 Train
fun <T : Any> addData2Train(context: Context, name:String, data:T, addFunction: (dao: Study2TrainDao, data: T) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val dao = Study2TrainDatabase.getInstance(context, name).getDao()
        addFunction(dao, data)
        withContext(Dispatchers.Main) {
            // You can update UI here if needed
        }
    }
}

fun addStudy2TrainAnswer(context: Context, name: String, data: Study2TrainAnswer){
    addData2Train(context, name, data) { dao, answer -> dao.add2TrainMetric(answer) }
}

fun addStudy2TrainLog(context: Context, name: String, data: Study2TrainLog){
    addData2Train(context, name, data
    ) { dao, answer -> dao.add2TrainLog(answer) }
}

fun closeStudy2TrainDatabase() {
    Study2TrainDatabase.closeDatabase()
}