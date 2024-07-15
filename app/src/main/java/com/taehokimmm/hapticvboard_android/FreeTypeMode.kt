package com.taehokimmm.hapticvboard_android

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FreeTypeMode(soundManager: SoundManager?) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            TextButton(onClick = { inputText = "" }) {
                Text("Clear", color = Color(0xFF006AFF), fontSize = 20.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp, 16.dp)
                    .heightIn(min = 30.dp, max = 200.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = inputText,
                    fontSize = 20.sp,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents, onKeyRelease = { key ->
                        inputText = when (key) {
                            "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                            "Space" -> "$inputText "
                            "Shift" -> inputText
                            else -> inputText + key
                        }
                    }, soundManager = soundManager
                )
                AndroidView(modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                    factory = { context ->
                        MultiTouchView(context).apply {
                            onMultiTouchEvent = { event ->
                                keyboardTouchEvents.clear()
                                keyboardTouchEvents.add(event)
                            }
                        }
                    })
            }
        }
    }
}

@Preview
@Composable
fun PreviewFreeTypeMode() {
    FreeTypeMode(null)
}