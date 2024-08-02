package com.taehokimmm.hapticvboard_android.layout.study1.intro

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
            listOf("s", "z", "f", "v", "h"),
            listOf("n", "m"),
            listOf("p", "t", "k", "c", "q"),
            listOf("b", "d", "g", "j"),
            listOf("l", "r"),
            listOf("x"),
            listOf("a", "e", "i", "o", "u", "w", "y")
        )
        names = listOf(
                "마찰음", "비음", "파열음-strong", "파열음-weak", "설측음", "others", "모음"
        )
    } else if(category == "location") {
        phonemeGroups = listOf(
            listOf("p", "b", "f", "v", "m", "e", "i"),
            listOf("t", "d", "s", "z", "l", "x", "w", "y"),
            listOf("k", "c", "q", "g", "j", "n", "r", "h", "a", "o", "u")
        )
        names = listOf("Up", "Both", "Down")
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