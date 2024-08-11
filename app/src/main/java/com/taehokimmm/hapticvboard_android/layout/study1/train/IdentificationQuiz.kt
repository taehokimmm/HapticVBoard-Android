package com.taehokimmm.hapticvboard_android.layout.study1.train

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.database.addStudy1TrainPhase2Answer
import com.taehokimmm.hapticvboard_android.database.Study1Phase2Answer
import com.taehokimmm.hapticvboard_android.database.closeStudy1Database
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager


// PHASE 2 : Identification Test
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Study1IdentiQuiz(
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

    val totalBlock = 3
    var testIter by remember { mutableStateOf(-1) }
    var testBlock by remember { mutableStateOf(1) }
    var testList by remember { mutableStateOf(allowlist.shuffled()) }
    val testNumber = testList.size

    var selectedIndex by remember {mutableStateOf(0)}
    var selectedAnswer by remember {mutableStateOf(-1)}
    var options by remember { mutableStateOf(listOf("")) }
    var isShowAnswer by remember {mutableStateOf(false)}
    var isExplaining by remember {mutableStateOf(false)}

    // Swipe Gesture
    var horizontalDragStart by remember {mutableStateOf(0f)}
    var horizontalDragEnd by remember {mutableStateOf(0f)}
    val swipeThreshold = 100

    fun explainKey(key: String, delay: Long = 0) {
        isExplaining = true
        // Word
        delay({soundManager?.speakOutChar(key)},delay+0)
        // Phoneme
        delay({soundManager?.playPhoneme(key)}, delay+700)
        delay({
            hapticManager?.generateHaptic(
                key,
                HapticMode.PHONEME
            )
        }, delay+1500)
        delay({isExplaining = false}, delay+2500)
    }

    fun speakNextWord() {
        delay({
            soundManager.speakOutChar(testList[testIter])
        }, 500)
    }

    fun onConfirm() {
        if (isExplaining) return
        if (isShowAnswer) {
            testIter++
            if (testIter < testList.size) {
                selectedIndex = 0
                selectedAnswer = -1
                options = generateCandidates(
                    testList[testIter],
                    allowlist
                )
                speakNextWord()
                isShowAnswer = false
            }
        } else {
            // Correct Feedback
            val selectedOption = options[selectedIndex]
            val targetOption = testList[testIter]
            val isCorrect = selectedOption == targetOption
            soundManager.playSound(isCorrect)
            selectedAnswer = selectedIndex
            // Deliver the answer feedback
            isShowAnswer = true

            //--- Append Data to Database ---//
            val data = Study1Phase2Answer(
                answer = targetOption,
                perceived = selectedOption,
                iter = testIter,
                block = testBlock
            )
            addStudy1TrainPhase2Answer(context, subject, group, data)
            // ------------------------------//
            explainKey(targetOption, 500)
        }
    }

    fun onSelect(index: Int) {
        if (isExplaining) return
        selectedIndex = index

        if (isShowAnswer) {
            explainKey(testList[testIter])
        } else {
            soundManager.speakOut((index+1).toString())
            delay(
                {
                    hapticManager.generateHaptic(
                        options[index],
                        HapticMode.PHONEME
                    )

                }, 900
            )
        }
    }

    LaunchedEffect (testIter) {
        if (testIter == -1) {
            soundManager.speakOut("시작하려면 탭하세요.")
        }
    }
    // Identification Test
    if (testIter == -1) {
        isShowAnswer = false
        selectedIndex = 0
        options = listOf("")
        selectedAnswer = -1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    testIter = 0
                    options = generateCandidates(
                        testList[testIter],
                        allowlist
                    )
                    speakNextWord()
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
    } else if (testIter == testNumber) {
        testBlock++
        if (testBlock > totalBlock) {
            closeStudy1Database()
            navController.navigate("study1/train/phase2/${subject}/${group}")
        } else {
            testList = allowlist.shuffled()
            testIter = -1
        }
    } else if (testIter < testNumber) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onConfirm() },
                        onTap = { onSelect(selectedIndex) }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            horizontalDragStart = offset.x
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            horizontalDragEnd = change.position.x
                        },
                        onDragEnd = {
                            if (isExplaining || isShowAnswer) return@detectHorizontalDragGestures

                            val swipeAmount = horizontalDragEnd - horizontalDragStart

                            if (swipeAmount > swipeThreshold) {
                                if (selectedIndex < options.size - 1) {
                                    selectedIndex++
                                } else {
                                    selectedIndex = 0
                                }
                                onSelect(selectedIndex)
                            } else if (swipeAmount < -swipeThreshold) {
                                if (selectedIndex > 0) {
                                    selectedIndex--
                                } else {
                                    selectedIndex = options.size - 1
                                }
                                onSelect(selectedIndex)
                            }
                        }
                    )
                }
        ) {
            TestDisplay(testIter, testNumber, testList[testIter][0], soundManager, height = 150.dp)

            Button(
                onClick = {
                    closeStudy1Database()
                    navController.navigate("study1/train/phase2/${subject}/${group}")
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text("Skip")
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .align(Alignment.BottomStart)
            ) {
                FlowRow(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,

                    ) {
                    options.forEachIndexed { index, alphabet ->
                        Box (
                            modifier = Modifier
                                .size(180.dp, 180.dp)
                                .padding(10.dp)
                                .align(Alignment.CenterVertically)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onSelect(selectedIndex) },
                                        onDoubleTap = { onConfirm() }
                                    )
                                }
                                .background(
                                    if (index == selectedIndex) Color.Blue else Color.White
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isShowAnswer) alphabet.toUpperCase() else (index+1).toString(),
                                color =  (
                                    if (isShowAnswer) {
                                        if (options[index] == testList[testIter]) {
                                            Color.Green
                                        } else if (index == selectedAnswer) {
                                            Color.Red
                                        } else if(index == selectedIndex) {
                                            Color.White
                                        } else {
                                            Color.Blue
                                        }
                                    } else {
                                        if (index == selectedIndex) Color.White else Color.Blue
                                    }
                                ),
                                fontSize = 40.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

        }
    }

}

fun getAllowGroup(group: String): List<String> {
    var allow = emptyList<String>().toMutableList()
    if (group.contains("A")) allow += listOf("q", "w", "e", "a", "s", "d", "z", "x")
    if (group.contains("B")) allow += listOf("e", "d", "x", "r", "t", "f", "g", "c", "v")
    if (group.contains("C")) allow += listOf("t", "y", "u", "g", "h", "j", "v", "b", "n")
    if (group.contains("D")) allow += listOf("u", "i", "o", "p", "j", "k", "l", "n", "m")
    allow = allow.toSet().toMutableList()
    return allow
}

fun generateCandidates(key: String, allowGroup: List<String>): List<String> {
    var group = listOf(key)
    var others = allowGroup.filterNot{it == key}
    group += others.shuffled().slice(0..3)
    return group.shuffled()
}

@Composable
fun TestDisplay(testIter: Int, testNumber: Int, testLetter: Char, soundManager: SoundManager, height: Dp = 300.dp) {
    Column(
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${testIter + 1} / $testNumber", fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = testLetter.uppercase(), fontSize =120.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}


fun delay(function: () -> Unit, delayMillis: Long, handler: Handler? = null): Runnable {
    val runnable = Runnable {
        function()
    }
    if (handler == null) {
        Handler(Looper.getMainLooper()).postDelayed(
            runnable,
            delayMillis
        )
    } else {
        handler.postDelayed(
            runnable,
            delayMillis
        )
    }

    return runnable
}