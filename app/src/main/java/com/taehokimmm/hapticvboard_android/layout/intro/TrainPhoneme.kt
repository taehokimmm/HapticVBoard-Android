package com.taehokimmm.hapticvboard_android.layout.intro

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun TrainPhoneme(
) {
    var allGroups = listOf(
        listOf( listOf("f", "v"), listOf("s", "z"), listOf("h")),
        listOf(listOf("p", "b"), listOf("t", "d"), listOf("g", "k")),
        listOf(listOf("m"), listOf("n"), listOf()),
        listOf(listOf(), listOf("l", "r"), listOf()),
        listOf(listOf(), listOf("j", "x"), listOf()),
        listOf(listOf("e", "i"), listOf(), listOf("a", "u")),
    )

    var namesENG = listOf(
        "plosize",
        "stop",
        "nasal",
        "approximate",
        "plosive+fricative",
        "vowel",
    )

    var names = listOf(
        "마찰음",
        "파열음",
        "비음",
        "접근음",
        "합성음",
        "모음",
    )

    var selectedOption by remember { mutableStateOf("") }

    var locations = listOf("왼쪽", "양쪽", "오른쪽")

    val boxSize = 60.dp
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(

        ){
            Row() {
                Box(modifier = Modifier.width(30.dp),
                    contentAlignment = Alignment.Center) {
                }
                locations.forEachIndexed{i, location ->
                    Box(modifier = Modifier.width(100.dp),
                        contentAlignment = Alignment.Center) {
                        Text(text = location)
                    }
                }
            }

            allGroups.forEachIndexed {i, phonemeGroups ->
                Row(){
                    Box(modifier = Modifier.size(30.dp, boxSize),
                        contentAlignment = Alignment.Center){
                        Text(
                            text = names[i],
                            color =  (Color.Black),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    phonemeGroups.forEachIndexed{j, locationGroups ->
                        Row(modifier = Modifier.width(boxSize * 2).align(Alignment.CenterVertically)) {
                            locationGroups.forEachIndexed{k, letter ->
                                Box(modifier =
                                Modifier.size(boxSize, boxSize)
                                    .align(Alignment.CenterVertically)
                                    .background(Color.LightGray)
                                    .selectable(selected = selectedOption == letter,
                                        onClick = { selectedOption = letter }),
                                    contentAlignment = Alignment.Center
                                    ){
                                    Text(
                                        text = letter.toString(),
                                        color =  (Color.Black),
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                }}

                        }
                    }
                }
            }
        }
    }

}