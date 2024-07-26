package com.taehokimmm.hapticvboard_android.database.study1

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("study1")
data class Study1(
    val answer: String,
    val perceived: String
) {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}