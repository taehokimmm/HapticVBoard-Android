package com.taehokimmm.hapticvboard_android.layout.study1.train

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
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager


// PHASE 2 : Identification Test
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Study1TrainPhase2(
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

    var isStart by remember {mutableStateOf(false)}
    var testIter by remember { mutableStateOf(0) }
    var testBlock by remember { mutableStateOf(1) }
    var testList by remember { mutableStateOf(allowlist.shuffled()) }
    val testNumber = testList.size

    var selectedIndex by remember {mutableStateOf(0)}
    var selectedAnswer by remember {mutableStateOf(-1)}
    var options by remember { mutableStateOf(generateCandidates(testList[testIter], allowlist)) }
    var isShowAnswer by remember {mutableStateOf(false)}

    // Swipe Gesture
    var swipeAmount by remember{mutableStateOf(0f)}
    var swipeStartTime by remember{mutableStateOf(0L)}

    fun onConfirm() {
        if (isShowAnswer) {
            // Move to Next Trial after 1 sec
            if (testIter < testList.size - 1) {
                selectedIndex = 0
                selectedAnswer = -1
                options = generateCandidates(
                    testList[testIter + 1],
                    allowlist
                )
                testIter++
                soundManager.speakOut("Find : " +  testList[testIter])
                isShowAnswer = false
            } else {
                navController.navigate("study1/train/phase3/${subject}/${group}")
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
        }
    }

    fun onSelect(index: Int) {
        hapticManager.generateHaptic(
            options[index],
            HapticMode.PHONEME
        )
        selectedIndex = index

        if (isShowAnswer) {
            soundManager.speakOut(options[index])
        }
    }
    if (!isStart) {
        soundManager.speakOut("Find : " +  testList[testIter])
        isStart = true
    }
    // Identification Test
    if (testIter < testNumber) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {onConfirm()},
                        onTap = {onSelect(selectedIndex)}
                    )
                }.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {offset ->
                            swipeStartTime = System.currentTimeMillis() },
                        onHorizontalDrag = {change, dragAmount ->
                            swipeAmount = dragAmount},
                        onDragEnd = {
                            val time = System.currentTimeMillis() - swipeStartTime
                            val speed = swipeAmount / time * 1000
                            if (swipeAmount > 0 && speed > 0) {
                                if (selectedIndex < options.size - 1) {
                                    selectedIndex ++
                                } else {
                                    selectedIndex = 0
                                }
                                onSelect(selectedIndex)
                            } else if (swipeAmount < 0 && speed < 0) {
                                if (selectedIndex > 0) {
                                    selectedIndex --
                                } else {
                                    selectedIndex = options.size - 1
                                }
                                onSelect(selectedIndex)
                            }
                        }
                    )
                }
        ) {
            TestDisplay(testIter, testNumber, testList[testIter][0], soundManager, height = 400.dp)

            Button(
                onClick = {
                    navController.navigate("study1/train/phase3/${subject}/${group}")
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
                                        onTap = {onSelect(selectedIndex)},
                                        onDoubleTap = {onConfirm()}
                                    )
                                }.background(
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
    val allow = emptyList<String>().toMutableList()
    if (group.contains("1")) allow += listOf("q", "w", "e", "a", "s", "d", "z", "x")
    if (group.contains("2")) allow += listOf("e", "d", "x", "r", "t", "f", "g", "c", "v")
    if (group.contains("3")) allow += listOf("t", "y", "u", "g", "h", "j", "v", "b", "n")
    if (group.contains("4")) allow += listOf("u", "i", "o", "p", "j", "k", "l", "n", "m")
    return allow
}

fun generateCandidates(key: String, allowGroup: List<String>): List<String> {
    var group = listOf(key)
    var others = allowGroup.filterNot{it == key}
    group += others.shuffled().slice(0..2)
    return group.shuffled()
}

@Composable
fun TestDisplay(testIter: Int, testNumber: Int, testLetter: Char, soundManager: SoundManager, height: Dp = 420.dp) {
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

        Button(
            onClick = {
                soundManager.speakOut(testLetter.toString())
            },
            modifier = Modifier.fillMaxWidth().height(height),
            shape = RoundedCornerShape(corner = CornerSize(0)),
            colors = ButtonColors(Color.White, Color.Black, Color.Gray, Color.Gray)
        ) {
            Text(
                text = testLetter.uppercase(), fontSize = 60.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}
