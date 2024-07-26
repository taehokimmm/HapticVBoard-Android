package com.taehokimmm.hapticvboard_android.database

import android.content.Context
import android.os.AsyncTask
import com.taehokimmm.hapticvboard_android.database.study1.Study1
import com.taehokimmm.hapticvboard_android.database.study1.Study1Database

fun saveStudy1Data(context: Context, name: String, data: Study1){
    class SaveData : AsyncTask<Void, Void, Void>(){
        override fun doInBackground(vararg p0: Void?): Void? {
            Study1Database(context, name).getDao().add(data)
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
        }
    }
    SaveData().execute()
}