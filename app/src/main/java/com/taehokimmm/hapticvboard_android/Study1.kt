package com.taehokimmm.hapticvboard_android

import android.util.Log
import android.view.MotionEvent
import android.widget.Spinner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.database.deleteDatabaseByName
import com.taehokimmm.hapticvboard_android.database.resetStudy1Data
import com.taehokimmm.hapticvboard_android.database.openStudy1
import com.taehokimmm.hapticvboard_android.database.saveStudy1Data
import com.taehokimmm.hapticvboard_android.database.study1.Study1Answer
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager
import kotlinx.coroutines.delay

@Composable
fun Study1TrainInit(navController: NavHostController) {
    var context = LocalContext.current
    var testSubjectIdentifier by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var checkboxLeftState by remember { mutableStateOf(false) }
    var checkboxCenterState by remember { mutableStateOf(false) }
    var checkboxRightState by remember { mutableStateOf(false) }

    val subjectFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Test subject identifier
            TextField(
                value = testSubjectIdentifier,
                onValueChange = { testSubjectIdentifier = it.trim() },
                maxLines = 1,
                label = { Text(text = "Test Subject", fontSize = 16.sp) },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(subjectFocusRequester),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() })
            )

            // Select Test Group
            Text(
                modifier = Modifier.padding(start = 14.dp),
                text = "Select Train Group",
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
                            deleteDatabaseByName(context, testSubjectIdentifier)
                            navController.navigate("study1/train/phase1/${testSubjectIdentifier}/${group}")
                        }
                    }
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Start Train")
            }
        }
    }
}

@Composable
fun Study1TrainPhase1(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val suppress = getSuppressGroup(group)
    var countdown by remember { mutableStateOf(300) }

    LaunchedEffect(countdown) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        navController.navigate("study1/train/phase2/${subject}/${group}")
    }
    // Free
    //
    // Play

    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

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

        Button(
            onClick = {
                navController.navigate("study1/train/phase2/${subject}/${group}")
            }, modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text("Skip")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Box {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { },
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = HapticMode.VOICEPHONEME,
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
}

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

    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableStateOf(0) }
    val testList = allowlist.shuffled()

    // Identification Test
    if (testBlock < 3) {
        val defaultColor = Color.LightGray
        val pressedColor = Color.Gray
        var backgroundColor by remember { mutableStateOf(defaultColor) }
        var key by remember { mutableStateOf(testList[testIter]) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Epoch ${testBlock + 1}, Iteration ${testIter + 1}/${testList.size}",
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
            )

            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                TextButton(onClick = {
                    testIter++
                    if (testIter == testList.size) {
                        testIter = 0
                        testBlock++
                    }
                    key = testList[testIter]
                }) {
                    Text(text = "Next", color = Color(0xFF006AFF), fontSize = 20.sp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 30.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(60.dp))
                        .size(300.dp, 300.dp)
                        .background(backgroundColor)
                        .clickable(onClick = {
                            // Haptic feedback
                            hapticManager.generateHaptic(key)
                            backgroundColor =
                                if (backgroundColor == defaultColor) pressedColor else defaultColor
                        }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = key.uppercase(), color = Color.White, fontSize = 60.sp)
                }
            }
        }
    } else {
        navController.navigate("study1/train/phase3/${subject}/${group}")
    }
}

@Composable
fun Study1TrainPhase3(
    innerPadding: PaddingValues,
    subject: String,
    group: String,
    navController: NavHostController,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    hapticMode: HapticMode
) {
    val suppress = getSuppressGroup(group)
    val allowlist = getAllowGroup(group)

    var testBlock by remember { mutableStateOf(0) }
    var testIter by remember { mutableStateOf(0) }
    var testList = remember { allowlist.shuffled() }

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    var correct by remember { mutableIntStateOf(0) }

    // Record the wrong answers and the respective correct answers
    val wrongAnswers = remember { mutableStateListOf<Char>() }
    val correctAnswers = remember { mutableStateListOf<Char>() }

    if (testIter >= testList.size) {
        testBlock++
        testIter = 0
        correct = 0
        wrongAnswers.clear()
        correctAnswers.clear()
        testList = allowlist.shuffled()
        if (testBlock >= 3) {
            navController.navigate("study1/train/end/${subject}")
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            TestDisplay(testIter, testList.size, testList[testIter][0])

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyRelease = { key ->
                            if (key == testList[testIter]) {
                                correct++
                            } else {
                                wrongAnswers.add(key[0])
                                correctAnswers.add(testList[testIter][0])
                            }
                            testIter++
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
    }
}

@Composable
fun Study1TrainEnd(subject: String, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Train Completed for $subject!", fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            navController.navigate("study1/train/init")
        }) {
            Text("Return to Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

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
                    text = {Text(option)},
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

    // Typing Test
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    if (testIter == -1) {
        soundManager.speakOut("Tap to start Block " + testBlock)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                "Block : " + testBlock
            )
            Button(
                onClick = {
                    testIter = 0
                    soundManager.speakOut(testList[0])
                },
                modifier =Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            ) {
                Text(text="Tap to Start")
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
            TestDisplay(testIter, testList.size, testList[testIter][0])

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    KeyboardLayout(
                        touchEvents = keyboardTouchEvents,
                        onKeyRelease = { key ->

                            //--- Append Data to Database ---//
                            val data = Study1Answer (
                                answer = testList[testIter],
                                perceived = key,
                                iter= testIter,
                                block= testBlock
                            )
                            saveStudy1Data(context, subject, group, data)
                            // ------------------------------//
                            testIter++

                            if (testIter < testList.size) {
                                // Speak next target alphabet key
                                soundManager.speakOut(testList[testIter])
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


fun getSuppressGroup(group: String): List<String> {
    var suppress = ('a'..'z').map { it.toString() }
    if (group.contains("L")) suppress =
        suppress.filterNot { it in listOf("q", "w", "e", "r", "a", "s", "d", "f", "z", "x", "c") }
    if (group.contains("C")) suppress =
        suppress.filterNot { it in listOf("r", "t", "y", "u", "f", "g", "h", "c", "v", "b") }
    if (group.contains("R")) suppress =
        suppress.filterNot { it in listOf("u", "i", "o", "p", "h", "j", "k", "l", "b", "n", "m") }
    return suppress
}

fun getAllowGroup(group: String): List<String> {
    val allow = emptyList<String>().toMutableList()
    if (group.contains("L")) allow += listOf("q", "w", "e", "r", "a", "s", "d", "f", "z", "x", "c")
    if (group.contains("C")) allow += listOf("r", "t", "y", "u", "f", "g", "h", "c", "v", "b")
    if (group.contains("R")) allow += listOf("u", "i", "o", "p", "h", "j", "k", "l", "b", "n", "m")
    return allow
}

@Composable
fun CheckboxWithLabel(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, fontSize = 16.sp)
    }
}