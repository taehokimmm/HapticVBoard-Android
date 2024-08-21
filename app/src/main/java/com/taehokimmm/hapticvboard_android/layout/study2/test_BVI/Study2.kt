package com.taehokimmm.hapticvboard_android.layout.study2.test_BVI

import android.view.MotionEvent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.Study2BVITestAnswer
import com.taehokimmm.hapticvboard_android.database.Study2BVITestLog
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.database.addStudy2BVIMetric
import com.taehokimmm.hapticvboard_android.database.closeStudy2Database
import com.taehokimmm.hapticvboard_android.layout.study1.train.TestDisplay
import com.taehokimmm.hapticvboard_android.layout.study1.train.delay
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

@Composable
fun Study2BVITest(
    innerPadding: PaddingValues,
    subject: String,
    navController: NavHostController?,
    soundManager: SoundManager,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val totalBlock = 3
    val testNumber = 26
    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableIntStateOf(-1) }

    var testAlphabets = ('a'..'z').map { it.toString() }

    var testList by remember { mutableStateOf(listOf("")) }

    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }

    val databaseName = subject + "_" + when(hapticMode) {
        HapticMode.TICK -> "vibration"
        HapticMode.PHONEME -> "phoneme"
        HapticMode.VOICE -> "audio"
        HapticMode.VOICEPHONEME -> "voicephoneme"
        else -> ""
    }

    fun initMetric() {
        startTime = System.currentTimeMillis()
    }

    LaunchedEffect(testBlock) {
        testList = testAlphabets.shuffled()
    }

    fun speak(word: String) {
        soundManager.speakOutChar(word)
        startTime = -1L
    }

    fun addLogging(inputText: String) {
        endTime = System.currentTimeMillis()
        val data = Study2BVITestAnswer(
            answer = testList[testIter],
            perceived = inputText,
            iteration = testIter,
            block = testBlock,
            duration = endTime - startTime
        )
        addStudy2BVIMetric(context, databaseName, data)
    }

    fun onConfirm(inputText: String) {
        addLogging(inputText)
        initMetric()
        delay({
            testIter++
        }, 500)
    }

    var timer by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }

    LaunchedEffect(timer) {
        kotlinx.coroutines.delay(1000L)
        timer++
    }

    LaunchedEffect(testIter) {
        if (testIter == -1) {
            timer = 0
        } else if (testIter >= testList.size) {
            testBlock++
            if (testBlock > totalBlock) {
                closeStudy2Database()
                navController!!.navigate("study2/BVI/end/$subject")
            } else {
                testIter = -1
            }
        } else {
            speak(testList[testIter])
        }
    }

    LaunchedEffect(timer) {
        var temp: Int
        if (testBlock == 0) {
            temp = 0
        } else {
            temp = 30 - timer
        }
        if (temp < 0) temp = 0
        if (temp != countdown) countdown = temp
    }

    LaunchedEffect(countdown) {
        if (countdown == 0) {
            soundManager.speakOut("시작하려면 탭하세요.")
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
                        text = "Tap to Start \n Block : " + (testBlock + 1), fontSize = 20.sp
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
            TestDisplay(testBlock, totalBlock, testIter, testNumber, testList[testIter][0], soundManager)
            Column(
                modifier = Modifier
                    .align(Alignment.End)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box (
                    modifier = Modifier
                        .align(Alignment.End)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyPress = { key ->
                            if (startTime == -1L)
                                startTime = System.currentTimeMillis()
                        },
                        onKeyRelease = { key ->
                            onConfirm(key)
                        },
                        enterKeyVisibility = false,
                        soundManager = soundManager,
                        hapticManager = hapticManager,
                        hapticMode = hapticMode,
                        logData = Study2BVITestLog(
                            block = testBlock,
                            iteration = testIter,
                            answer = testList[testIter]
                        ),
                        name = databaseName
                    )
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight(),
                        factory = { context ->
                            MultiTouchView(context).apply {
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
    }
}

@Composable
fun TrainTextDisplay(testBlock: Int, blockNumber: Int, testIter: Int, testNumber: Int, modeIter: Int, testString: String, soundManager: SoundManager) {
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .clickable(onClick = {
                soundManager.speakOut(testString)
            })
            .height(200.dp)
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
                text = "Iteration : ${testIter + 1} / $testNumber", fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = testString.uppercase(), fontSize = 50.sp
            )
        }
    }
}