package com.taehokimmm.hapticvboard_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.MotionEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val soundManager = SoundManager(this)
        setContent {
            var inputText by remember { mutableStateOf("") }
            val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
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
                        touchEvents = keyboardTouchEvents,
                        onKeyRelease = { key ->
                            when (key) {
                                "Backspace" -> if (inputText.isNotEmpty()) inputText =
                                    inputText.dropLast(1)

                                "Space" -> inputText += " "
                                "Shift" -> Unit
                                else -> inputText += key
                            }
                        },
                        soundManager = soundManager
                    )
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        factory = { context ->
                            MultiTouchView(context).apply {
                                onMultiTouchEvent = { event ->
                                    keyboardTouchEvents.clear()
                                    keyboardTouchEvents.add(event)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}