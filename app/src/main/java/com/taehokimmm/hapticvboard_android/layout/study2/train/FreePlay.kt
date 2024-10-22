package com.taehokimmm.hapticvboard_android.layout.study2.train

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.layout.study1.train.getAllowGroup
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import kotlinx.coroutines.delay


// PHASE 1 : FREE PLAY
@Composable
fun Study2FreePlay(
    innerPadding: PaddingValues,
    subject: String,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    group: String,
) {

    var allowGroup = getAllowGroup(group)
    var countdown by remember { mutableStateOf(0) }

    LaunchedEffect(countdown) {
        delay(1000L)
        countdown++
    }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

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
                    closeStudy1Database()
                    navController?.navigate("study2/train/train/${subject}/${group}")
                }, modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text("Skip")
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
        ) {
            KeyboardLayout(
                touchEvents = keyboardTouchEvents,
                onKeyRelease = { },
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