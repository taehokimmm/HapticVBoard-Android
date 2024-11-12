package com.taehokimmm.hapticvboard_android.layout.typingtest

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.closeStudyDatabase
import com.taehokimmm.hapticvboard_android.layout.vibrationtest.getAllowGroup
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import kotlinx.coroutines.delay


// PHASE 1 : FREE PLAY
@Composable
fun TypingTestFreePlay(
    innerPadding: PaddingValues,
    subject: String,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    group: String,
    block: Int,
    mode: String
) {

    var allowGroup = getAllowGroup(group)
    var countdown by remember { mutableStateOf(0) }

    LaunchedEffect(countdown) {
        delay(1000L)
        countdown++
    }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Show countdown (MM:SS)
            Text(
                text = "%02d:%02d".format(countdown / 60, countdown % 60),
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Button(
                onClick = {
                    closeStudyDatabase()
                    navController?.navigate("typingTest/train/${subject}/${group}/${block}/${mode}")
                }, modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text("Skip")
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = inputText,
                fontSize = 30.sp
            )
            KeyboardLayout(
                touchEvents = keyboardTouchEvents,
                onKeyRelease = { key ->
                    if (key == "Backspace") {
                        if (inputText.isNotEmpty()) {
                            val deletedChar = inputText.last()
                            soundManager.speakOut(deletedChar + " Deleted")
                            hapticManager?.generateHaptic(deletedChar.toString(), HapticMode.PHONEME)
                            inputText = inputText.dropLast(1)
                        }
                    }
                },
                soundManager = soundManager,
                hapticManager = hapticManager,
                hapticMode = HapticMode.VOICEPHONEME,
                allow = allowGroup
            )

            AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
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