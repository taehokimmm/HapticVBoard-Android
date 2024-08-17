package com.taehokimmm.hapticvboard_android.layout.study1.test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.addStudy1Answer
import com.taehokimmm.hapticvboard_android.database.Study1TestAnswer
import com.taehokimmm.hapticvboard_android.database.Study1TestLog
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.layout.study1.train.TestDisplay
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.layout.study1.train.getAllowGroup
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


@Composable
fun Study1Test(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val context = LocalContext.current

    val allowlist = getAllowGroup(group)
    val keyboardAllowlist = getAllowGroup(group)

    val totalBlock = 1
    var testBlock by remember { mutableStateOf(1) }
    var testIter by remember { mutableStateOf(-1) }

    var testList = remember { allowlist.shuffled() }

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }
    val timer = Timer()
    var timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            return
        }
    }


    var startTime by remember { mutableStateOf(0L) }

    fun speak() {
        soundManager.speakOutChar(testList[testIter])
        startTime = -1L
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            soundManager.speakOutKor("시작하려면 탭하세요")
        }
    }

    if (testIter == -1) {
        // Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    testIter = 0
                    speak()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(corner = CornerSize(0)),
                colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
            ) {
                Text(
                    text = "Tap to Start \n Block : " + testBlock, fontSize = 20.sp
                )
            }
        }
    } else if (testIter >= testList.size) {
        testBlock++
        testList = allowlist.shuffled()
        if (testBlock > totalBlock) {
            closeStudy1Database()
            navController.navigate("study1/test/end/${subject}")
        } else {
            testIter = -1
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TestDisplay(testBlock, totalBlock, testIter, testList.size, testList[testIter][0], soundManager)

            val answer = testList[testIter]
            val iter = testIter
            val block = testBlock
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyPress = {key ->
                        if (startTime == -1L)
                            startTime = System.currentTimeMillis() },
                    onKeyRelease = { key ->
                        if (keyboardAllowlist.contains(key)) {
                            //--- Append Data to Database ---//
                            val curTime = System.currentTimeMillis()
                            val data = Study1TestAnswer(
                                answer = testList[testIter],
                                perceived = key,
                                iter = testIter,
                                block = testBlock,
                                duration = curTime - startTime
                            )
                            addStudy1Answer(context, subject, group, data)
                            // ------------------------------//
                            Handler(Looper.getMainLooper()).postDelayed(
                                {// Speak next target alphabet key
                                    testIter++
                                    if (testIter < testList.size) speak()
                                }, 200
                            )
                        }
                    },
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = hapticMode,
                    allow = keyboardAllowlist,
                    logData = Study1TestLog(
                        answer = answer, iter = iter, block = block
                    ),
                    name = subject + "_" + group.last()
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
        timer.schedule(timerTask, 0, 100)
    }
}