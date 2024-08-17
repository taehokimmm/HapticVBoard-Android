package com.taehokimmm.hapticvboard_android.layout.study1.train

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val totalBlock = 4
    var testBlock by remember { mutableStateOf(1) }
    var testIter by remember { mutableStateOf(-1) }
    var testList = remember { allowlist.shuffled() }


    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var startTime by remember { mutableStateOf(0L) }

    fun speak() {
        soundManager.speakOutChar(testList[testIter])
        startTime = -1L
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            soundManager.speakOutKor("시작하려면 탭하세요")
        } else if (testIter < testList.size) {
            speak()
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
                    text = "Tap to Start \n Block : " + testBlock, fontSize = 20.sp
                )
            }
        }
    } else if (testIter == testList.size) {
        testBlock++
        if (testBlock > totalBlock) {
            closeStudy1Database()
            navController.navigate("study1/train/end/${subject}")
        } else {
            testList = allowlist.shuffled()
            testIter = -1
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TestDisplay(testBlock, totalBlock, testIter, testList.size, testList[testIter][0], soundManager, height = 300.dp)
            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyPress = {key ->
                        if(startTime == -1L) startTime = System.currentTimeMillis()
                    },
                    onKeyRelease = { key ->
                        if (keyboardAllowlist.contains(key)) {
                            if (testBlock % 2 == 0)
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
                                {// Speak next target alphabet key
                                    soundManager.playSound(isCorrect)
                                }, 500
                            )

                            delay(
                                {// Speak next target alphabet key
                                    testIter++
                                }, 1500
                            )
                        }
                    },
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = if(testBlock % 2 == 1) HapticMode.VOICEPHONEME else HapticMode.PHONEME,
                    allow = keyboardAllowlist,
                    logData = Study1Phase3Log(
                        answer = testList[testIter], iter = testIter, block = testBlock
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
    }
}

