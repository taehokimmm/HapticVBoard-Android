package com.taehokimmm.hapticvboard_android

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HapticTest(soundManager: SoundManager?) {

    val defaultColor = Color.LightGray
    val pressedColor = Color.Gray
    var backgroundColor by remember { mutableStateOf(defaultColor) }
    var key by remember { mutableStateOf(getRandomAlphabet()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 90.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            TextButton(onClick = { key = getRandomAlphabet() }) {
                Text(text = "Next", color = Color(0xFF006AFF), fontSize = 20.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(60.dp))
                    .size(300.dp, 300.dp)
                    .background(backgroundColor)
                    .clickable(onClick = {
                        // Haptic feedback
                        soundManager!!.speakOut(key.toString())

                        backgroundColor =
                            if (backgroundColor == defaultColor) pressedColor else defaultColor
                    }),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "${key.uppercaseChar()}", color = Color.White, fontSize = 60.sp)
            }
        }
    }
}

fun getRandomAlphabet(): Char {
    val alphabet = ('a'..'z').toList()
    return alphabet.random()
}