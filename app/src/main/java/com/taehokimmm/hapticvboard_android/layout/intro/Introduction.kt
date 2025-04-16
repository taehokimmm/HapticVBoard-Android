package com.taehokimmm.hapticvboard_android.layout.intro

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.taehokimmm.hapticvboard_android.layout.intro.PhonemeGroupProvider.locationGroup
import com.taehokimmm.hapticvboard_android.layout.intro.PhonemeGroupProvider.phonemeGroups
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.getAllowGroup
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

enum class VocalLocation(val vocalLocation: String){
    FRONT_CONSONANT("Consonant Left"),
    BACK_CONSONANT("Consonant Right"),
    MIDDLE("Consonant Both"),
    FRONT_VOWEL("Vowel Left"),
    BACK_VOWEL("Vowel Right"),
    FRONT2BACK("Vowel Moving"),
    BACK2FRONT("Diphthongs"),
    NONE("")
}

enum class Location(val location: String){
    LEFT("Left"),
    RIGHT("Right"),
    BOTH("Both"),
    L2R("Left to Right"),
    R2L("Right to Left"),
    NONE("")
}

fun getVocal2Location(vocalLocation: VocalLocation, isFront2Left: Boolean = true): Location {
    if (isFront2Left)
        return when(vocalLocation) {
            VocalLocation.FRONT_CONSONANT, VocalLocation.FRONT_VOWEL -> Location.LEFT
            VocalLocation.BACK_CONSONANT, VocalLocation.BACK_VOWEL -> Location.RIGHT
            VocalLocation.MIDDLE -> Location.BOTH
            VocalLocation.FRONT2BACK -> Location.L2R
            VocalLocation.BACK2FRONT -> Location.R2L
            VocalLocation.NONE -> Location.NONE
        }
    else
        return when(vocalLocation) {
            VocalLocation.FRONT_CONSONANT, VocalLocation.FRONT_VOWEL -> Location.RIGHT
            VocalLocation.BACK_CONSONANT, VocalLocation.BACK_VOWEL-> Location.LEFT
            VocalLocation.MIDDLE -> Location.BOTH
            VocalLocation.FRONT2BACK -> Location.R2L
            VocalLocation.BACK2FRONT -> Location.L2R
            VocalLocation.NONE -> Location.NONE
        }
}

enum class PhonemeGroup(val group: String) {
    Plosive("Plosive"),
    Fricative("Fricative"),
    Affricate("Plosive+Fricative"),
    Nasals("Nasals, Approximant"),
    Vowel("Monothongs"),
    Diphthongs("Diphthongs"),
    None("")
}

object PhonemeGroupProvider{
    val locationGroup = mapOf(
        VocalLocation.FRONT_CONSONANT to listOf("f", "v", "b", "p", "m"),
        VocalLocation.BACK_CONSONANT to listOf("h", "g", "c", "k", "q"),
        VocalLocation.MIDDLE to listOf("t", "d", "j", "l", "r", "x", "s", "z", "n"),
        VocalLocation.FRONT_VOWEL to listOf("u"),
        VocalLocation.BACK_VOWEL to listOf("a", "e", "i"),
        VocalLocation.FRONT2BACK to listOf("o", "w"),
        VocalLocation.BACK2FRONT to listOf("y")
    )

    val phonemeGroups = mapOf(
        PhonemeGroup.Plosive to listOf("p", "k", "c", "q", "t", "d", "b", "g"),
        PhonemeGroup.Fricative to listOf("f", "s", "h", "v", "z"),
        PhonemeGroup.Affricate to listOf("j", "x"),
        PhonemeGroup.Nasals to listOf("m", "n", "l", "r"),
        PhonemeGroup.Vowel to listOf("a", "e", "i", "u"),
        PhonemeGroup.Diphthongs to listOf("y", "o", "w")
    )
}

fun getLocation(letter: String, isFront2Left: Boolean = true): Location {
    locationGroup.forEach() {
            (key: VocalLocation, value: List<String>) ->
        if (value.contains(letter)) {
            return getVocal2Location(key, isFront2Left)
        }
    }
    return Location.NONE
}


fun getPhonemeGroup(key: String): PhonemeGroup {
    phonemeGroups.forEach() {
        if (it.value.contains(key)) return it.key
    }
    return PhonemeGroup.None
}

@Composable
fun GroupIntro(
    innerPadding: PaddingValues,
    soundManager: SoundManager?,
    hapticManager: HapticManager?,
    category: String,
    group: String,
) {
    var filteredNames = mutableListOf<String>()
    var nonEmptyGroups = mutableListOf<List<String>>()
    val allowGroup = getAllowGroup(group)
    if (category == "phoneme") {
         phonemeGroups.forEach() {
            (key: PhonemeGroup, value: List<String>) ->
             val filteredGroup = value.intersect(allowGroup).toList()
                if (filteredGroup.isNotEmpty()) {
                    nonEmptyGroups.add(filteredGroup)
                    filteredNames.add(key.group)
                }
         }
    } else if(category == "location") {
        locationGroup.forEach() { (key: VocalLocation, value: List<String>) ->
            if(key == VocalLocation.FRONT2BACK || key == VocalLocation.BACK2FRONT) return@forEach

            val filteredGroup = value.intersect(allowGroup).toList()
            if (filteredGroup.isNotEmpty()) {
                nonEmptyGroups.add(filteredGroup)
                filteredNames.add(key.vocalLocation)
            }
        }
        // 이중모음은 따로 취급
        val group = PhonemeGroup.Diphthongs
        val filteredGroup = phonemeGroups[group]?.intersect(allowGroup)?.toList()
        if (filteredGroup != null) {
            if (filteredGroup.isNotEmpty()) {
                nonEmptyGroups.add(filteredGroup)
                filteredNames.add(group.group)
            }
        }
    } else if(category == "location-essential") {
        val groups = mutableListOf(
            listOf("u", "i"),
            listOf("s", "f", "h"),
            listOf("d", "g"),
            listOf("c", "b"),
            listOf("n", "m"),
        )
        val names = mutableListOf("1열-모음", "2열-마찰음", "2열-파열음", "3열-파열음", "3열-비음")
        val filteredGroups = groups.map { phonemes ->
            phonemes.intersect(allowGroup).toList()
        }
        val filtered = names.zip(filteredGroups).filter { it.second.isNotEmpty() }.unzip()
        filteredNames = filtered.first.toMutableList()
        nonEmptyGroups = filtered.second.toMutableList()
    }

    TrainGroup(innerPadding, soundManager, hapticManager, nonEmptyGroups, filteredNames)
}

