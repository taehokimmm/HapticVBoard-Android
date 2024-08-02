package com.taehokimmm.hapticvboard_android.layout.study1.train

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import kotlinx.coroutines.delay


// PHASE 2 : FREE PLAY
@Composable
fun Study1FreePlay(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val allowGroup = getAllowGroup(group)
    var countdown by remember { mutableStateOf(0) }

    LaunchedEffect(countdown) {
        delay(1000L)
        countdown++
    }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

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
                navController.navigate("study1/train/phase3/${subject}/${group}")
            }, modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text("Skip")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Box {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { },
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = HapticMode.VOICEPHONEME,
                    allow = allowGroup
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

fun getSuppressGroup(group: String): List<String> {
    val allow = getAllowGroup(group)
    var suppress = ('a'..'z').map { it.toString() }
    suppress = suppress.filterNot { it in allow }
    return suppress
}
