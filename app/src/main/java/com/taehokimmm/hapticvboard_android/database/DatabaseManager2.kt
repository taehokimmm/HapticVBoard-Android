package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import com.taehokimmm.hapticvboard_android.HapticMode

// INSERT DATA FOR STUDY2
fun <T : Any> addData2(context: Context, name:String, data:T, addFunction: (dao: Study2Dao, data: T) -> Unit) {
    class SaveData : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void? {
            val dao = Study2Database(context, name).getDao()
            addFunction(dao, data)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }
    SaveData().execute()
}
fun addStudy2Metric(context: Context, subject: String, hapticMode: HapticMode, data: Study2Metric){
    var name = subject + "_"
    when(hapticMode) {
        HapticMode.TICK -> name += "vibration"
        HapticMode.PHONEME -> name += "phoneme"
        HapticMode.VOICE -> name += "audio"
        else -> name += ""
    }
    addData2(context, name, data) { dao, answer -> dao.add2Metric(answer) }
    closeStudy2Database()
}

fun closeStudy2Database() {
    Study2Database.closeDatabase()
}