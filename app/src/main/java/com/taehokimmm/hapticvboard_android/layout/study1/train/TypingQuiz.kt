package com.taehokimmm.hapticvboard_android.layout.study1.train

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.addStudy1TrainPhase3Answer
import com.taehokimmm.hapticvboard_android.database.Study1Phase3Answer
import com.taehokimmm.hapticvboard_android.database.Study1Phase3Log
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.util.Locale


// Phase 3 : Typing Test
@Composable
fun Study1TypingQuiz(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val context = LocalContext.current
    val keyboardAllowlist = when(group) {
        "A" -> getAllowGroup("A")
        "B" -> getAllowGroup("AB")
        "C" -> getAllowGroup("ABC")
        "D" -> getAllowGroup("ABCD")
        else -> listOf("")
    }

    val allowlist = getAllowGroup(group)

    val totalBlock = 6
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableStateOf(-1) }
    var testList by remember { mutableStateOf(allowlist.shuffled()) }
    var modeNames = listOf("음성 모드 학습", "진동 모드 학습")


    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var startTime by remember { mutableStateOf(0L) }
    var isTypingMode by remember { mutableStateOf(false) }

    fun speak() {
        soundManager.stop()
        soundManager.speakOutChar(testList[testIter])
        startTime = -1L
    }


    var isExplaining by remember {mutableStateOf(false)}
    val handler = Handler(Looper.getMainLooper())
    var runnables = remember { mutableStateListOf<Runnable>() }

    fun explainKey(key: String, delay: Long = 0) {
        // Clear any previous runnables before adding new ones
        runnables.clear()
        isExplaining = true

        runnables.add(
            delay({soundManager?.speakOutChar(key)}, delay, handler)
        )
        // Phoneme
        runnables.add(
            delay({ soundManager.playPhoneme(key) }, 1500+delay, handler)
        )

        runnables.add(
            delay({
                hapticManager.generateHaptic(
                    key,
                    HapticMode.PHONEME
                )
            }, 2500+delay, handler)
        )

        runnables.add(
            delay({isExplaining = false}, 2500, handler)
        )
    }


    LaunchedEffect(testIter) {
        if (testIter == -1) {
            val modeName = modeNames[testBlock%2]
            soundManager.speakOutKor(modeName + " : 시작하려면 탭하세요")
        } else if (testIter < testList.size) {
            soundManager.stop()
            runnables.forEach { handler.removeCallbacks(it) }
            runnables.clear()
            isTypingMode = true
            speak()
        }
    }

    LaunchedEffect(isTypingMode) {
        if (isTypingMode == false && testIter != -1 && testIter < testList.size) {
            explainKey(testList[testIter], 1500)
        }
    }


    if (testIter == -1) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    testIter = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(corner = CornerSize(0)),
                colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
            ) {
                Text(
                    text = "Tap to Start \n Block : " + (testBlock+1)
                            + "\n Mode : " + modeNames[testBlock%2]
                    , fontSize = 20.sp
                )
            }
        }
    } else if (testIter == testList.size) {
        testBlock++
        if (testBlock >= totalBlock) {
            closeStudy1Database()
            navController.navigate("study1/train/end/${subject}")
        } else {
            testList = allowlist.shuffled()
            testIter = -1
        }
    } else {
        Box(
            modifier = when(isTypingMode) {
                true ->  Modifier
                    .fillMaxSize()
                    .padding(innerPadding)

                false ->  Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                soundManager.stop()
                                runnables.apply {
                                    forEach { handler.removeCallbacks(it) }
                                    clear()
                                }
                                soundManager.playEarcon("beep")
                                isExplaining = false

                                delay({
                                    testIter++
                                    isTypingMode = true
                                }, 500)
                            },
                            onTap = {
                                if (isExplaining) return@detectTapGestures
                                explainKey(testList[testIter])
                            }
                        )
                    }
            }
        ) {
            TestDisplay(testBlock, totalBlock, testIter, testList.size, testList[testIter][0], soundManager, height = 300.dp)
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                if (isTypingMode) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyPress = {key ->
                            if (testBlock % 2 == 0)
                                soundManager.stop()
                            if(startTime == -1L) startTime = System.currentTimeMillis()
                        },
                        onKeyRelease = { key ->
                            if (keyboardAllowlist.contains(key)) {
                                if (testBlock % 2 == 1)
                                    soundManager.speakOut(key)
                                val isCorrect = key == testList[testIter]
                                //--- Append Data to Database ---//
                                val curTime = System.currentTimeMillis()

                                val data = Study1Phase3Answer(
                                    answer = testList[testIter],
                                    perceived = key,
                                    iter = testIter,
                                    block = testBlock,
                                    duration = curTime - startTime
                                )
                                addStudy1TrainPhase3Answer(context, subject, group, data)
                                // ------------------------------//

                                delay(
                                    {// Correction Feedback
                                        soundManager.playSound(isCorrect)
                                    }, 500
                                )
                                isTypingMode = false
                            }
                        },
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = if(testBlock % 2 == 0) HapticMode.VOICEPHONEME else HapticMode.PHONEME,
                        allow = keyboardAllowlist,
                        logData = Study1Phase3Log(
                            answer = testList[testIter], iter = testIter, block = testBlock
                        ),
                        name = subject + "_" + group.last()
                    )
                }
            }
        }

        if (isTypingMode) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                MultiTouchView(
                    context,
                    onTap = {speak()}
                ).apply {
                    onMultiTouchEvent = { event ->
                        keyboardTouchEvents.clear()
                        keyboardTouchEvents.add(event)
                    }
                }
            })
        }
    }
}

