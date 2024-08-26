package com.taehokimmm.hapticvboard_android.layout.study1.test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    var testIter by remember { mutableStateOf(0) }

    var testList = remember { allowlist.shuffled() }

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }
    var startTime by remember { mutableStateOf(0L) }
    var isTypingMode by remember { mutableStateOf(false) }

    fun speak() {
        soundManager.stop()
        soundManager.speakOutChar(testList[testIter])
        startTime = -1L
    }

    var correctAnswer by remember {mutableStateOf(0)}
    var wrongAnswer by remember {mutableStateOf(listOf(listOf("")))}

    var isSpeakingDone by remember { mutableStateOf(true) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        // Initiate TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeakingDone = false
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeakingDone = true
                    }

                    override fun onError(utteranceId: String?) {
                    }
                })
            }
        }
    }

    fun speakOutEng(word: String) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setLanguage(Locale.US)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
    }

    fun speakOutKor(word: String) {
        isSpeakingDone = false
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId")
        tts?.setLanguage(Locale.KOREA)
        tts?.speak(word, TextToSpeech.QUEUE_ADD, params, "utteranceId")
    }

    fun explainResult() {
        if (isSpeakingDone == false) return
        // Explain Result
        speakOutKor(testList.size.toString() + "개 중 " + correctAnswer.toString() + "개 정답")
        speakOutKor("오답")

        if (wrongAnswer.size == 1) {
            speakOutKor("없음")
        } else {
            wrongAnswer.forEach { wrong ->
                if (wrong.size == 1) return@forEach
                speakOutEng(wrong[0])
            }
        }
    }

    LaunchedEffect(testIter) {
        if (testIter < testList.size) {
            isTypingMode = true
            speak()
        } else {
            explainResult()
        }
    }

    if (testIter >= testList.size) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            closeStudy1Database()
                            navController.navigate("study1/test/end/${subject}")
                        },
                        onTap = {
                            explainResult()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                // Show countdown (MM:SS)
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "정답 : " + correctAnswer +" / " + testList.size,
                    fontSize = 40.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text (text = "눌러야하는 키 / 실제 누른 키")
                wrongAnswer.forEach { wrong ->
                    if (wrong.size == 2) {
                        Text(text = wrong[0] + " : " + wrong[1], fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
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
                if (isTypingMode) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyPress = {
                            key -> startTime = System.currentTimeMillis() },
                        onKeyRelease = { key ->
                            if (keyboardAllowlist.contains(key)) {
                                val isCorrect = key == testList[testIter]
                                if (isCorrect) correctAnswer++
                                else wrongAnswer += listOf(listOf(testList[testIter], key))

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
                                    }, 1000
                                )
                                isTypingMode = false
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

                }
            }
        }

        if (isTypingMode) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                MultiTouchView(
                    context,
                    onTap = { speak()}
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