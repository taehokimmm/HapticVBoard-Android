package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import com.taehokimmm.hapticvboard_android.HapticMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// INSERT DATA FOR STUDY2
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