package com.taehokimmm.hapticvboard_android

import android.view.MotionEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.taehokimmm.hapticvboard_android.manager.HapticManager
import com.taehokimmm.hapticvboard_android.manager.SoundManager

@Composable
fun Study2Init(navController: NavHostController) {
    var testSubjectIdentifier by remember { mutableStateOf("test") }
    var testQuestions by remember { mutableIntStateOf(10) }
    var testQuestionString by remember { mutableStateOf("10") }
    var errorMessage by remember { mutableStateOf("") }

    val subjectFocusRequester = FocusRequester()
    val questionsFocusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current

    var checkboxLeftState by remember { mutableStateOf(false) }
    var checkboxCenterState by remember { mutableStateOf(false) }
    var checkboxRightState by remember { mutableStateOf(false) }

    var subjects = listOf("test")
    for(i in 1 until 12) {
        subjects += listOf("P" + i)
    }
    for(i in 1 until 5) {
        subjects += listOf("Pilot" + i)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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

            // Select Modality
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CheckboxWithLabel(
                    checked = checkboxLeftState,
                    onCheckedChange = { checkboxLeftState = it },
                    label = "Audio"
                )
                CheckboxWithLabel(
                    checked = checkboxCenterState,
                    onCheckedChange = { checkboxCenterState = it },
                    label = "Phoneme"
                )
                CheckboxWithLabel(
                    checked = checkboxRightState,
                    onCheckedChange = { checkboxRightState = it },
                    label = "Vibration"
                )
            }

            // Number of questions
            TextField(
                value = testQuestionString,
                onValueChange = {
                    testQuestionString = it
                    testQuestions = it.toIntOrNull() ?: 10
                },
                maxLines = 1,
                label = { Text(text = "Number of Questions", fontSize = 16.sp) },
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(questionsFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            testQuestionString = ""
                        }
                        if (!focusState.isFocused && testQuestionString.isEmpty()) {
                            testQuestions = 10
                            testQuestionString = testQuestions.toString()
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (testSubjectIdentifier.isNotEmpty() && testQuestions > 0) {
                        val feedback = buildString {
                            if (checkboxLeftState) append("audio")
                            if (checkboxCenterState) append("phoneme")
                            if (checkboxRightState) append("vibration")
                        }
                        navController.navigate("study2/$testSubjectIdentifier/$testQuestions/$feedback")
                    } else if (testSubjectIdentifier.isEmpty()) {
                        errorMessage = "Please enter a test subject"
                    } else {
                        errorMessage = "Number must be a positive integer"
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
fun Study2Test(
    innerPadding: PaddingValues,
    testName: String,
    testNumber: Int,
    navController: NavHostController?,
    soundManager: SoundManager?,
    hapticManager: HapticManager?,
    hapticMode: HapticMode
) {
    var inputText by remember { mutableStateOf("") }
    val keyboardTouchEvents = remember { mutableStateListOf<MotionEvent>() }

    val context = LocalContext.current
    var testIter by remember { mutableIntStateOf(0) }
    val testList = readTxtFile(context, R.raw.phrases)

//    val database = AppDatabase.getDatabase(context)
//    val testMetricDao = database.testMetricDao()

    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(0L) }
    var wordCount by remember { mutableIntStateOf(0) }
    var accuracy by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(testIter) {
        if (testIter == 0) {
            startTime = System.currentTimeMillis()
        } else if (testIter < testNumber) {
            endTime = System.currentTimeMillis()
            wordCount = inputText.split("\\s+".toRegex()).size
           // accuracy = calculateAccuracy(inputText, testList[testIter - 1])
           // val wpm = calculateWPM(startTime, endTime, wordCount)
//            testMetricDao.insert(
//                TestMetric(
//                    testName = testName,
//                    iteration = testIter,
//                    wpm = wpm,
//                    accuracy = accuracy,
//                    touchMetrics = emptyList(),
//                    timestamp = System.currentTimeMillis()
//                )
//            )
            startTime = System.currentTimeMillis()
        } else {
            navController!!.navigate("study2/end/$testName")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        TextDisplay(testIter, testNumber, testList[testIter])
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(20.dp))
                    .padding(20.dp, 16.dp)
                    .heightIn(min = 30.dp, max = 200.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = inputText,
                    fontSize = 20.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box {
                KeyboardLayout(
                    touchEvents = keyboardTouchEvents,
                    onKeyRelease = { key ->
                        inputText = when (key) {
                            "Backspace" -> if (inputText.isNotEmpty()) inputText.dropLast(1) else inputText
                            "Space" -> "$inputText "
                            "Shift" -> inputText
                            "Enter" -> {
                                testIter++
                                ""
                            }

                            else -> inputText + key
                        }
                    },
                    enterKeyVisibility = true,
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    hapticMode = hapticMode
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
fun Study2End(
    subject: String, navController: NavHostController
) {
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
            navController.navigate("study2/init")
        }) {
            Text("Return to Test Selection")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
