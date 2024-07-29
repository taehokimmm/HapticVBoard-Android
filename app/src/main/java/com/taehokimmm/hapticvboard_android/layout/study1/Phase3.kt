package com.taehokimmm.hapticvboard_android.layout.study1

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager


// Phase 3 : Typing Test
@Composable
fun Study1TrainPhase3(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val suppress = getSuppressGroup(group)
    val allowlist = getAllowGroup(group)

    var testBlock by remember { mutableStateOf(1) }
    var testIter by remember { mutableStateOf(-1) }
    var testList = remember { allowlist.shuffled() }

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var correct by remember { mutableIntStateOf(0) }

    // Record the wrong answers and the respective correct answers
    val wrongAnswers = remember { mutableStateListOf<Char>() }
    val correctAnswers = remember { mutableStateListOf<Char>() }

    if (testIter == -1) {
        soundManager.speakOut("Tap to start Block " + testBlock)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    soundManager.speakOut("Press :"+testList[0])
                    testIter = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(corner = CornerSize(0)),
                colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
            ) {
                Text(text="Tap to Start \n Block : " + testBlock,
                    fontSize = 20.sp)
            }
        }
    } else if (testIter >= testList.size) {
        testBlock++
        correct = 0
        wrongAnswers.clear()
        correctAnswers.clear()
        testList = allowlist.shuffled()
        if (testBlock > 3) {
            navController.navigate("study1/train/end/${subject}")
        } else {
            testIter = -1
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            TestDisplay(testIter, testList.size, testList[testIter][0], soundManager)

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyRelease = { key ->
                            soundManager.speakOut(key)

                            val isCorrect = key == testList[testIter]
                            if (key == testList[testIter]) {
                                correct++
                            } else {
                                wrongAnswers.add(key[0])
                                correctAnswers.add(testList[testIter][0])
                            }
                            Handler(Looper.getMainLooper()).postDelayed(
                                {// Speak next target alphabet key
                                    soundManager.playSound(isCorrect)
                                },500
                            )
                            if (testIter < testList.size) {
                                Handler(Looper.getMainLooper()).postDelayed(
                                    {// Speak next target alphabet key
                                        testIter++
                                        soundManager.speakOut("Press :" + testList[testIter]) },
                                    1500
                                )
                            }
                        },
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = hapticMode,
                        suppress = suppress
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
}


@Composable
fun Study1TrainEnd(subject: String, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Train Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            navController.navigate("study1/train/init")
        }) {
            Text("Return to Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
