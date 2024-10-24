package com.taehokimmm.hapticvboard_android.layout.intro

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.getAllowGroup
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

@Composable
fun GroupIntro(
    innerPadding: PaddingValues,
    soundManager: SoundManager?,
    hapticManager: HapticManager?,
    category: String,
    group: String,
) {
    var phonemeGroups = listOf(listOf(""))
    var names = listOf("")
    if (category == "phoneme") {
        phonemeGroups = listOf(
            listOf("f", "s", "h", "v", "z"),
            listOf("b", "d", "g", "p", "t", "c", "k", "q"),
            listOf("j", "x"),
            listOf("m", "n", "l", "r"),
            listOf("a", "e", "i", "u"),
            listOf("y", "o", "w")
        )
        names = listOf(
            "마찰음",
            "파열음",
            "파열음+마찰음",
            "비음,접근음",
            "모음",
            "이중모음"
        )
    } else if(category == "location") {
        phonemeGroups = listOf(
            listOf("f", "v", "b", "p", "m", "l"),
            listOf("t", "d", "j"),
            listOf("s", "h", "z",  "g", "c", "k", "q", "n", "r", "x"),
            listOf("a", "e", "i"),
            listOf("u"),
            listOf("o", "w", "y")
        )
        names = listOf("자음-왼쪽", "자음-양쪽", "자음-오른쪽", "모음-왼쪽", "모음-오른쪽", "모음-양쪽")

//        phonemeGroups = listOf(
//            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
//            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
//            listOf("z", "x", "c", "v", "b", "n", "m")
//        )
//        names = listOf("1행", "2행", "3행")
    }
    val allowGroup = getAllowGroup(group)

    val filteredPhonemeGroups = phonemeGroups.map { phonemes ->
        phonemes.intersect(allowGroup).toList()
    }

    val (filteredNames, nonEmptyGroups) = names.zip(filteredPhonemeGroups)
        .filter { it.second.isNotEmpty() }
        .unzip()

    TrainGroup(innerPadding, soundManager, hapticManager, nonEmptyGroups, filteredNames)
}