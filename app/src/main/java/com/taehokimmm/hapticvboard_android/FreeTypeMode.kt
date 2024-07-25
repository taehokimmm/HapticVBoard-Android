package com.taehokimmm.hapticvboard_android

import android.os.Build
import android.os.Handler
import android.text.InputType
import android.util.Log
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
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.time.delay
import java.util.Timer
import java.util.TimerTask

@Composable
fun FreeTypeMode(
    soundManager: SoundManager?, serialManager: SerialManager?, vibrationManager: VibrationManager?, hapticMode: HapticMode
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }
    var timer: Timer? = null
    var timerTask: TimerTask? = null


    fun onSpace() {
        var words = inputText.split(" ")
        if (words.size == 0) return
        var word = words[words.size - 1]
        // Speak out the word
        if (soundManager == null) return
        soundManager?.speakOut(word)

        // Haptic Feedback for the word
        if (serialManager == null) return
        var index = 0
        timerTask = object : TimerTask() {
            override fun run() {
                if (index == word.length) {
                    timerTask?.cancel()
                    timer?.cancel()
                    timer?.purge()
                    return
                }
                var character = word[index++]
                Log.e("SPACE", character.toString())
                hapticFeedback(
                    soundManager, serialManager, hapticMode,
                    character.toString()
                )
            }
        }
        timer = Timer()
        timer?.schedule(timerTask, 0, 300)
    }

    fun onBackspace() {
        if (inputText.length == 0) return
        var character = inputText[inputText.length - 1]
        Log.e("Backspace", character.toString())
        if (soundManager != null && serialManager != null) {
            hapticFeedback(
                soundManager, serialManager, hapticMode,
                character.toString()
            )
        }
    }

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
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        EditText(ctx).apply {
                            hint = "Enter text here"
                            textSize = 20f
                            showSoftInputOnFocus = false
                            setText(inputText)
                            setSelection(inputText.length)
                            isFocusable = true
                            isCursorVisible = true
                            isPressed=true
                        }
                    },
                    update = { editText ->
                        if (editText.text.toString() != inputText) {
                            editText.setText(inputText)
                            editText.setSelection(inputText.length)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { key ->
                        if (key == "Space") {
                            onSpace()
                        } else if(key == "Backspace") {
                            onBackspace()
                        }

                        inputText = when (key) {
                            "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                            "Space" -> "$inputText "
                            "Shift" -> inputText
                            else -> inputText + key
                        }
                    },
                    soundManager = soundManager,
                    serialManager = serialManager,
                    vibrationManager = vibrationManager,
                    hapticMode = hapticMode
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
                    })
            }
        }
    }
}

@Preview
@Composable
fun PreviewFreeTypeMode() {
    FreeTypeMode(null, null, null, HapticMode.NONE)
}