package com.taehokimmm.hapticvboard_android.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("train")
data class Train(
    val answer: String,
    val perceived: String
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}