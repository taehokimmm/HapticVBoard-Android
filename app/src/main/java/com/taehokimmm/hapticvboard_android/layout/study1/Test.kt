package com.taehokimmm.hapticvboard_android.layout.study1

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.HapticMode
import com.taehokimmm.hapticvboard_android.layout.view.KeyboardLayout
import com.taehokimmm.hapticvboard_android.layout.view.MultiTouchView
import com.taehokimmm.hapticvboard_android.database.addStudy1Answer
import com.taehokimmm.hapticvboard_android.database.resetStudy1Data
import com.taehokimmm.hapticvboard_android.database.study1.Study1Answer
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import java.util.Timer
import java.util.TimerTask


@Composable
fun Study1TestInit(navController: NavHostController) {
    val context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("test") }
    var errorMessage by remember { mutableStateOf("") }

    var checkboxLeftState by remember { mutableStateOf(false) }
    var checkboxCenterState by remember { mutableStateOf(false) }
    var checkboxRightState by remember { mutableStateOf(false) }

    val subjectFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var subjects = listOf("test")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 5) {
        subjects += listOf("Pilot" + i)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Test subject identifier
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Subject",
                fontSize = 16.sp
            )

            Spinner(
                options = subjects,
                onOptionSelected = { selectedOption ->
                    testSubjectIdentifier = selectedOption.trim()
                }
            )

            // Select Test Group
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Test Group",
                fontSize = 16.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CheckboxWithLabel(
                    checked = checkboxLeftState,
                    onCheckedChange = { checkboxLeftState = it },
                    label = "Left"
                )
                CheckboxWithLabel(
                    checked = checkboxCenterState,
                    onCheckedChange = { checkboxCenterState = it },
                    label = "Center"
                )
                CheckboxWithLabel(
                    checked = checkboxRightState,
                    onCheckedChange = { checkboxRightState = it },
                    label = "Right"
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        testSubjectIdentifier.isEmpty() -> errorMessage =
                            "Please enter a test subject"

                        !checkboxLeftState && !checkboxCenterState && !checkboxRightState -> errorMessage =
                            "Please select a test group"

                        else -> {
                            val group = buildString {
                                if (checkboxLeftState) append("L")
                                if (checkboxCenterState) append("C")
                                if (checkboxRightState) append("R")
                            }
                            resetStudy1Data(context, testSubjectIdentifier, group)
                            navController.navigate("study1/test/${testSubjectIdentifier}/${group}")
                        }
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start Test")
            }
        }
    }
}

@Composable
fun Spinner(options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }

    Box {
        Text(
            text = selectedOption,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = true })
                .background(
                    color = Color.LightGray
                )
                .padding(16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        onOptionSelected(option)
                    })
            }
        }
    }
}

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

    val suppress = getSuppressGroup(group)
    val allowlist = getAllowGroup(group)
    var testBlock by remember { mutableStateOf(1) }
    var testIter by remember { mutableStateOf(-1) }

    var testList = remember { allowlist.shuffled() }
    var startTime by remember { mutableStateOf(0L) }

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }
    val timer = Timer()
    var timerTask: TimerTask = object: TimerTask() {
        override fun run(){
            return
        }
    }

    if (testIter == -1) {
        // Audio Explanation
        soundManager.speakOut("Tap to start Block " + testBlock)
        // Logger
//        timerTask = object: TimerTask() {
//            override fun run() {
//                val data = Study1Logging(
//                    answer = testList[testIter],
//                    touched = "a",
//                    iter = testIter,
//                    block = testBlock,
//                    timestamp = System.currentTimeMillis()/1000,
//                    x = 0,
//                    y = 0,
//                    state = "Touch"
//                )
//                addStudy1Log(context, subject, group, data)
//            }
//        }
        // Layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    soundManager.speakOut("Press :"+testList[0])
                    startTime = System.currentTimeMillis()
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
    }
    else if (testIter >= testList.size) {
        testBlock++
        testList = allowlist.shuffled()
        if (testBlock > 3) {
            navController.navigate("study1/test/end/${subject}")
        } else {
            testIter = -1
        }
    }
    else {
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
                            // Stop Logging
                            timerTask.cancel()
                            timer.cancel()
                            timer.purge()
                            //--- Append Data to Database ---//
                            val curTime = System.currentTimeMillis()
                            val data = Study1Answer (
                                answer = testList[testIter],
                                perceived = key,
                                iter = testIter,
                                block = testBlock,
                                duration = curTime - startTime
                            )
                            addStudy1Answer(context, subject, group, data)
                            // ------------------------------//
                            testIter++

                            if (testIter < testList.size) {
                                // Speak next target alphabet key
                                soundManager.speakOut("Press : "+testList[testIter])
                                startTime = System.currentTimeMillis()
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

        timer.schedule(timerTask, 0, 100)
    }
}

@Composable
fun Study1TestEnd(subject: String, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Test Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            navController.navigate("study1/test/init")
        }) {
            Text("Return to Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}



@Composable
fun CheckboxWithLabel(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, fontSize = 16.sp)
    }
}