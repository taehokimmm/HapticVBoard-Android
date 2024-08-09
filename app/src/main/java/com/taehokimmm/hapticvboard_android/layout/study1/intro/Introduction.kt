package com.taehokimmm.hapticvboard_android.layout.study1.intro

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.taehokimmm.hapticvboard_android.layout.study1.train.getAllowGroup
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
            listOf("s", "z", "f", "v", "h", "j"),
            listOf("n", "m"),
            listOf("p", "t", "k", "c", "q"),
            listOf("b", "d", "g"),
            listOf("l", "r"),
            listOf("x"),
            listOf("a", "e", "i", "o", "u", "w", "y")
        )
        names = listOf(
                "마찰음", "비음", "강한 파열음", "약한 파열음", "설측음", "합성음", "모음"
        )
    } else if(category == "location") {
//        phonemeGroups = listOf(
//            listOf("p", "b", "f", "v", "m", "e", "i"),
//            listOf("k", "c", "q", "g", "n", "r", "h", "a", "o", "u"),
//            listOf("t", "d", "s", "z", "l", "x", "j"),
//            listOf("w"),
//            listOf("y")
//        )
//        names = listOf("위", "아래", "위아래 동시에", "위에서 아래", "아래에서 위")

        phonemeGroups = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        )
        names = listOf("위", "중간", "아래")
    }
    val allowGroup = getAllowGroup(group)

    val filteredPhonemeGroups = phonemeGroups.map { phonemes ->
        phonemes.intersect(allowGroup).toList()
    }

    val (filteredNames, nonEmptyGroups) = names.zip(filteredPhonemeGroups)
        .filter { it.second.isNotEmpty() }
        .unzip()

    if (category == "phoneme")
        TrainGroup(innerPadding, soundManager, hapticManager, nonEmptyGroups, filteredNames)
    else if (category == "location")
        TrainGroupLocation(innerPadding, soundManager, hapticManager, nonEmptyGroups, filteredNames)
}