package com.taehokimmm.hapticvboard_android.layout.study2.train

import android.util.Log
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

    var timer by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(0) }

    fun onConfirm(inputText: String): Boolean {
        var delay = 0L
        isTypingMode = false
        if (modeIter <= 1) {
            val isCorrect = (inputText == testList[testIter])
            soundManager.playSound(isCorrect)
            delay = 1000L
        }

        addLogging(inputText)
        timer = 0
        delay({
            testIter++
            initMetric()
        }, delay)
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
            temp = 10 - timer
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
            Log.d("study2train", testList.toString())
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
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                if (isTypingMode) {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyPress = { key ->
                            if (modeIter == 0)
                                soundManager.stop()
                            if (startTime == -1L)
                                startTime = System.currentTimeMillis()
                        },
                        onKeyRelease = { key ->
                            onConfirm(key)
                            if (modeIter == 1) soundManager.speakOut(key)
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