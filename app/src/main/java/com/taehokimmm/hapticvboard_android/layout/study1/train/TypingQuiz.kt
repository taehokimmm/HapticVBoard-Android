package com.taehokimmm.hapticvboard_android.layout.study1.train

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    var keyboardAllowlist = when(group) {
        "A" -> getAllowGroup("A")
        "B" -> getAllowGroup("AB")
        "C" -> getAllowGroup("ABC")
        "D" -> getAllowGroup("ABCD")
        else -> listOf("")
    }

    val allowlist = getAllowGroup(group)

    var totalBlock = 6
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableStateOf(-1) }
    var testList by remember { mutableStateOf(allowlist.shuffled()) }


    if (subject == "practice") {
        testList = testList.slice(0 .. 1)
        totalBlock = 2
    }
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

    var inputKey by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }


    var isExplaining by remember {mutableStateOf(false)}
    val handler = Handler(Looper.getMainLooper())
    var runnables = remember { mutableStateListOf<Runnable>() }

    fun explainKey(key: String, delay: Long = 0) {
        // Clear any previous runnables before adding new ones
        runnables.clear()
        isExplaining = true

        runnables.add(
            delay({
                soundManager.stop()}, delay, handler)
        )

        runnables.add(
            delay({soundManager?.speakOut(key)}, delay, handler)
        )
        // Phoneme
        runnables.add(
            delay({ soundManager.playPhoneme(key) }, 700+delay, handler)
        )

        runnables.add(
            delay({
                hapticManager.generateHaptic(
                    key,
                    HapticMode.PHONEME
                )
            }, 1400+delay, handler)
        )

        runnables.add(
            delay({isExplaining = false}, 1400, handler)
        )
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

    fun onConfirm(key: String) {
        if (keyboardAllowlist.contains(key)) {
            inputKey = key
            if (testBlock % 2 == 1)
                soundManager.speakOut(key)
            isCorrect = key == testList[testIter]
            if (isCorrect) correctAnswer++
            else wrongAnswer += listOf(listOf(testList[testIter], key))

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
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            val modeName = modeNames[testBlock%2]
            tts?.stop()
            soundManager.stop()
            soundManager.speakOutKor(modeName + " : 시작하려면 이중탭하세요")
        } else if (testIter < testList.size) {
            soundManager.stop()
            runnables.forEach { handler.removeCallbacks(it) }
            runnables.clear()
            isTypingMode = true
            speak()
        } else {
            explainResult()
        }
    }

    LaunchedEffect(isTypingMode) {
        if (isTypingMode == false) {
            if (testIter != -1 && testIter < testList.size) {
                explainKey(testList[testIter], 1500)
            }
        }
    }

    if (testIter == -1) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            testIter = 0
                        },
                        onTap = {
                            val modeName = modeNames[testBlock%2]
                            soundManager.stop()
                            soundManager.speakOut(modeName + " : 시작하려면 이중탭하세요.")
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Start \n Block : " + (testBlock+1)
                        + "\n Mode : " + modeNames[testBlock%2]
                , fontSize = 20.sp
            )
        }
    } else if (testIter < testList.size) {
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
                            startTime = System.currentTimeMillis()
                        },
                        onKeyRelease = { key ->
                            onConfirm(key)
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
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inputKey.toUpperCase(),
                            color = if (isCorrect) Color.Green else Color.Red,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            correctAnswer = 0
                            wrongAnswer = listOf(listOf(""))

                            testBlock++
                            if (testBlock >= totalBlock) {
                                closeStudy1Database()
                                navController.navigate("study1/train/end/${subject}")
                            } else {
                                testList = allowlist.shuffled()
                                testIter = -1
                            }
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

    }
}

