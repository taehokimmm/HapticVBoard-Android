package com.taehokimmm.hapticvboard_android

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrainMode(soundManager: SoundManager?, serialManager: SerialManager?, hapticMode: HapticMode) {

    val keys = listOf(
        listOf("a", "b", "c", "d"),
        listOf("e", "f", "g", "h"),
        listOf("i", "j", "k", "l"),
        listOf("m", "n", "o", "p"),
        listOf("q", "r", "s", "t"),
        listOf("u", "v", "w", "x"),
        listOf("y", "z")
    )
    val width = 80.dp
    val spacing = 5.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.Start
            ) {
                rowKeys.forEach { key ->
                    val backgroundColor = remember { mutableStateOf(Color.White) }
                    Box(
                        modifier = Modifier
                            .padding(spacing)
                            .clip(RoundedCornerShape(5.dp))
                            .size(width, width)
                            .background(Color.White)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        hapticFeedback(
                                            soundManager!!,
                                            serialManager!!,
                                            hapticMode,
                                            key
                                        )
                                        backgroundColor.value =
                                            if (backgroundColor.value == Color.White) Color.Gray else Color.White
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = key, fontSize = 25.sp)
                    }
                }
            }
        }
    }
}