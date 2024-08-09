package com.taehokimmm.hapticvboard_android.layout.study1.train

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
    val allowlist = getAllowGroup(group)

    val totalBlock = 4
    var testBlock by remember { mutableStateOf(1) }
    var testIter by remember { mutableStateOf(-1) }
    var testList = remember { allowlist.shuffled() }


    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var correct by remember { mutableIntStateOf(0) }

    // Record the wrong answers and the respective correct answers
    val wrongAnswers = remember { mutableStateListOf<Char>() }
    val correctAnswers = remember { mutableStateListOf<Char>() }

    var startTime by remember { mutableStateOf(0L) }
    var isSpeakingDone by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    LaunchedEffect(Unit) {
        // Initiate TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeakingDone = true
                        startTime = System.currentTimeMillis()
                    }

                    override fun onError(utteranceId: String?) {
                    }
                })
            }
        }
    }

    fun speak() {
        isSpeakingDone = false
        soundManager.speakOutChar(testList[testIter])
        delay({
            isSpeakingDone = true
        }, 500)
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            soundManager.speakOutKor("시작하려면 탭하세요")
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
    } else if (testIter == testList.size) {
        testBlock++
        if (testBlock > totalBlock) {
            closeStudy1Database()
            navController.navigate("study1/train/end/${subject}")
        } else {
            correct = 0
            wrongAnswers.clear()
            correctAnswers.clear()
            testList = allowlist.shuffled()
            testIter = -1
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TestDisplay(testIter, testList.size, testList[testIter][0], soundManager, height = 100.dp)

            if (isSpeakingDone) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
                ) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyRelease = { key ->
                            if (allowlist.contains(key)) {
                                if (testBlock % 2 == 0)
                                    soundManager.speakOut(key)

                                val isCorrect = key == testList[testIter]
                                if (key == testList[testIter]) {
                                    correct++
                                } else {
                                    wrongAnswers.add(key[0])
                                    correctAnswers.add(testList[testIter][0])
                                }
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
                                        if (testIter < testList.size) speak()
                                    }, 1500
                                )
                                isSpeakingDone = false

                            }
                        },
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = if(testBlock % 2 == 1) HapticMode.VOICEPHONEME else HapticMode.PHONEME,
                        allow = allowlist,
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
}

