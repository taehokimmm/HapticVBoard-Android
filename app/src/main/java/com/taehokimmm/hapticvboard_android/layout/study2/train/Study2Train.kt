package com.taehokimmm.hapticvboard_android.layout.study2.train

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableLongStateOf
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
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.database.Study2TrainAnswer
import com.taehokimmm.hapticvboard_android.database.Study2TrainLog
import com.taehokimmm.hapticvboard_android.database.addStudy2TrainAnswer
import com.taehokimmm.hapticvboard_android.database.closeStudy2Database
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

@Composable
fun Study2Train(
    innerPadding: PaddingValues,
    subject: String,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val totalBlock = 4
    val testNumber = 26
    val modeCnt = 3
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableIntStateOf(-1) }
    var modeIter by remember { mutableIntStateOf(0) }
    var modeNames = listOf("음성 모드 학습", "진동 모드 학습", "테스트")

    var testAlphabets = ('a'..'z').map { it.toString() }

    var testList by remember { mutableStateOf(listOf("")) }

    var isTypingMode by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }

    val databaseName = subject+ "_study2"

    var timer by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }

    var isExplaining by remember {mutableStateOf(false)}
    val handler = Handler(Looper.getMainLooper())
    var runnables = remember { mutableStateListOf<Runnable>() }

    var inputKey by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }

    fun initMetric() {
        startTime = System.currentTimeMillis()
    }

    fun speak(word: String) {
        soundManager.stop()
        soundManager.speakOutChar(word)
        startTime = -1L
    }

    fun addLogging(inputText: String) {
        endTime = System.currentTimeMillis()
        val data = Study2TrainAnswer(
            answer = testList[testIter],
            perceived = inputText,
            iteration = testIter,
            mode = modeIter,
            block = testBlock,
            duration = endTime - startTime
        )
        addStudy2TrainAnswer(context, databaseName, data)
    }

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
                hapticManager?.generateHaptic(
                    key,
                    HapticMode.PHONEME
                )
            }, 2200+delay, handler)
        )

        runnables.add(
            delay({isExplaining = false}, 2200, handler)
        )
    }


    fun onConfirm(inputText: String): Boolean {
        isTypingMode = false
        inputKey = inputText
        isCorrect = (inputText == testList[testIter])

        if (!isCorrect) {
            if (testIter != -1 && testIter < testList.size
                && modeIter < 2) {
                explainKey(testList[testIter], 1500)
            }
        }
        if (modeIter <= 1) {
            delay(
                {// Correction Feedback
                    soundManager.playSound(isCorrect)
                }, 500
            )
        }

        addLogging(inputText)
        timer = 0

        if (modeIter == 2 || isCorrect) {
            delay({
                testIter++
                initMetric()
            }, 1000)
        }
        return true
    }


    LaunchedEffect(timer) {
        kotlinx.coroutines.delay(1000L)
        timer++

        var temp: Int
        if (testBlock == 0 && modeIter == 0) {
            temp = 0
        } else if (modeIter == 0) {
            temp = 60 - timer
        } else {
            temp = 3 - timer
        }
        if (temp < 0) temp = 0
        if (temp != countdown) countdown = temp
    }

    LaunchedEffect(modeIter) {
        if (modeIter >= modeCnt) {
            testBlock++
            if (testBlock > totalBlock) {
                closeStudy2Database()
                navController!!.navigate("study2/train/end/$subject")
            } else {
                modeIter = 0
            }
            return@LaunchedEffect
        }
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            testList = testAlphabets.shuffled()
            timer = 0
        } else if (testIter >= testList.size) {
            modeIter++
            testIter = -1
        } else {
            isTypingMode = true
            speak(testList[testIter])
        }
    }

    LaunchedEffect(countdown) {
        if (countdown == 0) {
            val modeName = modeNames[modeIter]
            soundManager.speakOut(modeName + " : 시작하려면 탭하세요.")
        }
    }

    if (testIter == -1) {
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

            if (countdown == 0) {
                Button(
                    onClick = {
                        testIter = 0
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(corner = CornerSize(0)),
                    colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
                ) {
                    Text(
                        text = "Tap to Start \n Block : " + (testBlock + 1) + "\n Mode : " + modeNames[modeIter],
                        fontSize = 20.sp
                    )
                }
            }
        }
    } else if (testIter < testList.size) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TrainTextDisplay(testBlock, totalBlock, testIter, testNumber, modeIter, testList[testIter], soundManager)
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
                                    if (isExplaining) return@detectTapGestures
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
                if (isTypingMode) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyPress = { key ->
                            if (modeIter == 0)
                                soundManager.stop()
                            startTime = System.currentTimeMillis()
                        },
                        onKeyRelease = { key ->
                            if (modeIter == 1) soundManager.speakOut(key)
                            onConfirm(key)
                        },
                        enterKeyVisibility = false,
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = if (modeIter == 0) HapticMode.VOICEPHONEME else HapticMode.PHONEME,
                        logData = Study2TrainLog(
                            block = testBlock,
                            iteration = testIter,
                            mode = modeIter,
                            answer = testList[testIter]
                        ),
                        name = databaseName
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
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight(),
                factory = { context ->
                    MultiTouchView(
                        context,
                        onTap = {speak(testList[testIter])}
                    ).apply {
                        onMultiTouchEvent = { event ->
                            keyboardTouchEvents.clear()
                            keyboardTouchEvents.add(event)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TrainTextDisplay(testBlock: Int, blockNumber: Int, testIter: Int, testNumber: Int, modeIter: Int, testString: String, soundManager: SoundManager) {
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Block : ${testBlock + 1} / $blockNumber", fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mode : ${listOf("Train-Voice", "Train-Phoneme", "Test")[modeIter]}", fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${testIter + 1} / $testNumber", fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = testString.uppercase(), fontSize = 120.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}