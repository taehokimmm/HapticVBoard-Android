package com.taehokimmm.hapticvboard_android.layout.study1.train

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
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
    val allowlist = getAllowGroup(group)

    var testIter by remember { mutableStateOf(0) }
    var testList by remember { mutableStateOf(allowlist.shuffled()) }
    val testNumber = testList.size
    var selectedOption by remember{ mutableStateOf("1") }
    var options by remember { mutableStateOf(generateCandidates(testList[testIter], allowlist)) }
    val touchEvents = remember { mutableStateListOf<MotionEvent>() }

    // Identification Test
    if (testIter < testNumber) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TestDisplay(testIter, testNumber, testList[testIter][0], soundManager, height = 100.dp)

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
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            hapticManager.generateHaptic(
                                                options[index],
                                                HapticMode.PHONEME
                                            )
                                            selectedOption = options[index]
                                        },
                                        onDoubleTap = {
                                            // Correct Feedback
                                            val isCorrect = selectedOption == testList[testIter]
                                            soundManager.playSound(isCorrect)

                                            // Deliver the answer feedback
                                            Handler(Looper.getMainLooper()).postDelayed(
                                                {
                                                    hapticManager.generateHaptic(
                                                        testList[testIter],
                                                        HapticMode.PHONEME
                                                    )
                                                }, 500
                                            )

                                            // Move to Next Trial after 1 sec
                                            Handler(Looper.getMainLooper()).postDelayed(
                                                {
                                                    selectedOption = "1"
                                                    options = generateCandidates(
                                                        testList[testIter + 1],
                                                        allowlist
                                                    )
                                                    testIter++
                                                }, 1000
                                            )
                                        }
                                    )
                                }.background(if (alphabet == selectedOption) Color.Blue else Color.White)
                        ) {
                            Text(
                                text = (index+1).toString(),
                                color =  if (alphabet == selectedOption) Color.White else Color.Blue,
                                fontSize = 40.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    Log.e("Phase2", "Double Tapped")

                }
            ) {
                Text(text = "NEXT")
            }
        }
    } else {
        navController.navigate("study1/train/phase3/${subject}/${group}")
    }
}

fun getAllowGroup(group: String): List<String> {
    val allow = emptyList<String>().toMutableList()
    if (group.contains("L")) allow += listOf("q", "w", "e", "r", "a", "s", "d", "f", "z", "x", "c")
    if (group.contains("C")) allow += listOf("r", "t", "y", "u", "f", "g", "h", "c", "v", "b")
    if (group.contains("R")) allow += listOf("u", "i", "o", "p", "h", "j", "k", "l", "b", "n", "m")
    return allow
}

fun generateCandidates(key: String, allowGroup: List<String>): List<String> {
    var group = listOf(key)
    var others = allowGroup.filterNot{it == key}
    if (key == "q" || key == "c") {
        others = others.filterNot{it == "q" || it == "c"}
    }
    group += others.shuffled().slice(0..4)
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
